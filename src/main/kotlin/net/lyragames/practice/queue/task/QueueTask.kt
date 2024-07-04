package net.lyragames.practice.queue.task

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.manager.ArenaManager
import net.lyragames.practice.manager.MatchManager
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.match.Match
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.queue.Queue
import net.lyragames.practice.queue.QueuePlayer
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object QueueTask : BukkitRunnable() {

    init {
        runTaskTimer(PracticePlugin.instance, 20L, 20L)
    }

    override fun run() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return

        try {
            QueueManager.queues.values.forEach { queue ->
                processQueue(queue)
            }
        } catch (ignored: ConcurrentModificationException) {
        }
    }

    private fun processQueue(queue: Queue) {
        if (queue.getQueueingPlayers().isEmpty()) return

        queue.tickAllRanges()

        if (queue.getQueueingPlayers().size < 2) return

        val queuePlayers = queue.getQueueingPlayers().toMutableList()
        for (firstQueueProfile in queuePlayers) {
            val firstPlayer = Bukkit.getPlayer(firstQueueProfile.uuid) ?: continue

            for (secondQueueProfile in queuePlayers) {
                if (firstQueueProfile.uuid == secondQueueProfile.uuid) continue

                val secondPlayer = Bukkit.getPlayer(secondQueueProfile.uuid) ?: continue

                if (!isValidMatch(firstQueueProfile, secondQueueProfile, firstPlayer, secondPlayer, queue.type)) continue

                val arena = ArenaManager.getFreeArena(queue.kit)

                if (arena == null) {
                    for (i in 0..6) {
                        firstPlayer.sendMessage(CC.translate("&cNo arenas found"))
                        secondPlayer.sendMessage(CC.translate("&cNo arenas found"))
                    }
                    return
                }

                queue.removePlayer(firstQueueProfile)
                queue.removePlayer(secondQueueProfile)

                val profile = PracticePlugin.instance.profileManager.findById(firstPlayer.uniqueId)
                val profile1 = PracticePlugin.instance.profileManager.findById(secondPlayer.uniqueId)

                val match =
                    MatchManager.createMatch(queue.kit, arena, queue.type, false, firstPlayer, secondPlayer) ?: return

                println(match)

                addFollowersAsSpectators(profile, firstPlayer, match)
                addFollowersAsSpectators(profile1, secondPlayer, match, firstPlayer.uniqueId, secondPlayer.uniqueId)
            }
        }

        return
    }

    private fun isValidMatch(firstQueueProfile: QueuePlayer, secondQueueProfile: QueuePlayer, firstPlayer: Player, secondPlayer: Player, queueType: QueueType): Boolean {
        return when {
            secondQueueProfile.pingFactor != 0 && PlayerUtil.getPing(firstPlayer) > secondQueueProfile.pingFactor -> false
            firstQueueProfile.pingFactor != 0 && PlayerUtil.getPing(secondPlayer) > firstQueueProfile.pingFactor -> false
            queueType == QueueType.RANKED && (!firstQueueProfile.isInRange(secondQueueProfile.elo) || !secondQueueProfile.isInRange(firstQueueProfile.elo)) -> false
            else -> true
        }
    }

    private fun addFollowersAsSpectators(profile: Profile?, player: Player, match: Match, vararg excludeUUIDs: UUID) {
        profile?.followers?.filterNot { it in excludeUUIDs }?.forEach { uuid ->
            val followerProfile = PracticePlugin.instance.profileManager.findById(uuid)!!
            followerProfile.silent = true
            match.addSpectator(followerProfile.player)
            followerProfile.player.teleport(player.location)
        }
    }
}
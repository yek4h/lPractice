package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.queue.Queue
import net.lyragames.practice.queue.QueuePlayer
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.match.Match
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object QueueManager {
    val queues: MutableMap<Pair<Kit, QueueType>, Queue> = mutableMapOf()
    private val playingCounts: MutableMap<Pair<Kit, QueueType>, Int> = mutableMapOf()

    fun load() {
        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            addQueue(kit, QueueType.UNRANKED)
            if (kit.ranked) {
                addQueue(kit, QueueType.RANKED)
            }
        }
        println("Queues loaded: ${queues.size}")
        startUpdateTask()
    }

    private fun addQueue(kit: Kit, queueType: QueueType) {
        queues[Pair(kit, queueType)] = Queue(kit, queueType)
    }

    fun addToQueue(player: Player, kit: Kit, queueType: QueueType) {
        val queue = findQueue(kit, queueType) ?: return

        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val queuePlayer = QueuePlayer(player.uniqueId, player.name ?: "", queue, 0).apply {
            elo = profile.getKitStatistic(queue.kit.name)?.elo ?: 0
        }

        profile.queuePlayer = queuePlayer
        profile.state = ProfileState.QUEUE
        queue.addPlayer(queuePlayer)
    }

    fun findQueue(kit: Kit, queueType: QueueType): Queue? {
        return queues[Pair(kit, queueType)]
    }

    fun getQueue(uuid: UUID): Queue? {
        return queues.values.firstOrNull { queue ->
            queue.getQueueingPlayers().any { it.uuid == uuid }
        }
    }

    fun getQueueByKit(kit: Kit): List<Queue> {
        return queues.values.filter { it.kit.name.equals(kit.name, ignoreCase = true) }
    }

    fun getTotalQueueingPlayers(): Int {
        return queues.values.sumOf { it.getPlayerCount() }
    }

    fun getPlayingCount(kit: Kit, queueType: QueueType): Int {
        return playingCounts.getOrDefault(Pair(kit, queueType), 0)
    }

    fun updatePlayingCount(match: Match, increment: Int) {
        val key = Pair(match.kit, if (match.ranked) QueueType.RANKED else QueueType.UNRANKED)
        val currentCount = getPlayingCount(match.kit, if (match.ranked) QueueType.RANKED else QueueType.UNRANKED)
        playingCounts[key] = currentCount + increment
    }

    private fun startUpdateTask() {
        object : BukkitRunnable() {
            override fun run() {
                updateQueues()
            }
        }.runTaskTimer(PracticePlugin.instance, 0L, 20L) // Actualiza cada segundo (20 ticks)
    }

    private fun updateQueues() {
        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            if (!queues.containsKey(Pair(kit, QueueType.UNRANKED))) {
                addQueue(kit, QueueType.UNRANKED)
            }
            if (kit.ranked && !queues.containsKey(Pair(kit, QueueType.RANKED))) {
                addQueue(kit, QueueType.RANKED)
            }
        }
    }
}
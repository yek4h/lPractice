package net.lyragames.practice.profile

import dev.yek4h.practice.util.scoreboard.Aether
import dev.yek4h.practice.util.scoreboard.board.Board
import dev.yek4h.practice.util.scoreboard.board.Board.Companion.getByPlayer
import dev.yek4h.practice.util.scoreboard.event.BoardCreateEvent
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.manager.FFAManager
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.match.Match
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.item.CustomItemStack
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture

object ProfileListener : Listener {

    private var xd: Boolean = false

    @EventHandler
    fun onAsyncLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.name == null) {
            return
        }

        CompletableFuture.runAsync {
            try {
                var profile = ProfileManager.findById(event.uniqueId)

                if (profile == null) {
                    profile = Profile(event.uniqueId).apply {
                        name = event.name
                        load()  // Load the profile from the database
                        save(true)  // Save the profile after loading
                    }
                    ProfileManager.profiles[profile.uuid] = profile
                } else {
                    profile.name = event.name
                    profile.save(true)
                }

                // Update the profile in the ProfileManager
                ProfileManager.profiles.putIfAbsent(profile.uuid, profile)

                // Allow the login
                event.allow()
            } catch (e: Exception) {
                event.apply {
                    loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                    kickMessage = CC.RED + "Failed to load your profile!"
                }
                e.printStackTrace()
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        event.joinMessage = null
        val hiMessage = PracticePlugin.instance.settingsFile.getStringList("WELCOME-MESSAGE")

        hiMessage.forEach {
            player.sendMessage(CC.translate(it))
        }
        PracticePlugin.instance.hologramManager.show(player)

        val deboy = PracticePlugin.instance.deboy

        deboy.createScoreboard(event.player);


        PlayerUtil.allowMovement(player)
        PlayerUtil.reset(player)

        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!.apply {
            state = ProfileState.LOBBY
        }

        Constants.SPAWN?.let { player.teleport(it) }
        Hotbar.giveHotbar(profile)

        val entityPlayer = (player as CraftPlayer).handle

        /*¡Bukkit.getOnlinePlayers().forEach {
            player.hidePlayer(it)
            it.hidePlayer(player)
        }*/

        FFAManager.ffaMatches.forEach { ffa ->
            ffa.droppedItems.map { PacketPlayOutEntityDestroy(it.entityId) }
                .forEach { entityPlayer.playerConnection.sendPacket(it) }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        event.quitMessage = null
        PracticePlugin.instance.hologramManager.hide(player)

        when (profile.state) {
            ProfileState.QUEUE -> {
                profile.queuePlayer?.let {
                    QueueManager.getQueue(player.uniqueId)?.queuePlayers?.remove(it)
                    profile.state = ProfileState.LOBBY
                    profile.queuePlayer = null
                }
            }
            ProfileState.MATCH -> {
                profile.match?.let { matchId ->
                    Match.getByUUID(matchId)?.getMatchPlayer(player.uniqueId)?.let { matchPlayer ->
                        Match.getByUUID(matchId)?.handleQuit(matchPlayer)
                        ProfileState.LOBBY
                    }
                }
            }
            ProfileState.SPECTATING -> {
                if (profile.spectatingMatch == null) {
                    return
                }

                Match.getSpectator(profile.spectatingMatch!!)?.removeSpectator(player)
                ProfileState.LOBBY
            }
            ProfileState.FFA -> {
                profile.ffa?.let { ffaId ->
                    FFAManager.getByUUID(ffaId).getFFAPlayer(player.uniqueId).let { ffaPlayer ->
                        FFAManager.getByUUID(ffaId).handleLeave(ffaPlayer, true)
                        ProfileState.LOBBY
                    }
                }
            }
            else -> {}
        }

        // Remover CustomItemStacks y perfiles de manera segura
        CustomItemStack.removeAllByPlayer(player.uniqueId)
    }
}

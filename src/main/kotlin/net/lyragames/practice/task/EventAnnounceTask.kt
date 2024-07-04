package net.lyragames.practice.task

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.EventType
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.TextBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 05/06/2024
 */

object EventAnnounceTask : BukkitRunnable() {

    init {
        this.runTaskTimer(PracticePlugin.instance, 20 * 4L, 20 * 4L)
    }

    override fun run() {
        val event = EventManager.event ?: return
        if (event.state != EventState.ANNOUNCING) return

        if (event.players.isEmpty()) {
            Bukkit.broadcastMessage("${CC.RED}Stopped event as no one joined!")
            event.players.forEach {
                PracticePlugin.instance.profileManager.findById(it.uuid)?.state = ProfileState.LOBBY
            }
            event.players.clear()
            EventManager.event = null
            return
        }

        if (event.requiredPlayers == event.players.size) {
            event.startRound()
            return
        }

        if (System.currentTimeMillis() - event.created >= (1000 * 60) * 5) {
            Bukkit.broadcastMessage("${CC.RED}Stopped event as no one joined!")
            event.players.forEach {
                PracticePlugin.instance.profileManager.findById(it.uuid)?.state = ProfileState.LOBBY
            }
            event.players.clear()
            EventManager.event = null
            return
        }

        val host = Bukkit.getPlayer(event.host)
        val fancyMessage = buildMessage(host, event.type)

        for (player in Bukkit.getOnlinePlayers()) {
            if (event.getPlayer(player.uniqueId) != null) continue
            player.spigot().sendMessage(fancyMessage)
        }
    }

    private fun buildMessage(host: Player, eventType: EventType): TextComponent {
        return TextBuilder()
            .setText("${CC.GREEN}${host.name}${CC.YELLOW} is hosting ${CC.GREEN}${eventType.eventName}${CC.YELLOW} event!")
            .then()
            .setText("${CC.GREEN} [Click to join]")
            .setCommand("/event join")
            .then()
            .build()
    }
}
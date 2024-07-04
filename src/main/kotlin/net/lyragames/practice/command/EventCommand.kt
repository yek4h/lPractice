package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.Locale
import net.lyragames.practice.event.EventState
import net.lyragames.practice.ui.events.EventHostMenu
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.utils.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class EventCommand {

    @Command(name = "", desc = "Event help command")
    fun help(@Sender sender: CommandSender) {
        sender.sendMessage("""
            ${CC.SECONDARY}Event Help
            ${CC.CHAT_BAR}
            ${CC.PRIMARY}/event join
            ${CC.PRIMARY}/event host
            ${CC.PRIMARY}/event start
            ${CC.CHAT_BAR}
        """.trimIndent())
    }

    @Command(name = "host", desc = "Host an event", aliases = ["hostevent"])
    @Require("practice.command.event.host")
    fun host(@Sender sender: CommandSender) {
        val player = sender as? Player ?: return
        EventHostMenu().openMenu(player)
    }

    @Command(name = "join", desc = "Join an event", aliases = ["joinevent"])
    @Require("practice.command.event.join")
    fun join(@Sender sender: CommandSender) {
        val player = sender as? Player ?: return
        val event = EventManager.event

        when {
            event == null -> player.sendMessage(Locale.NO_ACTIVE_EVENTS.getMessage())
            event.requiredPlayers == event.players.size -> player.sendMessage(Locale.EVENT_FULL.getMessage())
            event.getPlayer(player.uniqueId) != null -> player.sendMessage(Locale.ALREADY_IN_EVENT.getMessage())
            event.state != EventState.ANNOUNCING -> player.sendMessage(Locale.ALREADY_STARTED.getMessage())
            else -> event.addPlayer(player)
        }
    }

    @Command(name = "start", desc = "Force start an event", aliases = ["forcestart", "fs"])
    @Require("practice.command.event.forcestart")
    fun forcestart(@Sender sender: CommandSender) {
        val player = sender as? Player ?: return

        val event = EventManager.event
        if ((event?.players?.size ?: 0) < 2) {
            player.sendMessage(Locale.NOT_ENOUGH_PLAYER.getMessage())
        } else {
            event?.startRound()
        }
    }
}

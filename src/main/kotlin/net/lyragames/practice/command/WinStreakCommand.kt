package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.manager.DailyWinStreakManager
import org.bukkit.command.CommandSender


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class WinStreakCommand(private val manager: DailyWinStreakManager) {

    @Command(name = "", desc = "Sends the help command of winstreak command")
    fun help(@Sender sender: CommandSender) {
        val msg = """
           &bWinStreak Practice Help Command &7- (StarPractice @ 2024)
           &b/winstreak updateAll &f- Updates the winstreak
           &b/winstreak toggle &f- toggles the winstreak daily to on or off
           &b/winstreak status &f- Shows the status of the daily winstreak
        """.trimIndent()
    }

    @Command(name = "toggle", desc = "Toggle daily winstreaks on or off")
    fun toggle(@Sender sender: CommandSender) {
        manager.toggle()
        val state = if (manager.isEnabled()) "§aenabled" else "§cdisabled"
        sender.sendMessage("§bDaily winstreaks have been $state.")
    }

    @Command(name = "updateAll", desc = "Update all daily winstreaks")
    fun updateAll(@Sender sender: CommandSender) {
        manager.updateAllWinStreaks()
        sender.sendMessage("§aAll daily winstreaks have been updated.")
    }

    @Command(name = "status", desc = "Get the status of daily winstreaks")
    fun status(@Sender sender: CommandSender) {
        val state = if (manager.isEnabled()) "§aenabled" else "§cdisabled"
        sender.sendMessage("§bDaily winstreaks are currently $state.")
    }
}
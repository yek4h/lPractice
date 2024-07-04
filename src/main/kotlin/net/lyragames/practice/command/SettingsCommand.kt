package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.ui.SettingsMenu
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

class SettingsCommand {

    @Command(name = "", desc = "Open settings menu")
    fun settings(@Sender sender: CommandSender) {
        val player = sender as Player
        SettingsMenu().openMenu(player)
    }
}

package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.Locale
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
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

class SpawnCommand {

    @Command(name = "", desc = "Teleport to spawn")
    @Require("practice.spawn")
    fun spawn(@Sender sender: CommandSender) {
        val player = sender as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile?.state != ProfileState.LOBBY && profile.state != ProfileState.QUEUE) {
            player.sendMessage(Locale.CANT_DO_THIS.getMessage())
            return
        }

        Hotbar.giveHotbar(profile)
        player.teleport(Constants.SPAWN)
    }
}

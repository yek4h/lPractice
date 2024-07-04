package net.lyragames.practice.command.admin

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
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

class FollowCommand {

    @Command(name = "", desc = "Follow a target player")
    @Require("practice.command.follow")
    fun follow(@Sender sender: CommandSender, target: Player) {
        if (true) {
            sender.sendMessage("${CC.RED}This command is currently disabled!")
            return
        }

        val player = sender as? Player ?: return
        val targetProfile = PracticePlugin.instance.profileManager.findById(target.uniqueId)
        val playerProfile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (playerProfile?.state != ProfileState.LOBBY) return

        if (targetProfile?.followers?.contains(player.uniqueId) == true) {
            player.sendMessage("${CC.RED}You are already following ${target.name}.")
            return
        }

        targetProfile?.followers?.add(player.uniqueId)
        playerProfile?.following = true

        player.sendMessage("${CC.PRIMARY}Started following ${CC.SECONDARY}${target.name}")
    }
}

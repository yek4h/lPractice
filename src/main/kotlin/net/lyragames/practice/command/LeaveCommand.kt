package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.Locale
import net.lyragames.practice.manager.FFAManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
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

class LeaveCommand {

    @Command(name = "leave", desc = "Leave the FFA", aliases = ["leaveffa"])
    fun leave(@Sender sender: CommandSender) {
        val player = sender as? Player ?: return
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state != ProfileState.FFA) {
            player.sendMessage(Locale.NOT_IN_FFA.getMessage())
            return
        }

        val ffa = FFAManager.getByUUID(profile.ffa!!)
        ffa.handleLeave(ffa.getFFAPlayer(player.uniqueId), false)

        player.sendMessage(Locale.LEFT_FFA.getMessage())
    }
}

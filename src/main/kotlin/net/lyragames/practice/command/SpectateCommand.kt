package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.Locale
import net.lyragames.practice.match.Match
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

class SpectateCommand {

    @Command(name = "", desc = "Spectate a player in a match")
    fun spectate(@Sender sender: CommandSender, target: Player) {
        val player = sender as Player
        val targetProfile = PracticePlugin.instance.profileManager.findById(target.uniqueId)
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (targetProfile?.state != ProfileState.MATCH) {
            player.sendMessage(Locale.NOT_IN_A_MATCH.getMessage())
            return
        }

        if (!targetProfile.settings.spectators && !player.hasPermission("lpractice.bypass.spectate")) {
            player.sendMessage(Locale.SPECTATING_DISABLED.getMessage())
            return
        }

        val match = Match.getByUUID(targetProfile.match!!)

        if (match == null) {
            player.sendMessage(Locale.NOT_IN_A_MATCH.getMessage())
            return
        }

        if (profile?.state != ProfileState.LOBBY) return

        match.addSpectator(player)
        player.teleport(target)
    }
}

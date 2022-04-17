package net.lyragames.practice.command.admin

import me.vaperion.blade.command.annotation.Command
import me.vaperion.blade.command.annotation.Sender
import net.lyragames.llib.utils.CC
import net.lyragames.practice.profile.Profile
import org.bukkit.entity.Player


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/26/2022
 * Project: lPractice
 */

object FollowCommand {

    @Command(value = ["follow"], description = "follow a player")
    fun follow(@Sender player: Player, target: Player) {
        val profile = Profile.getByUUID(target.uniqueId)
        val profile1 = Profile.getByUUID(player.uniqueId)

        profile?.followers?.add(player.uniqueId)
        profile1?.following = true

        player.sendMessage("${CC.PRIMARY}Started following ${CC.SECONDARY}${target.name}")
    }
}
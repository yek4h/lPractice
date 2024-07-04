package net.lyragames.practice.command.admin

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.LocationUtil
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

class SetSpawnCommand {

    @Command(name = "", desc = "Set the spawn location")
    @Require("practice.admin")
    fun setSpawn(@Sender sender: CommandSender) {
        val player = sender as? Player ?: return
        Constants.SPAWN = player.location
        PracticePlugin.instance.settingsFile.config.set("SPAWN", LocationUtil.serialize(player.location))
        PracticePlugin.instance.settingsFile.save()

        player.sendMessage("${CC.GREEN}Successfully set spawn!")
    }
}

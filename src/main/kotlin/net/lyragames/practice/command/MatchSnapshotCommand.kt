package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.Locale
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.ui.match.MatchDetailsMenu
import net.lyragames.practice.match.snapshot.MatchSnapshot
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class MatchSnapshotCommand {

    @Command(name = "", desc = "View match snapshot details")
    fun command(@Sender sender: CommandSender, id: String) {
        val player = sender as Player

        val cachedInventory: MatchSnapshot? = try {
            MatchSnapshot.getByUuid(UUID.fromString(id))
        } catch (e: Exception) {
            MatchSnapshot.getByName(id)
        }

        if (cachedInventory == null) {
            player.sendMessage(Locale.COULDNT_FIND_INVENTORY.getMessage())
            return
        }

        Bukkit.getScheduler().runTask(PracticePlugin.instance) {
            MatchDetailsMenu(cachedInventory).openMenu(player)
        }
    }
}

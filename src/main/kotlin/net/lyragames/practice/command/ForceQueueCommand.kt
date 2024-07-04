package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 21/06/2024
*/

class ForceQueueCommand(
) {

    @Command(name = "", desc = "")
    @Require("practice.command.forcequeue")
    fun queue(@Sender sender: CommandSender, player: Player, kit: Kit?, queue: QueueType?) {

        if (!player.isOnline) {
            sender.sendMessage(CC.translate("&cPlayer not found!"))
            return
        }

        if (kit == null) {
            sender.sendMessage(CC.translate("&cKit not found!"))
            return
        }

        if (queue == null) {
            sender.sendMessage(CC.translate("&cThat queue wasn't found, use Unranked or Ranked"))
            return
        }

        QueueManager.addToQueue(player, kit, queue)
    }
}
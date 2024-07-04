package net.lyragames.practice.command.admin

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.rating.menu.ArenaRatingMenu
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

class ArenaRatingCommand {

    @Command(name = "", desc = "Opens the arena ratings menu")
    @Require("practice.command.arenaratings")
    fun ratings(@Sender sender: CommandSender, arena: Arena) {
        val player = sender as Player
        ArenaRatingMenu(arena).openMenu(player)
    }
}

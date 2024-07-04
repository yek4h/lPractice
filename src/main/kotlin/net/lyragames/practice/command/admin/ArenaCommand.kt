package net.lyragames.practice.command.admin

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.impl.StandaloneArena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.ui.arena.ArenaManageMenu
import net.lyragames.practice.utils.Cuboid
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import rip.katz.api.utils.CC


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 16/06/2024
*/

class ArenaCommand {

    @Command(name = "", desc = "")
    @Require("practice.arena.manage")
    fun help(@Sender sender: CommandSender) {
        val player = sender as Player
        val commands = listOf(
            "&bArena Commands:",
            "&7&m---------------------",
            "&b/arena create <name> <type>",
            "&b/arena type <name> <type> &7- Sumo, MLGRush, BedFight, Bridge, Build and Normal",
            "&b/arena delete <arena>",
            "&b/arena pos1 <arena>",
            "&b/arena pos2 <arena>",
            "&b/arena min <arena>",
            "&b/arena max <arena>",
            "&b/arena deadzone <arena> <deadzone> &7- set an arena's lowest Y location (Used for bridges, bedfight, etc)",
            "&b/arena bed1 <arena> &7- only supported for mlgrush (stand on bed)",
            "&b/arena bed2 <arena> &7- only supported for mlgrush (stand on bed)",
            "&7&m---------------------",
            "&bBedWars Arena Command:",
            "&7&m---------------------",
            "&b/arena redspawn <arena>",
            "&b/arena bluespawn <arena>",
            "&b/arena redbed <arena>",
            "&b/arena bluebed <arena>",
            "&7&m---------------------",
            "&bBridge Arena Command:",
            "&7&m---------------------",
            "&b/arena redspawn <arena>",
            "&b/arena bluespawn <arena>",
            "&b/arena redportal1 <arena>",
            "&b/arena redportal2 <arena>",
            "&b/arena blueportal1 <arena>",
            "&b/arena blueportal2 <arena>",
            "&7&m---------------------"
        )

        commands.forEach { command ->
            player.sendMessage(CC.color(command))
        }
    }

    @Command(name  = "create", desc = "")
    @Require("practice.arena.create")
    fun createArena(@Sender sender: CommandSender, name: String, type: ArenaType) {
        val player = sender as Player

        if (Arena.getByName(name) != null) {
            player.sendMessage(CC.color("&cThat arena already exists!"))
            return
        }
        val arena = when (type) {
            ArenaType.SHARED -> Arena(name)
            else -> {
                StandaloneArena(name)
            }
        }

        arena.save()
        Arena.arenas.add(arena)

        player.sendMessage(CC.color("&aSuccessfully created &b$name &aarena with &b${type.name} type!"))
    }

    @Command(name = "delete", desc = "")
    @Require("practice.arena.delete")
    fun deleteArena(@Sender sender: CommandSender, arena: Arena) {
        val player = sender as Player
        arena.delete()
        Arena.arenas.remove(arena)

        player.sendMessage(CC.color("&aSuccessfully deleted &b${arena.name}&a!"))
    }

    @Command(name = "pos1", desc = "", aliases = ["a", "position1", "location1", "p1", "l1"])
    @Require("practice.arena.positions")
    fun positionA(@Sender sender: CommandSender, arena: Arena) {
        val player = sender as Player
        arena.l1 = player.location
        arena.save()

        player.sendMessage(CC.color("&aSuccessfully set &b${arena.name}&b's position A!"))
    }

    @Command(name = "pos2", desc = "", aliases = ["b", "position2", "location2", "p2", "l2"])
    @Require("practice.arena.positions")
    fun positionB(@Sender sender: CommandSender, arena: Arena) {
        val player = sender as Player
        arena.l2 = player.location
        arena.save()

        player.sendMessage(CC.color("&aSuccessfully set &b${arena.name}'s position B!"))
    }

    @Command(name = "min", desc = "", aliases = ["minimum"])
    @Require("practice.arena.positions")
    fun arenaMinCorner(@Sender sender: CommandSender, arena: Arena) {
        val player = sender as Player
        arena.min = player.location
        arena.save()

        player.sendMessage("&aSuccessfully set &b${arena.name}'s min location!")
    }

    @Command(name = "max", desc = "", aliases = ["maximum"])
    @Require("practice.arena.positions")
    fun arenaMaxCorner(@Sender sender: CommandSender, arena: Arena) {
        val player = sender as Player
        arena.max = player.location
        arena.save()
    }

    @Command(name = "deadzone", desc = "", aliases = ["yval"])
    @Require("practice.arena.positions")
    fun arenaDeadzone(@Sender sender: CommandSender, arena: Arena, deadzone: Int) {
        val player = sender as Player
        arena.deadzone = deadzone
        arena.save()

        player.sendMessage(CC.color("&aSuccessfully set &b${arena.name}&a's deadzone to &b$deadzone&a!"))
    }

    @Command(name = "type", desc = "")
    @Require("practice.arena.type")
    fun arenaType(@Sender sender: CommandSender, arena: Arena, arenaType: ArenaType) {
        val player = sender as Player
        if (arenaType == ArenaType.STANDALONE) {
            val newArena = StandaloneArena(arena.name)
            newArena.l1 = arena.l1
            newArena.l2 = arena.l2
            newArena.deadzone = arena.deadzone
            newArena.min = arena.min
            newArena.max = arena.max
            newArena.arenaType = arenaType

            if (arena is StandaloneArena) {
                for (duplicate in arena.duplicates) {
                    val newDuplicate = StandaloneArena(duplicate.name)
                    newDuplicate.l1 = duplicate.l1
                    newDuplicate.l2 = duplicate.l2
                    newDuplicate.min = duplicate.min
                    newDuplicate.max = duplicate.max
                    newDuplicate.deadzone = duplicate.deadzone
                    newDuplicate.duplicate = true

                    newDuplicate.bounds = Cuboid(newDuplicate.min!!, newDuplicate.max!!)
                    newDuplicate.arenaType = ArenaType.STANDALONE

                    arena.duplicates.removeIf { it.name.equals(duplicate.name, false) }
                    arena.duplicates.add(newDuplicate)
                }
            }

            Arena.arenas.removeIf { it.name.equals(arena.name, false) }
            Arena.arenas.add(newArena)

            newArena.save()
        } else {
            arena.arenaType = arenaType
            arena.save()
        }

        player.sendMessage(CC.color("&aSuccessfully set &b${arena.name}&a's type to &b${arenaType.name}&a!"))
    }

    @Command(name = "menu", desc = "")
    @Require("practice.arena.manage")
    fun arenaManagement(@Sender sender: CommandSender, arena: Arena) {
        ArenaManageMenu(arena).openMenu(sender as Player)
    }
}

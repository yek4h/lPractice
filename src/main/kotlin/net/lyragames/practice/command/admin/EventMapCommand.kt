package net.lyragames.practice.command.admin

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.map.impl.TNTRunMap
import net.lyragames.practice.event.map.impl.TNTTagMap
import net.lyragames.practice.event.map.type.EventMapType
import net.lyragames.practice.manager.EventMapManager
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

class EventMapCommand {

    @Command(name = "", desc = "Manage event maps")
    @Require("practice.command.eventmap")
    fun help(@Sender sender: CommandSender) {
        sender.sendMessage("""
            ${CC.PRIMARY}EventMap Commands:
            ${CC.SECONDARY}/eventmap create <name>
            ${CC.SECONDARY}/eventmap delete <name>
            ${CC.SECONDARY}/eventmap spawn <name>
            ${CC.SECONDARY}/eventmap pos1 <name>
            ${CC.SECONDARY}/eventmap pos2 <name>
            ${CC.SECONDARY}/eventmap deadzone <name> <deadzone>
            ${CC.SECONDARY}/eventmap type <name> <type> - you can choose from Sumo & Brackets
        """.trimIndent())
    }

    @Command(name = "create", desc = "Create an event map")
    @Require("practice.command.eventmap.create")
    fun create(@Sender sender: CommandSender, name: String) {
        if (EventMapManager.getByName(name) != null) {
            sender.sendMessage("${CC.RED}That event map already exists!")
            return
        }

        val arena = EventMap(name)
        arena.save()
        EventMapManager.maps.add(arena)
        sender.sendMessage("${CC.PRIMARY}Successfully created ${CC.SECONDARY}'$name'!")
    }

    @Command(name = "delete", desc = "Delete an event map")
    @Require("practice.command.eventmap.delete")
    fun delete(@Sender sender: CommandSender, arena: EventMap) {
        arena.delete()
        EventMapManager.maps.remove(arena)
        sender.sendMessage("${CC.PRIMARY}Successfully deleted ${CC.SECONDARY}'${arena.name}'!")
    }

    @Command(name = "spawn", desc = "Set the spawn location of an event map")
    @Require("practice.command.eventmap.spawn")
    fun setSpawn(@Sender sender: CommandSender, arena: EventMap) {
        val player = sender as? Player ?: return
        arena.spawn = player.location
        arena.save()
        sender.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${arena.name}${CC.PRIMARY} spawn point!")
    }

    @Command(name = "deadzone", desc = "Set the deadzone of a TNT Run map")
    @Require("practice.command.eventmap.deadzone")
    fun setDeadzone(@Sender sender: CommandSender, arena: EventMap, deadzone: Int) {
        if (arena.type != EventMapType.TNT_RUN) {
            sender.sendMessage("${CC.RED}That option is not supported for this map type!")
            return
        }
        (arena as TNTRunMap).deadzone = deadzone
        arena.save()
        sender.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${arena.name}${CC.PRIMARY} deadzone!")
    }

    @Command(name = "pos1", desc = "Set the first position of an event map", aliases = ["position1", "l1", "location1"])
    @Require("practice.command.eventmap.pos1")
    fun setPos1(@Sender sender: CommandSender, arena: EventMap) {
        val player = sender as? Player ?: return
        if (arena.type in listOf(EventMapType.TNT_TAG, EventMapType.TNT_RUN)) {
            sender.sendMessage("${CC.RED}That option is not supported for this map type!")
            return
        }
        arena.l1 = player.location
        arena.save()
        sender.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${arena.name}${CC.PRIMARY} location 1!")
    }

    @Command(name = "pos2", desc = "Set the second position of an event map", aliases = ["position2", "l2", "location2"])
    @Require("practice.command.eventmap.pos2")
    fun setPos2(@Sender sender: CommandSender, arena: EventMap) {
        val player = sender as? Player ?: return
        if (arena.type in listOf(EventMapType.TNT_TAG, EventMapType.TNT_RUN)) {
            sender.sendMessage("${CC.RED}That option is not supported for this map type!")
            return
        }
        arena.l2 = player.location
        arena.save()
        sender.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${arena.name}${CC.PRIMARY} location 2!")
    }

    @Command(name = "type", desc = "Set the type of an event map")
    @Require("practice.command.eventmap.type")
    fun setType(@Sender sender: CommandSender, arena: EventMap, type: EventMapType) {
        arena.type = type
        when (type) {
            EventMapType.TNT_RUN -> {
                val newArena = TNTRunMap(arena.name).apply {
                    spawn = arena.spawn
                }
                EventMapManager.maps.replace(arena.name, newArena)
                newArena.save()
            }
            EventMapType.TNT_TAG -> {
                val newArena = TNTTagMap(arena.name).apply {
                    spawn = arena.spawn
                }
                EventMapManager.maps.replace(arena.name, newArena)
                newArena.save()
            }
            else -> arena.save()
        }
        sender.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${arena.name}${CC.PRIMARY}'s type to ${CC.SECONDARY}${type.eventName}${CC.PRIMARY}!")
    }
}

// Extension function to replace an item in a mutable list by name
private fun MutableList<EventMap>.replace(name: String, newArena: EventMap) {
    val index = indexOfFirst { it.name.equals(name, ignoreCase = true) }
    if (index != -1) {
        this[index] = newArena
    }
}

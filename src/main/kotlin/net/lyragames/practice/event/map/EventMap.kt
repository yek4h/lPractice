package net.lyragames.practice.event.map

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.map.type.EventMapType
import net.lyragames.practice.utils.LocationUtil
import org.bukkit.Location


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/13/2022
 * Project: lPractice
 */

open class EventMap(val name: String) {

    open var l1: Location? = null
    var l2: Location? = null
    var spawn: Location? = null

    open var type = EventMapType.SUMO

    companion object {
        val maps: MutableList<EventMap> = mutableListOf()

        fun getByName(name: String): EventMap? {
            return maps.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }

    open fun save() {
        val configFile = PracticePlugin.instance.eventsFile
        val section = configFile.createSection("maps.${name}")

        section.set("l1", if (l1 == null) "null" else LocationUtil.serialize(l1))
        section.set("l2", if (l2 == null) "null" else LocationUtil.serialize(l2))
        section.set("spawn", if (spawn == null) "null" else LocationUtil.serialize(spawn))
        section.set("type", type.name)

        configFile.save()
    }

    fun delete() {
        val configFile = PracticePlugin.instance.eventsFile

        configFile.config.set("maps.$name", null)

        configFile.save()
    }
}
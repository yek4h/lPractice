package net.lyragames.practice.arena

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.Cuboid
import net.lyragames.practice.utils.LocationUtil
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

/**
 * This Project is property of Zowpy © 2021
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 12/19/2021
 * Project: Practice
 */

open class Arena(val name: String) {
    var l1: Location? = null
    var l2: Location? = null
    var min: Location? = null
    var max: Location? = null
    open var arenaType = ArenaType.SHARED
    var deadzone = 0
    var free = true
    var duplicate = false
    lateinit var bounds: Cuboid
    val duplicates: MutableList<Arena> = mutableListOf()

    open val isSetup: Boolean
        get() = l1 != null && l2 != null && min != null && max != null

    open fun save() {
        val configFile = PracticePlugin.instance.arenasFile
        val configSection = configFile.createSection("arenas.$name")

        configSection.set("l1", LocationUtil.serialize(l1))
        configSection.set("l2", LocationUtil.serialize(l2))
        configSection.set("min", LocationUtil.serialize(min))
        configSection.set("max", LocationUtil.serialize(max))
        configSection.set("deadzone", deadzone)
        configSection.set("type", arenaType.name)

        if (duplicates.isNotEmpty()) {
            duplicates.forEachIndexed { index, duplicateArena ->
                val duplicateSection = configSection.createSection("duplicates.${index + 1}")
                duplicateSection.set("l1", LocationUtil.serialize(duplicateArena.l1))
                duplicateSection.set("l2", LocationUtil.serialize(duplicateArena.l2))
                duplicateSection.set("min", LocationUtil.serialize(duplicateArena.min))
                duplicateSection.set("max", LocationUtil.serialize(duplicateArena.max))
                duplicateSection.set("deadzone", duplicateArena.deadzone)
            }
        }

        configFile.save()
    }

    open fun delete() {
        val configFile = PracticePlugin.instance.arenasFile
        configFile.config.set("arenas.$name", null)
        configFile.save()
    }

    open fun isFree(): Boolean = free

    open fun isCompatible(kit: Kit): Boolean = true

    fun createDuplicate(name: String, section: ConfigurationSection?): Arena {
        return Arena(name).apply {
            l1 = LocationUtil.deserialize(section!!.getString("l1"))
            l2 = LocationUtil.deserialize(section.getString("l2"))
            min = LocationUtil.deserialize(section.getString("min"))
            max = LocationUtil.deserialize(section.getString("max"))
            bounds = Cuboid(min!!, max!!)
            deadzone = section.getInt("deadzone")
            duplicate = true
        }
    }

    companion object {
        @JvmStatic
        val arenas: MutableList<Arena> = mutableListOf()

        @JvmStatic
        fun getByName(name: String): Arena? {
            return arenas.firstOrNull { it.name.equals(name, true) }
        }
    }
}
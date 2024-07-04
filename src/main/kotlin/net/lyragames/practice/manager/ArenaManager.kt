package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.impl.StandaloneArena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.Cuboid
import net.lyragames.practice.utils.LocationUtil

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/15/2022
 * Project: lPractice
 *
 * Optimized by yek4h
 *
 */

object ArenaManager {

    fun load() {
        val configFile = PracticePlugin.instance.arenasFile

        configFile.getConfigurationSection("arenas")?.getKeys(false)?.forEach { key ->
            val section = configFile.getConfigurationSection("arenas.$key")
            val arenaType = ArenaType.valueOf(section!!.getString("type")!!.uppercase())

            val arena = if (arenaType == ArenaType.STANDALONE) {
                StandaloneArena(key)
            } else {
                Arena(key)
            }

            arena.apply {
                this.arenaType = arenaType
                deadzone = section.getInt("deadzone")
                l1 = LocationUtil.deserialize(section.getString("l1"))
                l2 = LocationUtil.deserialize(section.getString("l2"))
                min = LocationUtil.deserialize(section.getString("min"))
                max = LocationUtil.deserialize(section.getString("max"))
                bounds = Cuboid(min!!, max!!)
            }

            section.getConfigurationSection("duplicates")?.getKeys(false)?.forEach { duplicateKey ->
                val duplicateSection = section.getConfigurationSection("duplicates.$duplicateKey")
                val duplicateArena = arena.createDuplicate("$key$duplicateKey", duplicateSection)
                arena.duplicates.add(duplicateArena)
            }

            Arena.arenas.add(arena)
        }
    }

    fun getFreeArena(kit: Kit): Arena? {
        val eligibleArenas = Arena.arenas.filter { it.isSetup && it.isFree() && it.isCompatible(kit) }

        if (eligibleArenas.isEmpty()) {
            println("No eligible arenas found")
            return null
        }

        val weightedArenas = eligibleArenas.map { it to (ArenaRatingManager.getAverageRating(it) + 1) }
        val totalWeight = weightedArenas.sumByDouble { it.second }
        val randomValue = Math.random() * totalWeight

        var cumulativeWeight = 0.0
        for ((arena, weight) in weightedArenas) {
            cumulativeWeight += weight
            if (randomValue <= cumulativeWeight) {
                println("Selected arena: ${arena.name} with weight $weight")
                return arena
            }
        }

        println("Failed to select an arena")
        return null
    }
}
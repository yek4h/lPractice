package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class DailyWinStreakManager(private val plugin: JavaPlugin) {

    private val startTimes: MutableMap<UUID, Long> = ConcurrentHashMap()
    private var task: BukkitTask? = null
    private var enabled: Boolean = true

    fun toggle() {
        if (enabled) {
            disable()
        } else {
            enable()
        }
    }

    private fun enable() {
        if (enabled) return
        enabled = true
        task = object : BukkitRunnable() {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                PracticePlugin.instance.profileManager.profiles.keys.forEach { playerId ->
                    val profile = PracticePlugin.instance.profileManager.findById(playerId)!!
                    val startTime = startTimes[playerId] ?: return@forEach
                    if (currentTime - startTime >= 24 * 60 * 60 * 1000) {
                        profile.globalStatistic.dailyWinStreak = 0
                        for (kits in PracticePlugin.instance.kitManager.kits.values) {

                            profile.getKitStatistic(kits.name)!!.bestDailyStreak = 0
                            profile.getKitStatistic(kits.name)!!.rankedDailyStreak = 0
                            profile.getKitStatistic(kits.name)!!.currentDailyStreak = 0
                        }
                        startTimes[playerId] = currentTime
                        profile.save(true)
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20 * 60 * 30) // Run every 30 minutes
    }

    private fun disable() {
        if (!enabled) return
        enabled = false
        task?.cancel()
        task = null
    }

    fun updateWinStreak(playerId: UUID, won: Boolean) {
        if (!enabled) return
        val profile = PracticePlugin.instance.profileManager.findById(playerId)!!
        if (won) {
            profile.globalStatistic.dailyWinStreak += 1
            startTimes[playerId] = System.currentTimeMillis()
        } else {
            profile.globalStatistic.dailyWinStreak = 0
        }
        profile.save(true)
    }

    fun updateAllWinStreaks() {
        val currentTime = System.currentTimeMillis()
        PracticePlugin.instance.profileManager.profiles.keys.forEach { playerId ->
            val profile = PracticePlugin.instance.profileManager.findById(playerId)!!
            val startTime = startTimes[playerId] ?: return@forEach
            if (currentTime - startTime >= 24 * 60 * 60 * 1000) {
                profile.globalStatistic.dailyWinStreak = 0
                startTimes[playerId] = currentTime
                profile.save(true)
            }
        }
    }

    fun getWinStreak(playerId: UUID): Int {
        return PracticePlugin.instance.profileManager.findById(playerId)!!.globalStatistic.dailyWinStreak
    }

    fun getStartTime(playerId: UUID): Long {
        return startTimes[playerId] ?: 0
    }

    fun isEnabled(): Boolean {
        return enabled
    }
}
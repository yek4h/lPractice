package net.lyragames.practice.utils.scoreboard

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 03/07/2024
*/

class Deboy(val plugin: JavaPlugin, val adapter: ScoreInterface) {
    private val handlers: MutableSet<ScoreboardHandler> = mutableSetOf()

    init {
        startUpdating()
    }

    private fun startUpdating() {
        object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    handlers.find { it.player == player }?.update()
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L)
    }

    fun registerHandler(handler: ScoreboardHandler) {
        handlers.add(handler)
    }

    fun unregisterHandler(handler: ScoreboardHandler) {
        handlers.remove(handler)
    }

    fun createScoreboard(player: Player) {
        ScoreboardHandler(player, this)
    }
}
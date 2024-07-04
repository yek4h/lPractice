package net.lyragames.practice.utils.scoreboard

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 03/07/2024
*/

class ScoreboardHandler(val player: Player, private val deboy: Deboy) {
    private val scoreboard: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private val objective: Objective = scoreboard.registerNewObjective("deboy", "dummy")

    private val entries: MutableMap<Int, String> = mutableMapOf()
    private val cooldowns: MutableSet<ScoreCooldown> = mutableSetOf()

    init {
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = ChatColor.translateAlternateColorCodes('&', deboy.adapter.getTitle(player))
        player.scoreboard = scoreboard
        deboy.registerHandler(this)
    }

    fun update() {
        val lines = deboy.adapter.getLines(player)
        entries.clear()

        lines.forEachIndexed { index, line ->
            val formattedLine = ChatColor.translateAlternateColorCodes('&', line)
            entries[15 - index] = formattedLine
            val teamName = "line$index"
            val team = scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)

            val entry = getEntry(index)

            team.addEntry(entry)
            team.prefix = formattedLine.take(16)
            team.suffix = formattedLine.drop(16).take(30)

            objective.getScore(entry).score = 15 - index
        }
    }

    private fun getEntry(index: Int): String {
        return ChatColor.values()[index % ChatColor.values().size].toString() + ChatColor.RESET
    }

    fun addCooldown(cooldown: ScoreCooldown) {
        cooldowns.add(cooldown)
    }

    fun removeCooldown(cooldown: ScoreCooldown) {
        cooldowns.remove(cooldown)
    }

    fun getCooldowns(): Set<ScoreCooldown> {
        return cooldowns.filterNot { it.isExpired() }.toSet()
    }
}
package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.leaderboards.Leaderboards
import net.lyragames.practice.ui.leaderboards.LeaderboardRankedMenu
import net.lyragames.practice.utils.LocationUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import rip.katz.api.Katto


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 23/06/2024
*/

class LeaderboardCommand {

    @Command(name = "", desc = "")
    @Require("practice.command.leaderboards")
    fun leaderboard(@Sender sender: CommandSender) {
        sender as Player

        LeaderboardRankedMenu(PracticePlugin.instance).openMenu(sender)
    }


    @Command(name = "setup", desc = "")
    @Require("practice.command.setup.leaderboards")
    fun leaderboardLocation(@Sender sender: CommandSender) {
        val player = sender as Player
        PracticePlugin.instance.settingsFile.config.set("HOLOGRAMS.GLOBAL-LEADERBOARDS.LOCATION", LocationUtil.r(player.location))
        Katto.get().hologramManager.hologramDestroy("globalLeaderboards")

        val location = LocationUtil.u(PracticePlugin.instance.settingsFile.getString("HOLOGRAMS.GLOBAL-LEADERBOARDS.LOCATION"))
        val updateTime = PracticePlugin.instance.settingsFile.getInt("HOLOGRAMS.GLOBAL-LEADERBOARDS.UPDATABLE-TIME")
        val isUpdatable = PracticePlugin.instance.settingsFile.getBoolean("HOLOGRAMS.GLOBAL-LEADERBOARDS.UPDATABLE")
        val lbLines = PracticePlugin.instance.leaderboards.getTopProfilesByRankedKitsLines()
        val lines = PracticePlugin.instance.settingsFile.getStringList("HOLOGRAMS.GLOBAL-LEADERBOARDS.LINES").map { line ->
            line.replace("<lines>", lbLines.joinToString("\n")
                .replace("<updating>", ""))
        }.toMutableList()

        Katto.get().hologramManager.hologramCreation(
            Location(Bukkit.getWorld("world"), 725.5, 39.0, -1549.5),
            updateTime,
            "globalLeaderboards",
            lines.toMutableList(),
            isUpdatable,
            45.0,
            true
        )
    }
}
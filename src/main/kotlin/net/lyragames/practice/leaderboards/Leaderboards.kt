package net.lyragames.practice.leaderboards

import com.mongodb.client.MongoCursor
import dev.ryu.core.bukkit.CoreAPI
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.utils.ConfigFile
import net.lyragames.practice.utils.LocationUtil
import org.bson.Document
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * Author: yek4h © 2024
 * Date: 22/06/2024
 */

class Leaderboards(
    private val configFile: ConfigFile,
    private val plugin: PracticePlugin
) {

    private var currentKitIndex = 0
    private val kits: List<Kit> = plugin.kitManager.kits.values.filter { it.ranked }

    init {
        val globalLocation = LocationUtil.u(configFile.getString("HOLOGRAMS.GLOBAL-LEADERBOARDS.LOCATION"))
        val globalUpdateTime = configFile.getInt("HOLOGRAMS.GLOBAL-LEADERBOARDS.UPDATABLE-TIME")
        val globalRefreshTime = configFile.getInt("HOLOGRAMS.GLOBAL-LEADERBOARDS.REFRESH-TIME")
        val isGlobalUpdatable = configFile.getBoolean("HOLOGRAMS.GLOBAL-LEADERBOARDS.UPDATABLE")

        val kitLocation = LocationUtil.u(configFile.getString("HOLOGRAMS.KIT-ELO-LEADERBOARDS.LOCATION"))
        val kitUpdateTime = configFile.getInt("HOLOGRAMS.KIT-ELO-LEADERBOARDS.UPDATABLE-TIME")
        val kitRefreshTime = configFile.getInt("HOLOGRAMS.KIT-ELO-LEADERBOARDS.REFRESH-TIME")
        val isRankedKitsUpdatable = configFile.getBoolean("HOLOGRAMS.KIT-ELO-LEADERBOARDS.UPDATABLE")

        val streakLocation = LocationUtil.u(configFile.getString("HOLOGRAMS.WIN-STREAK-LEADERBOARDS.LOCATION"))
        val streakUpdateTime = configFile.getInt("HOLOGRAMS.WIN-STREAK-LEADERBOARDS.UPDATABLE-TIME")
        val streakRefreshTime = configFile.getInt("HOLOGRAMS.WIN-STREAK-LEADERBOARDS.REFRESH-TIME")
        val isStreakUpdatable = configFile.getBoolean("HOLOGRAMS.WIN-STREAK-LEADERBOARDS.UPDATABLE")

        updateGlobalHologram(globalLocation, globalUpdateTime, globalRefreshTime, isGlobalUpdatable)

        object : BukkitRunnable() {
            override fun run() {
                updateGlobalHologram(globalLocation, globalUpdateTime, globalRefreshTime, isGlobalUpdatable)
                if (kits.isEmpty()) return
                currentKitIndex = (currentKitIndex + 1) % kits.size
            }
        }.runTaskTimer(plugin, 0L, (globalUpdateTime * 20L))

        object : BukkitRunnable() {
            override fun run() {
                updateKitEloHolograms(kitLocation, kitUpdateTime, kitRefreshTime, isRankedKitsUpdatable)
                currentKitIndex = (currentKitIndex + 1) % kits.size
            }
        }.runTaskTimer(plugin, 0L, (kitUpdateTime * 20L))

        updateStreakHologram(streakLocation, streakUpdateTime, streakRefreshTime, isStreakUpdatable)
    }

    private fun updateGlobalHologram(location: Location, updateTime: Int, refreshTime: Int, isUpdatable: Boolean) {
        val leaderboardLines = getTopProfilesByGlobalElo()
            .take(10)
            .mapIndexed { index, (profile, elo) ->
                configFile.getString("HOLOGRAMS.GLOBAL-LEADERBOARDS.FORMAT")
                    .replace("<top>", (index + 1).toString())
                    .replace("<name>", "${ChatColor.valueOf(CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(profile.uuid)).color)}${profile.name}")
                    .replace("<elo>", elo.toString())
            }

        val formattedLines = configFile.getStringList("HOLOGRAMS.GLOBAL-LEADERBOARDS.LINES").toMutableList()

        if (formattedLines.contains("<lines>")) {
            formattedLines.remove("<lines>")
            formattedLines.addAll(leaderboardLines)
        }

        plugin.hologramManager.hologramCreation(
            location,
            updateTime,
            refreshTime,
            "globalLeaderboards",
            formattedLines,
            isUpdatable,
            80.0,
            false
        ) {}
    }

    private fun updateStreakHologram(location: Location, updateTime: Int, refreshTime: Int, isUpdatable: Boolean) {
        val leaderboardLines = getTopProfilesByDailyWinStreakLines().sortedByDescending { it.second }
            .take(10)
            .mapIndexed { index, (profile, dailyWinStreak) ->
                configFile.getString("HOLOGRAMS.WIN-STREAK-LEADERBOARDS.FORMAT")
                    .replace("<top>", (index + 1).toString())
                    .replace("<name>", "${ChatColor.valueOf(CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(profile.uuid)).color)}${profile.name}")
                    .replace("<streak>", dailyWinStreak.toString())
            }
        val formattedLines = configFile.getStringList("HOLOGRAMS.WIN-STREAK-LEADERBOARDS.LINES").toMutableList()

        if (formattedLines.contains("<lines>")) {
            formattedLines.remove("<lines>")
            formattedLines.addAll(leaderboardLines)
        }

        plugin.hologramManager.hologramCreation(
            location,
            updateTime,
            refreshTime,
            "winStreakLeaderboards",
            formattedLines,
            isUpdatable,
            80.0,
            false
        ) {}
    }

    private fun updateKitEloHolograms(location: Location, updateTime: Int, refreshTime: Int, isUpdatable: Boolean) {
        val currentKit = kits[currentKitIndex]
        val leaderboardLines = getTopProfilesByKitEloLines(currentKit).sortedByDescending { it.second }
            .take(10)
            .mapIndexed { index, (profile, elo) ->
                configFile.getString("HOLOGRAMS.GLOBAL-LEADERBOARDS.FORMAT")
                    .replace("<top>", (index + 1).toString())
                    .replace("<name>", "${ChatColor.valueOf(CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(profile.uuid)).color)}${profile.name}")
                    .replace("<elo>", elo.toString())
            }
        val formattedLines = configFile.getStringList("HOLOGRAMS.KIT-ELO-LEADERBOARDS.LINES").map { line ->
            line.replace("<kit>", currentKit.name)
        }.toMutableList()

        if (formattedLines.contains("<lines>")) {
            formattedLines.remove("<lines>")
            formattedLines.addAll(leaderboardLines)
        }

        plugin.hologramManager.hologramCreation(
            location,
            updateTime,
            refreshTime,
            "kitEloLeaderboards",
            formattedLines,
            isUpdatable,
            80.0,
            false
        ) {}
    }

    private fun getTopProfilesByKitEloLines(kit: Kit, limit: Int = 10): MutableList<Pair<Profile, Int>> {
        val sort = Document("kitStatistics.${kit.name}.elo", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.getKitStatistic(kit.name)?.elo ?: 0
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles
    }

    fun getTopProfilesByRankedKitsLines(limit: Int = 10): List<Map.Entry<Profile, Int>> {
        val rankedKits = plugin.kitManager.kits.values.filter { it.ranked }
        val profilesEloMap = mutableMapOf<Profile, MutableList<Int>>()

        for (kit in rankedKits) {
            val sort = Document("kitStatistics.${kit.name}.elo", -1)
            val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
                .find()
                .sort(sort)
                .limit(limit)
                .iterator()

            cursor.use {
                while (it.hasNext()) {
                    val document = it.next()
                    val profile = plugin.profileManager.parse(document)
                    val elo = profile.getKitStatistic(kit.name)?.elo ?: 0

                    profilesEloMap.computeIfAbsent(profile) { mutableListOf() }.add(elo)
                }
            }
        }

        val averageEloMap = profilesEloMap.mapValues { entry ->
            val elos = entry.value
            if (elos.isNotEmpty()) elos.sum() / elos.size else 0
        }

        val sortedProfiles = averageEloMap.entries.sortedByDescending { it.value }.take(limit)

        return sortedProfiles
    }

    fun getTopProfilesByKitWins(kit: Kit, limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("kitStatistics.${kit.name}.wins", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.getKitStatistic(kit.name)?.wins ?: 0
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }

    fun getTopProfilesByGlobalElo(limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("globalStatistics.elo", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.globalStatistic.elo
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }

    fun getAverageEloForPlayer(playerUuid: UUID): Int {
        val rankedKits = plugin.kitManager.kits.values.filter { it.ranked }
        val query = Document("_id", playerUuid.toString())
        val document = plugin.mongoManager.profileCollection.find(query).firstOrNull()
            ?: throw IllegalArgumentException("Player not found")

        val kitStatistics = document.get("kitStatistics") as? Document ?: return 1000

        val elos = rankedKits.mapNotNull { kit ->
            (kitStatistics.get(kit.name) as? Document)?.getInteger("elo")
        }

        return if (elos.isNotEmpty()) {
            (elos.sum() / elos.size)
        } else {
            1000
        }
    }

    private fun getTopProfilesByDailyWinStreakLines(limit: Int = 10): MutableList<Pair<Profile, Int>> {
        val sort = Document("globalStatistic.dailyWinStreak", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val dailyWinStreak = profile.globalStatistic.dailyWinStreak
                topProfiles.add(profile to dailyWinStreak)
            }
        }

        return topProfiles
    }

    fun getTopProfilesByKitCasualDailyWins(kit: Kit, limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("kitStatistics.${kit.name}.bestCasualStreak", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.getKitStatistic(kit.name)?.bestCasualStreak ?: 0
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }

    fun getTopProfilesByKitCompetitiveDailyWins(kit: Kit, limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("kitStatistics.${kit.name}.rankedBestStreak", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.getKitStatistic(kit.name)?.rankedBestStreak ?: 0
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }

    fun getTopProfilesByKitRankedWins(kit: Kit, limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("kitStatistics.${kit.name}.rankedWins", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.getKitStatistic(kit.name)?.rankedWins ?: 0
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }

    fun getTopProfilesByKitElo(kit: Kit, limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("kitStatistics.${kit.name}.elo", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val elo = profile.getKitStatistic(kit.name)?.elo ?: 0
                topProfiles.add(profile to elo)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }

    fun getLeaderboardPosition(profile: Profile, kit: Kit): Int {
        val profiles = getTopProfilesByKitElo(kit, 1000)
        return profiles.indexOfFirst { it.first.uuid == profile.uuid } + 1
    }

    fun getTopProfilesByDailyWinStreak(limit: Int = 10): List<Pair<Profile, Int>> {
        val sort = Document("globalStatistic.dailyWinStreak", -1)
        val cursor: MongoCursor<Document> = plugin.mongoManager.profileCollection
            .find()
            .sort(sort)
            .limit(limit)
            .iterator()

        val topProfiles = mutableListOf<Pair<Profile, Int>>()

        cursor.use {
            while (it.hasNext()) {
                val document = it.next()
                val profile = plugin.profileManager.parse(document)
                val dailyWinStreak = profile.globalStatistic.dailyWinStreak ?: 0
                topProfiles.add(profile to dailyWinStreak)
            }
        }

        return topProfiles.sortedByDescending { it.second }
    }
}

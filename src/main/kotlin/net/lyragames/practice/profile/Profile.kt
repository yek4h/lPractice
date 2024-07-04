package net.lyragames.practice.profile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.duel.DuelRequest
import net.lyragames.practice.match.Match
import net.lyragames.practice.party.invitation.PartyInvitation
import net.lyragames.practice.profile.editor.KitEditorData
import net.lyragames.practice.profile.settings.Settings
import net.lyragames.practice.profile.statistics.KitStatistic
import net.lyragames.practice.profile.statistics.global.GlobalStatistics
import net.lyragames.practice.queue.QueuePlayer
import net.lyragames.practice.utils.Cooldown
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * This Project is property of Zowpy © 2021
 * Redistribution of this Project is not allowed
 * Created: 12/19/2021
 * Project: Practice
 *
 * Recoded by yek4h
 *
 */

class Profile(val uuid: UUID, var name: String? = null) : IProfile {

    var match: UUID? = null
    var matchObject: Match? = null
    var ffa: UUID? = null
    var spectatingMatch: UUID? = null
    var queuePlayer: QueuePlayer? = null
    var kitStatistics: MutableList<KitStatistic> = mutableListOf()
    var globalStatistic = GlobalStatistics()
    var party: UUID? = null
    var partyInvites: MutableList<PartyInvitation> = mutableListOf()
    var duelRequests: MutableList<DuelRequest> = mutableListOf()
    var kitEditorData: KitEditorData? = KitEditorData()
    var settings: Settings = Settings()
    val followers: MutableList<UUID> = mutableListOf()
    var silent = false
    var following = false
    var state = ProfileState.LOBBY
    var canBuild = false
    var enderPearlCooldown: Cooldown? = null
    var arrowCooldown: Cooldown? = null
    var fireBallCooldown: Cooldown? = null
    var lastWinStreakUpdate: Date? = null

    val player: Player
        get() = Bukkit.getPlayer(uuid) ?: throw IllegalStateException("Player not found")

    private fun toBson(): Document {
        return Document("_id", uuid.toString()).apply {
            append("name", name)
            append("duelRequests", duelRequests.map { PracticePlugin.GSON.toJson(it) })
            append("partyInvites", partyInvites.map { PracticePlugin.GSON.toJson(it) })
            append("kitStatistics", kitStatistics.map { PracticePlugin.GSON.toJson(it) })
            append("globalStatistics", PracticePlugin.GSON.toJson(globalStatistic))
            append("settings", PracticePlugin.GSON.toJson(settings))
            append("silent", silent)
            append("lastWinStreakUpdate", lastWinStreakUpdate?.time)
        }
    }

    fun save() {
        save(true)
    }

    override fun save(async: Boolean) {
        val document = toBson()
        val collection = PracticePlugin.instance.mongoManager.profileCollection
        if (async) {
            CompletableFuture.runAsync {
                collection.updateOne(Filters.eq("_id", uuid.toString()), Document("\$set", document), UpdateOptions().upsert(true))
            }
        } else {
            collection.updateOne(Filters.eq("_id", uuid.toString()), Document("\$set", document), UpdateOptions().upsert(true))
        }
    }

    override fun load() {
        val document = PracticePlugin.instance.mongoManager.profileCollection
            .find(Filters.eq("_id", uuid.toString())).firstOrNull()
        if (document == null) {
            PracticePlugin.instance.kitManager.kits.values.forEach { kit ->
                kitStatistics.add(KitStatistic(kit.name))
            }
            save(true)
            return
        }
        load(document)
    }

    fun load(document: Document) {
        var save = false

        name = name ?: document.getString("name").also { save = true }

        duelRequests = document.getList("duelRequests", String::class.java).map {
            PracticePlugin.GSON.fromJson(it, DuelRequest::class.java)
        }.toMutableList()
        partyInvites = document.getList("partyInvites", String::class.java).map {
            PracticePlugin.GSON.fromJson(it, PartyInvitation::class.java)
        }.toMutableList()

        if (duelRequests.removeIf { it.isExpired() } || partyInvites.removeIf { it.isExpired() }) {
            save = true
        }

        kitStatistics = document.getList("kitStatistics", String::class.java).map {
            PracticePlugin.GSON.fromJson(it, KitStatistic::class.java)
        }.toMutableList()
        globalStatistic = PracticePlugin.GSON.fromJson(document.getString("globalStatistics"), GlobalStatistics::class.java)

        settings = when (val settingsField = document["settings"]) {
            is String -> {
                try {
                    PracticePlugin.GSON.fromJson(settingsField, Settings::class.java)
                } catch (e: JsonSyntaxException) {
                    Settings() // Fallback to a default settings object
                }
            }
            is Document -> PracticePlugin.GSON.fromJson(settingsField.toJson(), Settings::class.java)
            else -> Settings()
        }

        silent = document.getBoolean("silent", false)
        lastWinStreakUpdate = document.getLong("lastWinStreakUpdate")?.let { Date(it) }

        PracticePlugin.instance.kitManager.kits.values.forEach { kit ->
            if (kitStatistics.none { it.kit.equals(kit.name, false) }) {
                kitStatistics.add(KitStatistic(kit.name))
                save = true
            }
        }

        if (save) save(true)
    }

    fun getPartyInvite(uuid: UUID): PartyInvitation? {
        return partyInvites.firstOrNull { it.uuid == uuid && !it.isExpired() }
    }

    fun getDuelRequest(uuid: UUID): DuelRequest? {
        return duelRequests.firstOrNull { it.uuid == uuid && !it.isExpired() }
    }

    fun getKitStatistic(kit: String): KitStatistic? {
        return kitStatistics.firstOrNull { it.kit.equals(kit, true) }
    }

    override fun delete() {
        PracticePlugin.instance.profileManager.profiles.remove(uuid)
        PracticePlugin.instance.mongoManager.profileCollection.deleteOne(Filters.eq("_id", uuid.toString()))
    }

    fun addWin() {
        globalStatistic.wins++
        globalStatistic.currentWinStreak++
        globalStatistic.dailyWinStreak++

        if (globalStatistic.currentWinStreak > globalStatistic.bestWinStreak) {
            globalStatistic.bestWinStreak = globalStatistic.currentWinStreak
        }

        // Actualizar el globalElo
        updateGlobalElo()

        save(true)
    }

    fun resetDailyWinStreak() {
        globalStatistic.dailyWinStreak = 0
        save(true)
    }

    fun resetCurrentWinStreak() {
        globalStatistic.currentWinStreak = 0
        save(true)
    }

    fun updateWinStreak() {
        val now = Date()
        if (lastWinStreakUpdate == null || now.time - lastWinStreakUpdate!!.time >= 24 * 60 * 60 * 1000) {
            globalStatistic.dailyWinStreak = 0
            lastWinStreakUpdate = now
            save(true)
        }
    }

    fun updateGlobalElo() {
        val rankedKits = kitStatistics.filter { it.elo > 0 } // Solo kits con ELO > 0
        val totalElo = rankedKits.sumBy { it.elo }
        val count = rankedKits.size

        globalStatistic.elo = if (count > 0) totalElo / count else 1000 // Default ELO si no hay kits con ELO
    }
}

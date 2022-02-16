package net.lyragames.practice.profile

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.EditedKit
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.profile.statistics.KitStatistic
import net.lyragames.practice.profile.statistics.global.GlobalStatistics
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

/**
 * This Project is property of Zowpy © 2021
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 12/19/2021
 * Project: Practice
 */

class Profile(val uuid: UUID, val name: String) {

    var editedKits: List<EditedKit> = ArrayList()
    var match: UUID? = null

    var kitStatistics: MutableList<KitStatistic> = mutableListOf()
    var globalStatistic = GlobalStatistics()

    fun getEditKitsByKit(kit: Kit): List<EditedKit> {
        return editedKits.stream().filter { editedKit: EditedKit -> editedKit.originalKit == kit.name }
            .collect(Collectors.toList())
    }

    private fun toBson() : Document {
        return Document("uuid", uuid.toString())
            .append("name", name)
            .append("editedKits", editedKits.stream().map { editedKits -> PracticePlugin.GSON.toJson(editedKits) }.collect(Collectors.toList()))
            .append("kitsStatistics", kitStatistics.stream().map { kitStatistic -> PracticePlugin.GSON.toJson(kitStatistic) }.collect(Collectors.toList()))
            .append("globalStatistics", PracticePlugin.GSON.toJson(globalStatistic))
    }

    fun save() {
        CompletableFuture.runAsync {
            PracticePlugin.instance.practiceMongo.profiles.replaceOne(Filters.eq("uuid", uuid.toString()), toBson(), ReplaceOptions().upsert(true))
        }
    }

    fun load() {
        val document = PracticePlugin.instance.practiceMongo.profiles.find(Filters.eq("uuid", uuid.toString())).first()

        if (document == null) {
            save()
            return
        }

        load(document)
    }

    fun getKitStatistic(name: String): KitStatistic? {
        return kitStatistics.stream().filter { kitStatistic -> kitStatistic.kit.equals(name, true) }
            .findFirst().orElse(null)
    }

    fun load(document: Document) {
        var save = false

        if (!document.getString("name").equals(name)) {
            save = true
        }

        editedKits = document.getList("editedKits", String::class.java).stream().map { s -> PracticePlugin.GSON.fromJson(s, EditedKit::class.java) }.collect(Collectors.toList())
        kitStatistics = document.getList("kitsStatistics", String::class.java).stream().map { s -> PracticePlugin.GSON.fromJson(s, KitStatistic::class.java) }.collect(Collectors.toList())
        globalStatistic = PracticePlugin.GSON.fromJson(document.getString("globalStatistics"), GlobalStatistics::class.java)

        for (kit in Kit.kits) {
            var found = false

            for (kitStatistic in kitStatistics) {
                if (kitStatistic.kit.equals(kit.name, false)) {
                    found = true
                    break
                }
            }

            if (!found) {
                val kitStatistic = KitStatistic(kit.name)
                kitStatistics.add(kitStatistic)
                save = true
            }
        }

        if (save) {
            save()
        }
    }

    companion object {
        @JvmStatic
        val profiles: MutableList<Profile?> = mutableListOf()

        @JvmStatic
        fun getByUUID(uuid: UUID): Profile? {
            return profiles.stream().filter { profile: Profile? -> profile?.uuid == uuid }
                .findFirst().orElse(null)
        }
    }
}
package net.lyragames.practice.arena.rating

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import net.lyragames.practice.PracticePlugin
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy & EliteAres
 * Created: 6/2/2022
 * Project: lPractice
 */

class ArenaRating(val uuid: UUID, val stars: Int, val user: UUID, val arena: String) {

    private fun toBson(): Document {
        return Document("uuid", uuid.toString())
            .append("stars", stars)
            .append("user", user.toString())
            .append("arena", arena)
    }

    fun save() {
        CompletableFuture.runAsync {
            PracticePlugin.instance.mongoManager.arenaRatingsCollection
                .replaceOne(Filters.eq("uuid", uuid.toString()), toBson(), ReplaceOptions().upsert(true))
        }
    }
}
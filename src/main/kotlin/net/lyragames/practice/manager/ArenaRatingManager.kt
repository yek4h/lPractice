package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.rating.ArenaRating
import java.util.*

object ArenaRatingManager {

    val arenaRatings: MutableList<ArenaRating> = mutableListOf()

    fun load() {
        val ratingsCollection = PracticePlugin.instance.mongoManager.arenaRatingsCollection
        val documents = ratingsCollection.find().toList()

        if (documents.isEmpty()) return

        documents.forEach { document ->
            val arenaRating = ArenaRating(
                UUID.fromString(document.getString("uuid")),
                document.getInteger("stars"),
                UUID.fromString(document.getString("user")),
                document.getString("arena")
            )
            arenaRatings.add(arenaRating)
        }
    }

    fun getArenaRatings(arena: Arena): List<ArenaRating> {
        return arenaRatings.filter { it.arena.equals(arena.name, true) }
    }

    fun hasRated(uuid: UUID, arena: Arena): Boolean {
        return getArenaRatings(arena).any { it.user == uuid }
    }

    fun getAverageRating(arena: Arena): Double {
        val ratings = getArenaRatings(arena)
        val totalRatings = ratings.sumOf { it.stars }

        return if (ratings.isNotEmpty()) totalRatings.toDouble() / ratings.size else 0.0
    }

    fun getUsersRated(stars: Int, arena: Arena): Int {
        return getArenaRatings(arena).count { it.stars == stars }
    }
}

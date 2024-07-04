package net.lyragames.practice.profile

import com.mongodb.client.MongoCursor
import com.mongodb.client.model.Filters
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import org.bson.Document
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.logging.Logger

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 20/06/2024
*/
object ProfileManager {

    private val logger: Logger = Logger.getLogger(ProfileManager::class.java.name)
    val profiles: ConcurrentMap<UUID, Profile> = ConcurrentHashMap()

    internal fun initialLoaded() {
        val collection = PracticePlugin.instance.mongoManager.profileCollection
        collection.find().forEach {
            val profile = parse(it)
            profiles[profile.uuid] = profile
        }
        logger.info("[Profile] Successfully enabled '${ProfileManager::class.java.name}'")
    }

    internal fun shutdown() {
        logger.info("[Profile] Successfully disabled '${ProfileManager::class.java.name}'")
        profiles.values.forEach {
            it.save(true)
        }
        profiles.clear()
    }

    fun getProfilesValue(): Collection<Profile> {
        return profiles.values
    }

    internal fun findById(id: UUID): Profile? {
        return profiles[id]
    }

    internal fun findByIdRealTime(id: UUID): Profile? {
        val document = PracticePlugin.instance.mongoManager.profileCollection
            .find(Filters.eq("_id", id.toString()))
            .firstOrNull() ?: return null
        return loadProfileFromDocument(document)
    }

    fun findByName(name: String): Profile? {
        return profiles.values.firstOrNull { it.name == name }
    }

    internal fun parse(document: Document): Profile {
        return Profile(UUID.fromString(document.getString("_id"))).apply {
            load(document)
        }
    }

    private fun loadProfileFromDocument(document: Document): Profile? {
        return try {
            Profile(UUID.fromString(document.getString("_id"))).apply {
                load(document)
            }
        } catch (e: Exception) {
            logger.warning("Failed to load profile from document: ${document.toJson()}")
            null
        }
    }
}
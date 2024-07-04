package net.lyragames.practice.api

import com.mongodb.client.model.Filters
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.Profile
import java.util.*


/**
 * This Project is property of Zowpy & EliteAres © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy & EliteAres
 * Created: 2/15/2022
 * Project: lPractice
 */

class PracticeAPI {

    fun retrieveProfile(uuid: UUID): Profile? {
        val document = PracticePlugin.instance.mongoManager.profileCollection.find(Filters.eq("uuid", uuid.toString())).first() ?: return null

        val profile = Profile(uuid, null)
        profile.load(document)

        return profile
    }
}
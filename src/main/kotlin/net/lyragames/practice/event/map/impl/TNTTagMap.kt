package net.lyragames.practice.event.map.impl

import net.lyragames.llib.utils.LocationUtil
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.map.type.EventMapType


/**
 * This Project is property of Zowpy & EliteAres © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy & EliteAres
 * Created: 3/30/2022
 * Project: lPractice
 */

class TNTTagMap(name: String) : EventMap(name) {

    override var type = EventMapType.TNT_TAG

    override fun save() {
        val configFile = PracticePlugin.instance.eventsFile
        val section = configFile.createSection("maps.${name}")

        section.set("type", type.name)
        section.set("spawn", LocationUtil.serialize(spawn))

        configFile.save()
    }
}
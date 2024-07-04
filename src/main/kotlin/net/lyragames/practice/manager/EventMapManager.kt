package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.map.impl.TNTRunMap
import net.lyragames.practice.event.map.impl.TNTTagMap
import net.lyragames.practice.event.map.type.EventMapType
import net.lyragames.practice.utils.LocationUtil
import java.util.concurrent.ThreadLocalRandom

object EventMapManager {

    val maps: MutableList<EventMap> = mutableListOf()

    fun load() {
        val configFile = PracticePlugin.instance.eventsFile

        if (configFile.getConfigurationSection("maps") == null) return

        for (key in configFile.getConfigurationSection("maps")!!.getKeys(false)) {
            if (key == null) continue

            var eventMap = EventMap(key)

            val section = configFile.getConfigurationSection("maps.${key}")
            eventMap.type = EventMapType.valueOf(section!!.getString("type").uppercase())

            if (eventMap.type == EventMapType.TNT_RUN) {
                eventMap = TNTRunMap(key)

                if (section.getString("spawn") != null) {
                    eventMap.spawn = LocationUtil.deserialize(section.getString("spawn"))
                }

                if (section.getInt("deadzone") != null) {
                    eventMap.deadzone = section.getInt("deadzone")
                }

                maps.add(eventMap)
                continue
            }else if (eventMap.type == EventMapType.TNT_TAG) {
                eventMap = TNTTagMap(key)

                if (section.getString("spawn") != null) {
                    eventMap.spawn = LocationUtil.deserialize(section.getString("spawn"))
                }

                maps.add(eventMap)
                continue
            }

            if (section.getString("spawn") != null) {
                eventMap.spawn = LocationUtil.deserialize(section.getString("spawn"))
            }

            if (section.getString("l1") != null) {
                eventMap.l1 = LocationUtil.deserialize(section.getString("l1"))
            }

            if (section.getString("l2") != null) {
                eventMap.l2 = LocationUtil.deserialize(section.getString("l2"))
            }

            maps.add(eventMap)
        }
    }

    fun getFreeMap(): EventMap? {
        return if (maps.isEmpty()) null else maps[ThreadLocalRandom.current().nextInt(maps.size)]
    }

    fun getFreeMap(eventMapType: EventMapType): EventMap? {
        return maps.stream().filter { it.type == eventMapType }
            .findAny().orElse(null)
    }

    fun getByName(name: String): EventMap? {
        return maps.stream().filter { it.name.equals(name, true) }
            .findFirst().orElse(null)
    }
}
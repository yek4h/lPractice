package net.lyragames.practice.utils.providers

import com.jonahseguin.drink.argument.CommandArg
import com.jonahseguin.drink.parametric.DrinkProvider
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.manager.EventMapManager


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class EventMapProvider : DrinkProvider<EventMap>() {
    override fun doesConsumeArgument() = true
    override fun isAsync() = false
    override fun allowNullArgument() = false
    override fun defaultNullValue(): EventMap? = null

    override fun provide(arg: CommandArg, annotations: List<Annotation>): EventMap {
        val mapName = arg.get()
        return EventMapManager.getByName(mapName) ?: throw IllegalArgumentException("EventMap not found: $mapName")
    }

    override fun argumentDescription() = "event map"
    override fun getSuggestions(prefix: String): List<String> {
        return EventMapManager.maps.map { it.name }.filter { it.startsWith(prefix, ignoreCase = true) }
    }
}
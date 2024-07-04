package net.lyragames.practice.utils.providers

import com.jonahseguin.drink.argument.CommandArg
import com.jonahseguin.drink.parametric.DrinkProvider
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.KitManager


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class KitProvider(private val kitManager: KitManager) : DrinkProvider<Kit>() {
    override fun doesConsumeArgument() = true
    override fun isAsync() = false
    override fun allowNullArgument() = false
    override fun defaultNullValue(): Kit? = null

    override fun provide(arg: CommandArg, annotations: List<Annotation>): Kit {
        val kitName = arg.get()
        return kitManager.getKit(kitName) ?: throw IllegalArgumentException("Kit not found: $kitName")
    }

    override fun argumentDescription() = "kit"
    override fun getSuggestions(prefix: String): List<String> {
        return kitManager.getKits().map { it.name }.filter { it.startsWith(prefix, ignoreCase = true) }
    }
}
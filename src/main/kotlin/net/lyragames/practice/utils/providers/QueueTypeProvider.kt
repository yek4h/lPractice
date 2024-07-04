package net.lyragames.practice.utils.providers

import com.jonahseguin.drink.argument.CommandArg
import com.jonahseguin.drink.parametric.DrinkProvider
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.queue.QueueType


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 21/06/2024
*/

class QueueTypeProvider : DrinkProvider<QueueType>() {

    override fun doesConsumeArgument() = true
    override fun isAsync() = false
    override fun allowNullArgument() = false
    override fun defaultNullValue(): QueueType? = null

    override fun provide(arg: CommandArg, annotations: List<Annotation>): QueueType {
        val queueTypeName = arg.get().uppercase()
        return QueueType.entries.firstOrNull { it.name == queueTypeName }
            ?: throw IllegalArgumentException("Queue type not found: $queueTypeName")
    }

    override fun argumentDescription() = "queue type"

    override fun getSuggestions(prefix: String): List<String> {
        return QueueType.entries.map { it.name }.filter { it.startsWith(prefix.uppercase(), ignoreCase = true) }
    }
}
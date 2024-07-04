package net.lyragames.practice.queue

import net.lyragames.practice.Locale
import org.bukkit.Bukkit
import java.util.*

class QueuePlayer(val uuid: UUID, val name: String, val queue: Queue, val pingFactor: Int, var elo: Int = 0) {

    val started: Long = System.currentTimeMillis()
    private var range: Int = 25
    private var ticked: Int = 0

    fun tickRange() {
        ticked++
        if (ticked % 3 == 0) {
            range += 5
            if (ticked >= 50) {
                ticked = 0
                if (queue.type == QueueType.RANKED) {
                    Bukkit.getPlayer(uuid)?.sendMessage(Locale.ELO_SEARCH.getMessage()
                        .replace("<min>", "${getMinRange()}")
                        .replace("<max>", "${getMaxRange()}"))
                }
            }
        }
    }

    fun isInRange(elo: Int): Boolean {
        return elo in (this.elo - range)..(this.elo + range)
    }

    fun getMinRange(): Int {
        return (elo - range).coerceAtLeast(0)
    }

    fun getMaxRange(): Int {
        return (elo + range).coerceAtMost(2500)
    }
}

package net.lyragames.practice.utils.scoreboard

import java.util.concurrent.TimeUnit


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 03/07/2024
*/

class ScoreCooldown(private val handler: ScoreboardHandler, val id: String, duration: Double) {
    val endTime: Long = System.nanoTime() + TimeUnit.SECONDS.toNanos(duration.toLong())

    init {
        handler.addCooldown(this)
    }

    fun getTimeLeft(): String {
        val timeLeft = endTime - System.nanoTime()
        val seconds = TimeUnit.NANOSECONDS.toSeconds(timeLeft)
        val millis = TimeUnit.NANOSECONDS.toMillis(timeLeft) % 1000
        return String.format("%d.%03d", seconds, millis)
    }

    fun isExpired(): Boolean {
        return System.nanoTime() >= endTime
    }

    fun cancel() {
        handler.removeCooldown(this)
    }
}
package net.lyragames.practice.task

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.EventType
import net.lyragames.practice.event.impl.TNTTagEvent
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.utils.CC
import org.bukkit.scheduler.BukkitRunnable

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 * Created: 4/1/2022
 * Project: lPractice
 */

object TNTTagTask : BukkitRunnable() {

    init {
        this.runTaskTimerAsynchronously(PracticePlugin.instance, 20, 20)
    }

    override fun run() {
        val event = EventManager.event ?: return
        if (event.type != EventType.TNT_TAG) return
        if (event.state != EventState.FIGHTING) return

        val seconds = 60 - (System.currentTimeMillis() - event.started) / 1000

        when (seconds) {
            30L, 15L, 10L, 5L, 4L, 3L, 2L, 1L -> {
                event.sendMessage("${CC.PRIMARY}The round is ending in ${CC.SECONDARY}$seconds${CC.PRIMARY} seconds.")
            }
        }

        if (seconds <= 0) {
            (event as TNTTagEvent).endRound(null)
        }
    }
}
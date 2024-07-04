package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class WinStreakManager {

    var winstreakEnabled = true
    private val scheduler = Executors.newScheduledThreadPool(1)

    init {
        scheduler.scheduleAtFixedRate({
            if (winstreakEnabled) {
                updateAllWinStreaks()
            }
        }, 0, 30, TimeUnit.MINUTES)
    }

    fun updateAllWinStreaks() {
        PracticePlugin.instance.profileManager.profiles.values.forEach {
            it.updateWinStreak()
        }
    }

    fun toggleWinstreak() {
        winstreakEnabled = !winstreakEnabled

        if (winstreakEnabled) {
        }
    }

    fun isWinstreakEnabled(): Boolean {
        return winstreakEnabled
    }

    fun shutdown() {
        scheduler.shutdown()
    }
}
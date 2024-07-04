package net.lyragames.practice.task

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.match.Match
import net.lyragames.practice.profile.ProfileState
import org.bukkit.scheduler.BukkitRunnable

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 05/06/2024
 */

object ArrowCooldownTask: BukkitRunnable() {

    init {
        this.runTaskTimerAsynchronously(PracticePlugin.instance, 2L, 2L)
    }

    override fun run() {
        for (profile in PracticePlugin.instance.profileManager.profiles.values) {
            if (profile?.state == ProfileState.MATCH && profile.match != null) {
                val match = Match.getByUUID(profile.match!!) ?: return

                if (match.kit.bridge && profile.arrowCooldown != null && !profile.arrowCooldown!!.hasExpired()) {
                    val player = profile.player
                    val seconds = profile.arrowCooldown!!.timeRemaining.toInt() / 1000

                    player.level = seconds
                    player.exp = profile.arrowCooldown!!.timeRemaining / 6000.0f
                }
            }
            }
    }
}
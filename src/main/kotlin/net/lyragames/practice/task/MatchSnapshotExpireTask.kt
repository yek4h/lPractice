package net.lyragames.practice.task

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.match.snapshot.MatchSnapshot
import org.bukkit.scheduler.BukkitRunnable

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 05/06/2024
 */

object MatchSnapshotExpireTask : BukkitRunnable() {

    init {
        this.runTaskTimerAsynchronously(PracticePlugin.instance, 0, 20L)
    }

    override fun run() {
        MatchSnapshot.snapshots.removeIf { it.isExpired() }
    }
}
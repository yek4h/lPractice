package net.lyragames.practice.task

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.EventType
import net.lyragames.practice.event.impl.TNTRunEvent
import net.lyragames.practice.event.map.impl.TNTRunMap
import net.lyragames.practice.manager.EventManager
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 * Created: 3/26/2022
 * Project: lPractice
 *
 * Updated by yek4h
 *
 */

object TNTEventBlockRemovalTask : BukkitRunnable() {

    init {
        this.runTaskTimerAsynchronously(PracticePlugin.instance, 20L, 7L)
    }

    override fun run() {
        val currentEvent = EventManager.event ?: return
        if (currentEvent.state != EventState.FIGHTING) return

        if (currentEvent.type == EventType.TNT_RUN) {
            currentEvent as TNTRunEvent

            currentEvent.players.forEach { eventPlayer ->
                val player = eventPlayer.player

                if (player.location.y <= (currentEvent.eventMap as TNTRunMap).deadzone && !eventPlayer.dead) {
                    eventPlayer.dead = true
                    currentEvent.players.forEach { it.player.hidePlayer(eventPlayer.player) }
                    eventPlayer.player.allowFlight = true
                    eventPlayer.player.isFlying = true

                    if (currentEvent.getAlivePlayers().size == 1) {
                        val winner = currentEvent.getAlivePlayers()[0]
                        currentEvent.endRound(winner)
                    }
                    return
                }

                val block = player.location.subtract(0.0, 0.5, 0.0).block

                if (block.type == Material.SAND) {
                    currentEvent.removedBlocks[block] = Material.SAND
                    block.type = Material.AIR

                    val tntBlock = block.location.subtract(0.0, 0.5, 0.0).block
                    if (tntBlock.type == Material.TNT) {
                        currentEvent.removedBlocks[tntBlock] = Material.TNT
                        tntBlock.type = Material.AIR
                    }
                }
            }
        }
    }
}
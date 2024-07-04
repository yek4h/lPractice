package net.lyragames.practice.event.impl

import net.lyragames.practice.event.Event
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.player.EventPlayer
import net.lyragames.practice.event.player.EventPlayerState
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.countdown.Countdown
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import java.util.*


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/21/2022
 * Project: lPractice
 */

class BracketsEvent(host: UUID, eventMap: EventMap, val kit: Kit) : Event(host, eventMap) {

    val blocksPlaced: MutableList<Block> = mutableListOf()

    override fun startRound() {
        state = EventState.STARTING

        playingPlayers = getNextPlayers()

        for ((i, eventPlayer) in playingPlayers.withIndex()) {
            eventPlayer.roundsPlayed++
            eventPlayer.state = EventPlayerState.FIGHTING

            val profile = PracticePlugin.instance.profileManager.findById(eventPlayer.uuid)!!

            PlayerUtil.reset(eventPlayer.player)

            profile.getKitStatistic(kit.name)?.generateBooks(eventPlayer.player)

            if (i == 0) {
                eventPlayer.player.teleport(eventMap.l1)
            } else {
                eventPlayer.player.teleport(eventMap.l2)
            }

            PlayerUtil.denyMovement(eventPlayer.player)
        }

        for (eventPlayer in players) {
            if (eventPlayer.offline) continue

            countdowns.add(Countdown(
                eventPlayer.player,
                "&aRound $round starting in <seconds> seconds!",
                6
            ) {
                eventPlayer.player.sendMessage(CC.GREEN + "Round started!")
                state = EventState.FIGHTING

                started = System.currentTimeMillis()

                if (playingPlayers.contains(eventPlayer)) {
                    PlayerUtil.allowMovement(eventPlayer.player)
                }
            })
        }

    }

    override fun endRound(winner: EventPlayer?) {
        state = EventState.ENDING

        for (eventPlayer in playingPlayers) {
            eventPlayer.state = EventPlayerState.LOBBY

            eventPlayer.player.teleport(eventMap.spawn)
            Hotbar.giveHotbar(PracticePlugin.instance.profileManager.findById(eventPlayer.uuid)!!)
            PlayerUtil.reset(eventPlayer.player)
        }

        reset()

        if (getRemainingRounds() == 0) {
            end(winner)
        }else {
            round++
            startRound()
        }
    }

    override fun end(winner: EventPlayer?) {
        Bukkit.broadcastMessage("${CC.GREEN}${if (winner != null) winner.player.name else "N/A"} won the event!")

        players.forEach {
            forceRemove(it)
        }

        countdowns.forEach {
            it.cancel()
        }

        countdowns.clear()

        EventManager.event = null
    }

    fun reset() {
        blocksPlaced.forEach { it.type = Material.AIR }
        droppedItems.forEach { it.remove() }
    }
}
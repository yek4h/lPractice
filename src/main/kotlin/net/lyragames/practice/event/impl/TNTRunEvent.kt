package net.lyragames.practice.event.impl

import net.lyragames.practice.event.Event
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.map.impl.TNTRunMap
import net.lyragames.practice.event.player.EventPlayer
import net.lyragames.practice.event.player.EventPlayerState
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
 * Created: 3/26/2022
 * Project: lPractice
 */

class TNTRunEvent(host: UUID, eventMap: EventMap) : Event(host, eventMap) {

    val removedBlocks: MutableMap<Block, Material> = mutableMapOf()

    override fun startRound() {
        eventMap as TNTRunMap
        state = EventState.STARTING

        for (eventPlayer in players) {
            eventPlayer.state = EventPlayerState.FIGHTING

            PlayerUtil.reset(eventPlayer.player)
            eventPlayer.player.teleport(eventMap.spawn)
        }

        for (eventPlayer in players) {
            if (eventPlayer.offline) continue

            countdowns.add(Countdown(
                eventPlayer.player,
                "&aEvent starting in <seconds> seconds!",
                6
            ) {
                eventPlayer.player.sendMessage("${CC.GREEN}Event started!")
                state = EventState.FIGHTING

                started = System.currentTimeMillis()
            })
        }

    }

    override fun endRound(winner: EventPlayer?) {
        state = EventState.ENDING

        for (eventPlayer in playingPlayers) {
            eventPlayer.state = EventPlayerState.LOBBY

            Hotbar.giveHotbar(PracticePlugin.instance.profileManager.findById(eventPlayer.uuid)!!)
            PlayerUtil.reset(eventPlayer.player)
        }

        end(winner)
    }

    override fun handleDisconnect(eventPlayer: EventPlayer) {
        sendMessage("${CC.SECONDARY}${eventPlayer.name}${CC.PRIMARY} disconnected.")

        eventPlayer.dead = true
        eventPlayer.offline = true

        playingPlayers.remove(eventPlayer)

        if (getAlivePlayers().size < 2) {
            endRound(null)
        }
    }

    override fun end(winner: EventPlayer?) {
        Bukkit.broadcastMessage("${CC.GREEN}${if (winner != null) winner.player.name else "no one"} won the event!")

        players.forEach {
            forceRemove(it)
        }

        countdowns.forEach {
            it.cancel()
        }

        countdowns.clear()

        reset()
        EventManager.event = null
    }

    fun reset() {
        for (entry in removedBlocks) {
            entry.key.type = entry.value
        }
    }
}
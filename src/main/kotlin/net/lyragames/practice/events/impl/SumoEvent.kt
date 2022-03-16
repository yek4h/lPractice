package net.lyragames.practice.events.impl

import net.lyragames.llib.utils.CC
import net.lyragames.llib.utils.Countdown
import net.lyragames.llib.utils.PlayerUtil
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.events.Event
import net.lyragames.practice.events.EventState
import net.lyragames.practice.events.map.EventMap
import net.lyragames.practice.events.player.EventPlayer
import net.lyragames.practice.events.player.EventPlayerState
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import java.util.*


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/16/2022
 * Project: lPractice
 */

class SumoEvent(host: UUID, eventMap: EventMap) : Event(host, eventMap) {

    override fun startRound() {
        state = EventState.STARTING

        playingPlayers = getNextPlayers()

        for ((i, eventPlayer) in playingPlayers.withIndex()) {
            eventPlayer.roundsPlayed++
            eventPlayer.state = EventPlayerState.FIGHTING

            if (i == 0) {
                eventPlayer.player.teleport(eventMap.l1)
            }else {
                eventPlayer.player.teleport(eventMap.l2)
            }

            PlayerUtil.denyMovement(eventPlayer.player)
        }

        for (eventPlayer in players) {
            if (eventPlayer.offline) continue

            Countdown(
                PracticePlugin.instance,
                eventPlayer.player,
                "&aRound $round starting in <seconds> seconds!",
                6
            ) {
                eventPlayer.player.sendMessage(CC.GREEN + "Round started!")
                state = EventState.FIGHTING

                if (playingPlayers.contains(eventPlayer)) {
                    PlayerUtil.allowMovement(eventPlayer.player)
                }
            }
        }

    }

    override fun endRound(winner: EventPlayer?) {
        state = EventState.ENDING

        for (eventPlayer in playingPlayers) {
            eventPlayer.state = EventPlayerState.LOBBY

            eventPlayer.player.teleport(eventMap.spawn)
        }

        if (getRemainingRounds() == 0) {
            end(winner)
        }else {
            startRound()
        }
    }

    override fun end(winner: EventPlayer?) {
        sendMessage("${CC.GREEN}${if (winner != null) winner.player.name else "N/A"} won the event!")
        players.forEach {
            val profile = Profile.getByUUID(it.uuid)

            profile?.state = ProfileState.LOBBY
            Hotbar.giveHotbar(profile!!)
        }
    }
}
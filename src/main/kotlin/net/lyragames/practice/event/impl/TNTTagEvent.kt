package net.lyragames.practice.event.impl

import net.lyragames.practice.event.Event
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.player.EventPlayer
import net.lyragames.practice.event.player.EventPlayerState
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.countdown.Countdown
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ThreadLocalRandom


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/30/2022
 * Project: lPractice
 */

class TNTTagEvent(host: UUID, eventMap: EventMap) : Event(host, eventMap) {

    override fun startRound() {
        state = EventState.STARTING

        playingPlayers = getNextPlayers()

        for (eventPlayer in playingPlayers) {
            eventPlayer.state = EventPlayerState.FIGHTING

            PlayerUtil.reset(eventPlayer.player)
            eventPlayer.player.teleport(eventMap.spawn)
        }

        val tagger = players[ThreadLocalRandom.current().nextInt(players.size)]

        Bukkit.getScheduler().runTaskLater(PracticePlugin.instance, {
            tagger.tagged = true
            tagger.player.inventory.helmet = ItemStack(Material.TNT)

            for (x in 0 until 36) {
                tagger.player.inventory.setItem(x, ItemStack(Material.TNT))
            }
            tagger.player.updateInventory()

            sendMessage("${CC.SECONDARY}${tagger.player.name}${CC.PRIMARY} is the tagger!")
        }, 6 * 20L)

        for (eventPlayer in playingPlayers) {
            if (eventPlayer.offline) continue

            countdowns.add(Countdown(
                eventPlayer.player,
                "&aRound $round starting in <seconds> seconds!",
                6
            ) {
                eventPlayer.player.sendMessage("${CC.GREEN}Round started!")
                state = EventState.FIGHTING

                started = System.currentTimeMillis()
            })
        }
    }

    override fun endRound(winner: EventPlayer?) {
        state = EventState.ENDING

        for (eventPlayer in playingPlayers.filter { it.offline || it.dead || it.tagged }) {
            eventPlayer.dead = true

            eventPlayer.player.allowFlight = true
            eventPlayer.player.isFlying = true

            players.forEach {
                if (it.offline) return@forEach

                it.player.playEffect(eventPlayer.player.location, Effect.EXPLOSION_HUGE, 1000)
                it.player.hidePlayer(eventPlayer.player)
            }

            Hotbar.giveHotbar(PracticePlugin.instance.profileManager.findById(eventPlayer.uuid)!!)
        }

        Bukkit.getScheduler().runTaskLater(PracticePlugin.instance, {
            if (getRemainingRounds() == 1) {
                end(playingPlayers.first { !it.offline && !it.dead && !it.tagged })
            }else {
                round++
                startRound()
            }
        }, 40L)
    }

    override fun handleDisconnect(eventPlayer: EventPlayer) {
        sendMessage("${CC.SECONDARY}${eventPlayer.name}${CC.PRIMARY} disconnected.")

        eventPlayer.dead = true
        eventPlayer.offline = true

        if (eventPlayer.tagged || getAlivePlayers().size < 2) {
            endRound(null)
        }
    }

    override fun getRemainingRounds(): Int {
        return players.filter { !it.offline && !it.dead && !it.tagged}.count()
    }

    override fun getNextPlayers(): MutableList<EventPlayer> {
        return players.filter { !it.offline && !it.dead && !it.tagged }.toMutableList()
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
}
package net.lyragames.practice.event.impl

import net.lyragames.llib.utils.CC
import net.lyragames.llib.utils.Countdown
import net.lyragames.llib.utils.PlayerUtil
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.Event
import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.player.EventPlayer
import net.lyragames.practice.event.player.EventPlayerState
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.hotbar.Hotbar
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

        for (eventPlayer in playingPlayers) {
            if (eventPlayer.offline) continue

            Countdown(
                PracticePlugin.instance,
                eventPlayer.player,
                "&aGame starting in <seconds> seconds!",
                6
            ) {
                eventPlayer.player.sendMessage("${CC.GREEN}Game started!")
                state = EventState.FIGHTING


            }
        }

        Bukkit.getScheduler().runTaskLater(PracticePlugin.instance, {
            val tagger = players[ThreadLocalRandom.current().nextInt(players.size)]
            tagger.tagged = true
            tagger.player.inventory.helmet = ItemStack(Material.TNT)

            for (x in 0 until 35) {
                tagger.player.inventory.setItem(x, ItemStack(Material.TNT))
            }
            tagger.player.updateInventory()

            sendMessage("${CC.SECONDARY}${tagger.player.name}${CC.PRIMARY} is the tagger!")
        }, 6 * 20L)
    }

    override fun endRound(winner: EventPlayer?) {
        state = EventState.ENDING

        for (eventPlayer in playingPlayers.filter { it.offline || it.dead || it.tagged }) {
            eventPlayer.dead = true

            eventPlayer.player.allowFlight = true
            eventPlayer.player.isFlying = true

            players.forEach {
                if (it.player != null) {
                    it.player.playEffect(eventPlayer.player.location, Effect.EXPLOSION_HUGE, 1000)
                    it.player.hidePlayer(eventPlayer.player)
                }
            }

            Hotbar.giveHotbar(Profile.getByUUID(eventPlayer.uuid)!!)
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

    override fun getRemainingRounds(): Int {
        return players.filter { !it.offline && !it.dead && !it.tagged}.count()
    }

    override fun getNextPlayers(): MutableList<EventPlayer> {
        return players.filter { !it.offline && !it.dead && !it.tagged }.toMutableList()
    }

    override fun end(winner: EventPlayer?) {
        Bukkit.broadcastMessage("${CC.GREEN}${if (winner != null) winner.player.name else "N/A"} won the event!")
        players.forEach {
            forceRemove(it.player)
        }

        EventManager.event = null
    }
}
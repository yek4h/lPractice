package net.lyragames.practice.match.impl

import net.lyragames.practice.Locale
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.match.MatchState
import net.lyragames.practice.match.player.MatchPlayer
import net.lyragames.practice.match.player.TeamMatchPlayer
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.LocationHelper
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.countdown.TitleCountdown
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/29/2022
 * Project: lPractice
 */

class MLGRushMatch(kit: Kit, arena: Arena, ranked: Boolean) : TeamMatch(kit, arena, ranked) {

    private var round = 1

    fun handleBreak(event: BlockBreakEvent) {

        if (matchState != MatchState.FIGHTING) {
            event.isCancelled = true
            return
        }

        val player = getMatchPlayer(event.player.uniqueId)

        if (player?.dead!! || player.respawning) {
            event.isCancelled = true
            return
        }

        if (blocksPlaced.contains(event.block)) {
            blocksPlaced.remove(event.block)
        } else {
            event.isCancelled = true
        }

        if (event.block.type == Material.BED || event.block.type == Material.BED_BLOCK) {
            if (player.bedLocations.contains(event.block.location)) {
                player.player.sendMessage("${CC.RED}You cannot break your own bed.")
                event.isCancelled = true
                return
            }

            for (matchPlayer in players) {
                if (matchPlayer.bedLocations.contains(event.block.location)) {
                    event.isCancelled = true
                    player.points++
                    resetMatch(player as TeamMatchPlayer)
                }
            }
        }
    }

    override fun addPlayer(player: Player, location: Location) {
        val team = findTeam()
        val elo = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!!!.getKitStatistic(kit.name)!!.elo

        val teamMatchPlayer = TeamMatchPlayer(player.uniqueId, player.name, team?.spawn!!, team.uuid, elo)

        val blue = team.name == "Blue"

        teamMatchPlayer.coloredName = if (blue) "${CC.BLUE}${player.name}" else "${CC.RED}${player.name}"
        teamMatchPlayer.bedLocations = LocationHelper.findBedLocations(team.bedLocation!!)

        team.players.add(teamMatchPlayer)

        players.stream().map { it.player }.forEach {
            it.showPlayer(player)
            player.showPlayer(it)
        }
        players.add(teamMatchPlayer)
    }

    override fun handleDeath(player: MatchPlayer) {
        if (player.offline) {
            sendMessage(Locale.PLAYER_DISCONNECTED.getMessage().replace("<player>", player.name))
        } else if (player.lastDamager == null && !player.offline) {
            sendMessage(Locale.PLAYER_DIED.getMessage())
        } else {
            val matchPlayer = getMatchPlayer(player.lastDamager!!)

            sendMessage(Locale.PLAYED_KILLED.getMessage().replace("<player>", player.name).replace("<killer>", matchPlayer!!.name))
        }

        player.respawning = true

        PlayerUtil.reset(player.player)
        player.player.gameMode = GameMode.SPECTATOR

        player.player.allowFlight = true
        player.player.isFlying = true

        players.stream().forEach { if (it.player != null) it.player.hidePlayer(player.player) }

        player.player.teleport(player.spawn.clone().add(0.0, 5.0, 0.0))

        val countdown = TitleCountdown(
            player.player,
            "${CC.PRIMARY}Respawning in ${CC.SECONDARY}<seconds>${CC.PRIMARY}!",
            "${CC.RED}YOU DIED!",
            "${CC.YELLOW}Respawning in ${CC.SECONDARY}<seconds>${CC.PRIMARY}...",
            4) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uuid)
            player.player.teleport(player.spawn)

            PlayerUtil.reset(player.player)

            profile?.getKitStatistic(kit.name)?.generateBooks(player.player)

            player.respawning = false
            player.respawnCountdown = null

            players.stream().forEach { if (!it.offline) it.player.showPlayer(player.player) }
        }

        countdowns.add(countdown)
        player.respawnCountdown = countdown
    }

    private fun resetMatch(winner: TeamMatchPlayer) {
        if (winner.points >= 5) {
            end(getOpponent(winner.uuid) as TeamMatchPlayer)
        }else {
            round++

            countdowns.forEach { it.cancel() }

            reset()
            matchState = MatchState.STARTING

            for (matchPlayer in players) {
                if (matchPlayer.offline) continue

                if (matchPlayer.respawnCountdown != null) {
                    matchPlayer.respawnCountdown?.cancel()
                    matchPlayer.respawnCountdown = null
                }

                val player = matchPlayer.player
                val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

                PlayerUtil.reset(player)
                PlayerUtil.denyMovement(player)

                player.teleport(matchPlayer.spawn)
                profile?.getKitStatistic(kit.name)?.generateBooks(player)

                countdowns.add(TitleCountdown(
                    player,
                    "${CC.SECONDARY}<seconds>${CC.PRIMARY}...",
                    "${CC.SECONDARY}<seconds>",
                    null,
                    6
                ) {
                    player.sendMessage("${CC.PRIMARY}Round started!")
                    matchState = MatchState.FIGHTING
                    PlayerUtil.allowMovement(player)
                })
            }

        }
    }

    private fun end(player: TeamMatchPlayer) {
        end(getTeam(player.teamUniqueId)!!.players.map { getMatchPlayer(it.uuid)!! }.toMutableList())
    }

    override fun handleQuit(matchPlayer: MatchPlayer) {
        matchPlayer.offline = true

       // handleDeath(matchPlayer)

        if (teams.any { team -> team.players.none { !it.offline } }) {
            end(players.firstOrNull { !it.offline } as TeamMatchPlayer)
        }
    }
}
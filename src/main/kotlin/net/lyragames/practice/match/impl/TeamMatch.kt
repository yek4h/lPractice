package net.lyragames.practice.match.impl

import com.google.common.base.Joiner
import net.lyragames.practice.Locale
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.player.MatchPlayer
import net.lyragames.practice.match.player.TeamMatchPlayer
import net.lyragames.practice.match.snapshot.MatchSnapshot
import net.lyragames.practice.match.team.Team
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Collectors

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/12/2022
 * Project: lPractice
 */

open class TeamMatch(kit: Kit, arena: Arena, ranked: Boolean) : Match(kit, arena, ranked) {

    val teams: MutableList<Team> = mutableListOf()

    init {
        val team1 = Team("Red")
        team1.spawn = arena.l1

        val team2 = Team("Blue")
        team2.spawn = arena.l2

        teams.add(team1)
        teams.add(team2)
    }

    override fun handleDeath(player: MatchPlayer) {
        player.dead = true

        if (player.lastDamager == null) {
            sendMessage(Locale.PLAYER_DIED.getMessage().replace("<player>", player.coloredName))
        }else {
            val matchPlayer = getMatchPlayer(player.lastDamager!!)

            sendMessage(Locale.PLAYED_KILLED.getMessage().replace("<player>", player.coloredName).replace("<killer>", matchPlayer!!.name))
        }

        val losingTeam = teams.stream().filter { team -> !team.players.stream().anyMatch { !it.dead } }.findAny().orElse(null)

        if (losingTeam != null) {
            //ending
            losingTeam.players.map { getMatchPlayer(it.uuid)!! }.toMutableList().let { end(it) }
        }else {
            //not ending
            val bukkitPlayer = player.player

            val snapshot = MatchSnapshot(bukkitPlayer, true)
            snapshot.potionsThrown = player.potionsThrown
            snapshot.potionsMissed = player.potionsMissed
            snapshot.longestCombo = player.longestCombo

            PlayerUtil.reset(bukkitPlayer)

            snapshots.add(snapshot)

            bukkitPlayer.allowFlight = true
            bukkitPlayer.isFlying = true

            val location = bukkitPlayer.location
            bukkitPlayer.teleport(location.add(0.0, 4.0, 0.0))

            snapshot.contents.forEach {
                if (it == null || it.type == Material.AIR) return@forEach

                droppedItems.add(location.world.dropItemNaturally(location, it))
            }

            snapshot.armor.forEach {
                if (it == null || it.type == Material.AIR) return@forEach

                droppedItems.add(location.world.dropItemNaturally(location, it))
            }

            players.stream().map { it.player }
                .forEach {
                    it.hidePlayer(bukkitPlayer)
                }
        }
    }

    fun end(winner: Team) {
        val oppositeTeam = getOpponentTeam(winner)

        end(oppositeTeam!!.players.map { it }.toMutableList())
    }

    override fun canHit(player: Player, target: Player): Boolean {
        val teamMatchPlayer = getMatchPlayer(player.uniqueId) as TeamMatchPlayer
        val teamMatchPlayer1 = getMatchPlayer(target.uniqueId) as TeamMatchPlayer

        return teamMatchPlayer.teamUniqueId != teamMatchPlayer1.teamUniqueId
    }

    fun findTeam(): Team? {
        //return teams[ThreadLocalRandom.current().nextInt(teams.size)]
        return teams.stream().sorted { o1, o2 -> o1.players.size - o2.players.size }
            .findFirst().orElse(null)
    }

    fun getTeamByPlayer(uuid: UUID): Team? {
        return teams.first { team -> team.players.any { it.uuid == uuid } }
    }

    fun getTeam(uuid: UUID): Team? {
        return teams.stream().filter { it.uuid == uuid }
            .findFirst().orElse(null)
    }

    fun getOpponentTeam(team: Team): Team? {
        return teams.stream().filter { it.uuid != team.uuid }
            .findFirst().orElse(null)
    }

    override fun addPlayer(player: Player, location: Location) {
        val team = findTeam()
        val elo = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!!!.getKitStatistic(kit.name)!!.elo

        val teamMatchPlayer = TeamMatchPlayer(player.uniqueId, player.name, team?.spawn!!, team.uuid, elo)

        val blue = team.name == "Blue"

        teamMatchPlayer.coloredName = if (blue) "${CC.BLUE}${player.name}" else "${CC.RED}${player.name}"

        team.players.add(teamMatchPlayer)

        players.stream().map { it.player }.forEach {
            it.showPlayer(player)
            player.showPlayer(it)
        }
        players.add(teamMatchPlayer)
    }

    fun addPlayer(player: Player, team: Team) {
        val elo = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!!!.getKitStatistic(kit.name)!!.elo

        val teamMatchPlayer = TeamMatchPlayer(player.uniqueId, player.name, team.spawn!!, team.uuid, elo)

        team.players.add(teamMatchPlayer)

        players.stream().map { it.player }.forEach {
            it.showPlayer(player)
            player.showPlayer(it)
        }
        players.add(teamMatchPlayer)
    }

    override fun getOpponentString(uuid: UUID): String? {
        val team = getTeam((getMatchPlayer(uuid) as TeamMatchPlayer).teamUniqueId)
        val opponentTeam = getOpponentTeam(team!!)

        return Joiner.on(", ").join(opponentTeam!!.players.stream().map { if (isAlivePlayer(it.uuid)) it.name else "&8&m&o${it.name}" }.collect(Collectors.toList()))
    }

    fun isAlivePlayer(uuid: UUID): Boolean {
        players.filter {
            uuid == it.uuid
        }.filterNot {
            return it.dead
        }

        return false
    }

    fun getPlayerString(uuid: UUID): String? {
        val team = getTeam((getMatchPlayer(uuid) as TeamMatchPlayer).teamUniqueId)

        return Joiner.on(", ").join(team!!.players.stream().map { it.name }.collect(Collectors.toList()))
    }
}
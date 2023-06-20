package net.lyragames.practice.manager

import net.lyragames.llib.utils.CC
import net.lyragames.llib.utils.PlayerUtil
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.impl.bedwars.BedWarsArena
import net.lyragames.practice.arena.impl.bedwars.StandaloneBedWarsArena
import net.lyragames.practice.arena.impl.bridge.BridgeArena
import net.lyragames.practice.arena.impl.bridge.StandaloneBridgeArena
import net.lyragames.practice.arena.impl.mlgrush.MLGRushArena
import net.lyragames.practice.arena.impl.mlgrush.StandaloneMLGRushArena
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.impl.BedFightMatch
import net.lyragames.practice.match.impl.BridgeMatch
import net.lyragames.practice.match.impl.MLGRushMatch
import net.lyragames.practice.match.impl.TeamMatch
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object MatchManager {

    fun createMatch(kit: Kit, arena: Arena, ranked: Boolean, friendly: Boolean, firstPlayer: Player, secondPlayer: Player): Match {
        var match = Match(kit, arena, ranked)

        if (kit.kitData.mlgRush) {
            match = MLGRushMatch(kit, arena, ranked)
        }else if (kit.kitData.bedFights) {
            match = BedFightMatch(kit, arena, ranked)
        }else if (kit.kitData.bridge) {
            match = BridgeMatch(kit, arena, ranked)
        }

        match.friendly = friendly

        val profile = Profile.getByUUID(firstPlayer.uniqueId)
        val profile1 = Profile.getByUUID(secondPlayer.uniqueId)

        profile?.match = match.uuid
        profile?.matchObject = match
        profile?.state = ProfileState.MATCH

        profile1?.match = match.uuid
        profile1?.matchObject = match
        profile1?.state = ProfileState.MATCH

        if (arena is StandaloneBedWarsArena) {
            match.addPlayer(firstPlayer, arena.blueSpawn!!)
            match.addPlayer(secondPlayer, arena.redSpawn!!)

            match.getMatchPlayer(firstPlayer.uniqueId)?.bed = arena.blueBed
            match.getMatchPlayer(secondPlayer.uniqueId)?.bed = arena.redBed
        }else if (arena is BedWarsArena) {
            match.addPlayer(firstPlayer, arena.blueSpawn!!)
            match.addPlayer(secondPlayer, arena.redSpawn!!)

            match.getMatchPlayer(firstPlayer.uniqueId)?.bed = arena.blueBed
            match.getMatchPlayer(secondPlayer.uniqueId)?.bed = arena.redBed
        } else if (arena is StandaloneMLGRushArena) {
            match.addPlayer(firstPlayer, arena.l1!!)
            match.addPlayer(secondPlayer, arena.l2!!)

            match.getMatchPlayer(firstPlayer.uniqueId)?.bed = arena.bed1
            match.getMatchPlayer(secondPlayer.uniqueId)?.bed = arena.bed2
        }else if (arena is MLGRushArena) {
            match.addPlayer(firstPlayer, arena.l1!!)
            match.addPlayer(secondPlayer, arena.l2!!)

            match.getMatchPlayer(firstPlayer.uniqueId)?.bed = arena.bed1
            match.getMatchPlayer(secondPlayer.uniqueId)?.bed = arena.bed2
        }else if (arena is BridgeArena) {
            match.addPlayer(firstPlayer, arena.blueSpawn!!)
            match.addPlayer(secondPlayer, arena.redSpawn!!)
        }else if (arena is StandaloneBridgeArena) {
            match.addPlayer(firstPlayer, arena.blueSpawn!!)
            match.addPlayer(secondPlayer, arena.redSpawn!!)
        } else {
            match.addPlayer(firstPlayer, arena.l1!!)
            match.addPlayer(secondPlayer, arena.l2!!)
        }

        if (!friendly) {
            generateMessage(firstPlayer, secondPlayer, ranked, arena, kit)
        }

        Match.matches.add(match)

        match.start()

        return match
    }

    private fun generateMessage(firstPlayer: Player, secondPlayer: Player, ranked: Boolean, arena: Arena, kit: Kit) {
        firstPlayer.sendMessage(" ")
        secondPlayer.sendMessage(" ")

        firstPlayer.sendMessage("${CC.PRIMARY}${CC.BOLD}${if (ranked) "Ranked" else "Unranked"} Match")
        secondPlayer.sendMessage("${CC.PRIMARY}${CC.BOLD}${if (ranked) "Ranked" else "Unranked"} Match")

        firstPlayer.sendMessage("${CC.PRIMARY} ⚫ Map: ${CC.SECONDARY}${arena.name}")
        firstPlayer.sendMessage("${CC.PRIMARY} ⚫ Opponent: ${CC.RED}${secondPlayer.name}")
        firstPlayer.sendMessage("${CC.PRIMARY} ⚫ Ping: ${CC.RED}${PlayerUtil.getPing(secondPlayer)} ms")

        secondPlayer.sendMessage("${CC.PRIMARY} ⚫ Map: ${CC.SECONDARY}${arena.name}")
        secondPlayer.sendMessage("${CC.PRIMARY} ⚫ Opponent: ${CC.RED}${firstPlayer.name}")
        secondPlayer.sendMessage("${CC.PRIMARY} ⚫ Ping: ${CC.RED}${PlayerUtil.getPing(firstPlayer)} ms")

        if (ranked) {
            val profile = Profile.getByUUID(firstPlayer.uniqueId)
            val profile1 = Profile.getByUUID(secondPlayer.uniqueId)

            secondPlayer.sendMessage("${CC.PRIMARY} ⚫ ELO: ${CC.SECONDARY}${profile?.getKitStatistic(kit.name)?.elo}")
            firstPlayer.sendMessage("${CC.PRIMARY} ⚫ ELO: ${CC.SECONDARY}${profile1?.getKitStatistic(kit.name)?.elo}")
        }

        firstPlayer.sendMessage(" ")
        secondPlayer.sendMessage(" ")
    }

    fun createTeamMatch(kit: Kit, arena: Arena, ranked: Boolean, friendly: Boolean, players: MutableList<UUID>) {
        val match = TeamMatch(kit, arena, ranked)
        match.friendly = friendly

        for (uuid in players) {
            val partyPlayer = Bukkit.getPlayer(uuid) ?: continue

            val profile = Profile.getByUUID(uuid)

            profile!!.match = match.uuid
            profile.matchObject = match
            profile.state = ProfileState.MATCH

            match.addPlayer(partyPlayer, arena.l1!!)
        }

        Match.matches.add(match)

        match.start()
    }

    fun createTeamMatch(kit: Kit, arena: Arena, ranked: Boolean, friendly: Boolean, firstTeam: MutableList<UUID>, secondTeam: MutableList<UUID>) {
        val match = TeamMatch(kit, arena, ranked)
        match.friendly = friendly

        val team1 = match.teams[0]
        val team2 = match.teams[1]

        for (uuid in firstTeam) {
            val profileParty = Profile.getByUUID(uuid)

            profileParty?.state = ProfileState.MATCH
            profileParty?.match = match.uuid
            profileParty?.matchObject = match

            match.addPlayer(Bukkit.getPlayer(uuid), team1)
        }

        for (uuid in secondTeam) {
            val profileParty = Profile.getByUUID(uuid)

            profileParty?.state = ProfileState.MATCH
            profileParty?.match = match.uuid
            profileParty?.matchObject = match

            match.addPlayer(Bukkit.getPlayer(uuid), team2)
        }

        Match.matches.add(match)
        match.start()
    }
}
package net.lyragames.practice.adapter

import dev.yair.deboy.ScoreInterface
import dev.yair.deboy.annotations.*
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.event.EventType
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.manager.FFAManager
import net.lyragames.practice.manager.PartyManager
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.MatchState
import net.lyragames.practice.match.MatchType
import net.lyragames.practice.match.impl.BedFightMatch
import net.lyragames.practice.match.impl.BridgeMatch
import net.lyragames.practice.match.impl.MLGRushMatch
import net.lyragames.practice.match.impl.TeamMatch
import net.lyragames.practice.match.player.TeamMatchPlayer
import net.lyragames.practice.party.PartyMatchType
import net.lyragames.practice.party.PartyType
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ConfigFile
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.TimeUtil
import okhttp3.MultipartBody.Part
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.stream.Collectors


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 15/06/2024
*/


@Async(true)
@TickUpdate(2L)
class ScoreboardAdapter(private val configFile: ConfigFile) : ScoreInterface {

    override fun getTitle(player: Player): String {
        if (configFile.getString("scoreboard.title").contains("ANIMATED-TEXT")) {
            return PracticePlugin.instance.animatedTextManagerTitle.getText()
        }
        return CC.translate(configFile.getString("scoreboard.title"))
    }

    override fun getLines(player: Player): List<String> {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (!profile.settings.scoreboard) {
            return emptyList()
        }

        return when (profile.state) {
            ProfileState.LOBBY -> getLobbyLines(player)
            ProfileState.MATCH -> getMatchLines(player, profile)
            ProfileState.QUEUE -> getQueueLines(profile)
            ProfileState.FFA -> getFFALines(player, profile)
            ProfileState.EVENT -> getEventLines(player)
            ProfileState.SPECTATING -> getSpectateLines(player, profile)
        }
    }

    private fun getLobbyLines(player: Player): MutableList<String> {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val party = profile.party?.let { PartyManager.getByUUID(it) }
        return if (profile.party != null) {
            configFile.getStringList("scoreboard.party.lobby").stream()
                .map { CC.translate(it.replacePlaceholders()
                    .replace("<partyLeader>", Bukkit.getPlayer(party!!.leader).name)
                    .replace("<status>", if (party.partyType == PartyType.PRIVATE) "&cLocked ✘" else "&aOpen ✔")
                    .replace("<partyPlayers>", party.players.size.toString())) }
                .collect(Collectors.toList())
        } else {
            configFile.getStringList("scoreboard.lobby").stream()
                .map { CC.translate(it.replacePlaceholders()) }
                .collect(Collectors.toList())
        }
    }

    private fun getMatchLines(player: Player, profile: Profile): MutableList<String> {
        val match = profile.match?.let { Match.getByUUID(it) } ?: return mutableListOf()
        return when {
            match is MLGRushMatch -> getMLGRushLines(player, match)
            match is BridgeMatch -> getBridgeLines(player, match)
            match is BedFightMatch -> getBedFightLines(player, match)
            match.kit.boxing -> getBoxingLines(player, match)
            match is TeamMatch -> getTeamMatchLines(player, match)
            else -> getDefaultMatchLines(player, match)
        }
    }

    private fun getQueueLines(profile: Profile): MutableList<String> {
        val queuePlayer = profile.queuePlayer ?: return mutableListOf()
        val queueType = queuePlayer.queue.type
        val queueSection = when (queueType) {
            QueueType.UNRANKED -> "scoreboard.queue.unranked"
            QueueType.RANKED -> "scoreboard.queue.ranked"
        }

        return configFile.getStringList(queueSection).stream()
            .map {
                CC.translate(
                    it.replacePlaceholders()
                        .replace("<queue_kit>", queuePlayer.queue.kit.name)
                        .replace("<queue_type>", queueType.displayName)
                        .replace("<min>", queuePlayer.getMinRange().toString())
                        .replace("<max>", queuePlayer.getMaxRange().toString())
                        .replace("<time>", TimeUtil.millisToTimer(System.currentTimeMillis() - queuePlayer.started))
                )
            }
            .collect(Collectors.toList())
    }

    private fun getFFALines(player: Player, profile: Profile): MutableList<String> {
        val ffa = profile.ffa?.let { FFAManager.getByUUID(it) } ?: return getLobbyLines(player)
        val ffaPlayer = ffa.getFFAPlayer(player.uniqueId)
        return configFile.getStringList("scoreboard.ffa").stream()
            .map {
                CC.translate(
                    it.replacePlaceholders()
                        .replace("<kit>", ffa.kit.name)
                        .replace("<kills>", ffaPlayer.kills.toString())
                        .replace("<killstreak>", ffaPlayer.killStreak.toString())
                        .replace("<deaths>", ffaPlayer.death.toString())
                        .replace("<ping>", PlayerUtil.getPing(player).toString())
                )
            }
            .collect(Collectors.toList())
    }

    private fun getEventLines(player: Player): MutableList<String> {
        val event = EventManager.event ?: return getLobbyLines(player)
        return if (event.type == EventType.TNT_TAG || event.type == EventType.TNT_RUN) {
            configFile.getStringList("scoreboard.ffa-event").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<state>", event.state.stateName)
                            .replace("<type>", event.type.eventName)
                            .replace("<remainingPlayers>", event.getAlivePlayers().size.toString())
                    )
                }
                .collect(Collectors.toList())
        } else {
            configFile.getStringList("scoreboard.event").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<state>", event.state.stateName)
                            .replace("<type>", event.type.eventName)
                            .replace("<playing1>", event.playingPlayers.getOrNull(0)?.player?.name ?: "N/A")
                            .replace("<playing2>", event.playingPlayers.getOrNull(1)?.player?.name ?: "N/A")
                    )
                }
                .collect(Collectors.toList())
        }
    }

    private fun getSpectateLines(player: Player, profile: Profile): MutableList<String> {
        val match = profile.spectatingMatch?.let { Match.getByUUID(it) } ?: return getLobbyLines(player)
        return if (match.getMatchType() == MatchType.TEAM) {
            val randPlayer = (match as TeamMatch).players[0]
            configFile.getStringList("scoreboard.spectate").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<kit>", match.kit.name)
                            .replace("<time>", match.getTime())
                            .replace("<team1>", match.getPlayerString(randPlayer.uuid) ?: "")
                            .replace("<team2>", match.getOpponentString(randPlayer.uuid) ?: "")
                    )
                }
                .collect(Collectors.toList())
        } else {
            configFile.getStringList("scoreboard.spectate").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<kit>", match.kit.name)
                            .replace("<time>", match.getTime())
                            .replace("<team1>", match.players[0].name)
                            .replace("<team2>", match.players[1].name)
                    )
                }
                .collect(Collectors.toList())
        }
    }

    private fun getMLGRushLines(player: Player, match: MLGRushMatch): MutableList<String> {
        val matchPlayer = match.getMatchPlayer(player.uniqueId) as TeamMatchPlayer
        val team = match.getTeam(matchPlayer.teamUniqueId)
        val opponentTeam = match.getOpponentTeam(team!!)
        val points = team.players.sumOf { it.points }
        val opponentPoints = opponentTeam!!.players.sumOf { it.points }
        val symbol = "⬤"

        return configFile.getStringList("scoreboard.mlgrush").stream()
            .map {
                CC.translate(
                    it.replacePlaceholders()
                        .replace("<opponent>", match.getOpponentString(player.uniqueId)!!)
                        .replace("<kit>", match.kit.name)
                        .replace("<time>", match.getTime())
                        .replace("<points>", StringUtils.repeat("${CC.GREEN}$symbol", points) + StringUtils.repeat("${CC.GRAY}$symbol", 5 - points))
                        .replace("<opponent_points>", StringUtils.repeat("${CC.GREEN}$symbol", opponentPoints) + StringUtils.repeat("${CC.GRAY}$symbol", 5 - opponentPoints))
                )
            }
            .collect(Collectors.toList())
    }

    private fun getBridgeLines(player: Player, match: BridgeMatch): MutableList<String> {
        val matchPlayer = match.getMatchPlayer(player.uniqueId) as TeamMatchPlayer
        val team = match.getTeam(matchPlayer.teamUniqueId)
        val opponentTeam = match.getOpponentTeam(team!!)
        val points = team.points
        val opponentPoints = opponentTeam!!.points
        val symbol = "⬤"

        return configFile.getStringList("scoreboard.bridge").stream()
            .map {
                CC.translate(
                    it.replacePlaceholders()
                        .replace("<opponent>", match.getOpponentString(player.uniqueId)!!)
                        .replace("<kit>", match.kit.name)
                        .replace("<time>", match.getTime())
                        .replace("<points>", StringUtils.repeat("${CC.GREEN}$symbol", points) + StringUtils.repeat("${CC.GRAY}$symbol", 5 - points))
                        .replace("<opponent_points>", StringUtils.repeat("${CC.GREEN}$symbol", opponentPoints) + StringUtils.repeat("${CC.GRAY}$symbol", 5 - opponentPoints))
                )
            }
            .collect(Collectors.toList())
    }

    private fun getBedFightLines(player: Player, match: BedFightMatch): MutableList<String> {
        val alive = "✔"
        val matchPlayer = match.getMatchPlayer(player.uniqueId) as TeamMatchPlayer
        val team = match.getTeam(matchPlayer.teamUniqueId)
        val red = match.teams.find { it.name.equals("Red", true) }?.let { "${if (it.broken) "${CC.RED}${it.alivePlayers()}" else "${CC.GREEN}$alive"} ${if (it.uuid == team!!.uuid) "${CC.GRAY}YOU" else ""}" } ?: ""
        val blue = match.teams.find { it.name.equals("Blue", true) }?.let { "${if (it.broken) "${CC.BLUE}${it.alivePlayers()}" else "${CC.GREEN}$alive"} ${if (it.uuid == team!!.uuid) "${CC.GRAY}YOU" else ""}" } ?: ""

        return configFile.getStringList("scoreboard.bedfight").stream()
            .map {
                CC.translate(
                    it.replacePlaceholders()
                        .replace("<opponent>", match.getOpponentString(player.uniqueId)!!)
                        .replace("<kit>", match.kit.name)
                        .replace("<time>", match.getTime())
                        .replace("<red>", red)
                        .replace("<blue>", blue)
                )
            }
            .collect(Collectors.toList())
    }

    private fun getBoxingLines(player: Player, match: Match): MutableList<String> {
        val matchPlayer = match.getMatchPlayer(player.uniqueId)

        return if (match is TeamMatch) {
            val team = match.getTeam((matchPlayer as TeamMatchPlayer).teamUniqueId)
            val opponentTeam = match.getOpponentTeam(team!!)

            configFile.getStringList("scoreboard.boxing").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<opponent>", match.getOpponentString(player.uniqueId)!!)
                            .replace("<kit>", match.kit.name)
                            .replace("<hits>", team.hits.toString())
                            .replace("<opponent-hits>", match.getOpponentTeam(team)!!.hits.toString())
                            .replace("<diff>", if (team.hits == opponentTeam!!.hits) "" else if (team.hits < opponentTeam.hits) "${CC.RED}(-${opponentTeam.hits - team.hits})" else "${CC.GREEN}(+${team.hits - opponentTeam.hits})")
                            .replace("<time>", match.getTime())
                    )
                }
                .collect(Collectors.toList())
        } else {
            val opponent = match.getOpponent(matchPlayer!!.uuid)

            configFile.getStringList("scoreboard.boxing").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<opponent>", match.getOpponentString(player.uniqueId)!!)
                            .replace("<kit>", match.kit.name)
                            .replace("<hits>", matchPlayer.hits.toString())
                            .replace("<opponent-hits>", match.getOpponent(player.uniqueId)!!.hits.toString())
                            .replace("<diff>", if (opponent!!.hits == matchPlayer.hits) "" else if (matchPlayer.hits < opponent.hits) "${CC.RED}(-${opponent.hits - matchPlayer.hits})" else "${CC.GREEN}(+${matchPlayer.hits - opponent.hits})")
                            .replace("<time>", match.getTime())
                    )
                }
                .collect(Collectors.toList())
        }
    }

    private fun getTeamMatchLines(player: Player, match: TeamMatch): MutableList<String> {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)
        val party = PartyManager.getByUUID(profile!!.party!!)

        if (party != null) {
            return configFile.getStringList("scoreboard.party.split.match").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<opponents>", match.getOpponentString(player.uniqueId)!!)
                            .replace("<kit>", match.kit.name)
                            .replace("<time>", match.getTime())
                    )
                }
                .collect(Collectors.toList())
        } else {
            return configFile.getStringList("scoreboard.match").stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<opponent>", match.getOpponentString(player.uniqueId)!!)
                            .replace("<kit>", match.kit.name)
                            .replace("<time>", match.getTime())
                    )
                }
                .collect(Collectors.toList())
        }
    }

    private fun getDefaultMatchLines(player: Player, match: Match): MutableList<String> {
        val matchType = match.matchState
        var matchSection: String
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)
        val party = profile!!.party?.let { PartyManager.getByUUID(it) }

        if (party != null) {
            matchSection = when (matchType) {
                MatchState.STARTING -> "scoreboard.party.match.starting"
                MatchState.FIGHTING -> "scoreboard.party.match.ffa.fighting"
                MatchState.ENDING -> "scoreboard.party.match.ending"
            }

            val opponentName = match.getOpponentString(player.uniqueId)
            val opponent = if (opponentName != null) Bukkit.getPlayer(opponentName) else null
            val playerPing = (player as? CraftPlayer)?.handle?.ping?.toString() ?: "N/A"
            val opponentPing = if (opponent != null && opponent is CraftPlayer) {
                opponent.handle.ping.toString()
            } else {
                "N/A"
            }

            return configFile.getStringList(matchSection).stream()
                .map {
                    CC.translate(
                        it.replacePlaceholders()
                            .replace("<enemys>", match.getAlivePlayers().joinToString(", "))
                            .replace("<player>", player.name)
                            .replace("<opponent>", opponentName ?: "Unknown")
                            .replace("<kit>", match.kit.name)
                            .replace("<time>", match.getTime())
                            .replace("<ping>", playerPing)
                            .replace("<opponent_ping>", opponentPing)
                    )
                }
                .collect(Collectors.toList())
        } else {
            matchSection = when (matchType) {
                MatchState.STARTING -> "scoreboard.match.starting"
                MatchState.FIGHTING -> "scoreboard.match.fighting"
                MatchState.ENDING -> "scoreboard.match.ending"
            }
        }

        val opponentName = match.getOpponentString(player.uniqueId)
        val opponent = if (opponentName != null) Bukkit.getPlayer(opponentName) else null
        val playerPing = (player as? CraftPlayer)?.handle?.ping?.toString() ?: "N/A"
        val opponentPing = if (opponent != null && opponent is CraftPlayer) {
            opponent.handle.ping.toString()
        } else {
            "N/A"
        }

        return configFile.getStringList(matchSection).stream()
            .map {
                CC.translate(
                    it.replacePlaceholders()
                        .replace("<player>", player.name)
                        .replace("<opponent>", opponentName ?: "Unknown")
                        .replace("<kit>", match.kit.name)
                        .replace("<time>", match.getTime())
                        .replace("<ping>", playerPing)
                        .replace("<opponent_ping>", opponentPing)
                )
            }
            .collect(Collectors.toList())
    }

    private fun String.replacePlaceholders(): String {
        return this.replace("<online>", Bukkit.getOnlinePlayers().size.toString())
            .replace("<queuing>", QueueManager.getTotalQueueingPlayers().toString())
            .replace("<in_match>", Match.inMatch().toString())
            .replace("<footer>", PracticePlugin.instance.animatedTextManagerFooter.getText())
            .replace("-", " ")
    }
}
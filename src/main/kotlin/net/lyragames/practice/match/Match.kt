package net.lyragames.practice.match

import com.boydti.fawe.bukkit.chat.FancyMessage
import com.google.common.base.Joiner
import net.lyragames.practice.Locale
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.ArenaRatingManager
import net.lyragames.practice.manager.MatchManager
import net.lyragames.practice.manager.StatisticManager
import net.lyragames.practice.match.impl.MLGRushMatch
import net.lyragames.practice.match.impl.TeamMatch
import net.lyragames.practice.match.player.MatchPlayer
import net.lyragames.practice.match.snapshot.MatchSnapshot
import net.lyragames.practice.match.spectator.MatchSpectator
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.TextBuilder
import net.lyragames.practice.utils.TimeUtil
import net.lyragames.practice.utils.countdown.ICountdown
import net.lyragames.practice.utils.countdown.TitleCountdown
import net.lyragames.practice.utils.item.CustomItemStack
import net.lyragames.practice.utils.title.TitleBar
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.collections.HashMap

/**
 * This Project is property of Zowpy © 2021
 * Redistribution of this Project is not allowed
 * Created: 12/19/2021
 * Project: Practice
 *
 * Recoded by yek4h
 *
 */

open class Match(val kit: Kit, val arena: Arena, val ranked: Boolean, var friendly: Boolean = false) {

    val uuid: UUID = UUID.randomUUID()
    var matchState = MatchState.STARTING
    var started = 0L
    val players: MutableList<MatchPlayer> = mutableListOf()
    val blocksPlaced: MutableList<Block> = mutableListOf()
    val droppedItems: MutableList<Item> = mutableListOf()
    val snapshots: MutableList<MatchSnapshot> = mutableListOf()
    val spectators: MutableList<MatchSpectator> = mutableListOf()
    val countdowns: MutableList<ICountdown> = mutableListOf()
    val rematchingPlayers: MutableList<UUID> = mutableListOf()

    data class OldPosition(val profile: Profile, val position: Int)

    open fun start() {
        players.forEach { matchPlayer ->
            if (matchPlayer.offline) return@forEach

            val player = matchPlayer.player
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

            CustomItemStack.customItemStacks.removeIf { it.uuid == matchPlayer.uuid }

            if (kit.boxing || kit.combo) {
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1))
            }

            PlayerUtil.reset(player)
            PlayerUtil.denyMovement(player)
            matchPlayer.player.maximumNoDamageTicks = kit.damageTicks

            player.teleport(matchPlayer.spawn)
            profile.getKitStatistic(kit.name)?.generateBooks(player)

            countdowns.add(
                TitleCountdown(
                    player,
                    "${CC.SECONDARY}<seconds>${CC.PRIMARY}...",
                    "${CC.SECONDARY}<seconds>",
                    null,
                    6
                ) {
                    player.sendMessage("${CC.PRIMARY}Match started!")
                    matchState = MatchState.FIGHTING
                    PlayerUtil.allowMovement(player)
                    started = System.currentTimeMillis()
                }
            )
        }
    }

    open fun getMatchType(): MatchType {
        return when (this) {
            is TeamMatch -> MatchType.TEAM
            is MLGRushMatch -> MatchType.BEDFIGHTS
            else -> MatchType.NORMAL
        }
    }

    open fun addSpectator(player: Player) {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        profile.state = ProfileState.SPECTATING
        profile.spectatingMatch = uuid

        sendMessage(Locale.STARTED_SPECTATING.getMessage().replace("<player>", profile.name!!))

        players.filterNot { it.offline }.forEach {
            player.showPlayer(it.player)
        }

        PlayerUtil.resetSpectator(player)
        player.gameMode = GameMode.CREATIVE
        Hotbar.giveHotbar(profile)

        spectators.add(MatchSpectator(player.uniqueId, player.name))
        Match.spectators[player.uniqueId] = this
    }

    open fun removeSpectator(player: Player) {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)
        if (profile != null)
            sendMessage(Locale.STOPPED_SPECTATING.getMessage().replace("<player>", profile.name!!))
        removeSpec(player)
        Match.spectators.remove(player.uniqueId)
        spectators.removeIf { it.uuid == player.uniqueId }
        forceRemoveSpectator(player)
    }

    private fun removeSpec(player: Player) {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        profile.state = ProfileState.LOBBY
        profile.spectatingMatch = null

        players.filterNot { it.offline }.forEach {
            player.hidePlayer(it.player)
        }

        PlayerUtil.reset(player)
        Hotbar.giveHotbar(profile)

        Constants.SPAWN?.let { player.teleport(it) }
    }

    open fun forceRemoveSpectator(player: Player) {
        removeSpec(player)
        spectators.removeIf { it.uuid == player.uniqueId }
    }

    open fun canHit(player: Player, target: Player): Boolean = true

    open fun addPlayer(player: Player, location: Location) {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return
        val kitStatistic = profile.getKitStatistic(kit.name)
        val matchPlayer = MatchPlayer(player.uniqueId, player.name, location, kitStatistic?.elo ?: 1000)

        players.add(matchPlayer)

        players.filterNot { it.offline }.forEach {
            player.showPlayer(it.player)
            it.player.showPlayer(player)
        }
    }

    fun sendMessage(message: String) {
        val translatedMessage = CC.translate(message)
        players.filterNot { it.offline }.forEach { it.player.sendMessage(translatedMessage) }
        spectators.mapNotNull { Bukkit.getPlayer(it.uuid) }.forEach { it.sendMessage(translatedMessage) }
    }

    fun sendMessage(message: String, permission: String) {
        val translatedMessage = CC.translate(message)
        players.filter { it.player.hasPermission(permission) }.forEach { it.player.sendMessage(translatedMessage) }
        spectators.mapNotNull { Bukkit.getPlayer(it.uuid) }
            .filter { it.hasPermission(permission) }
            .forEach { it.sendMessage(translatedMessage) }
    }

    open fun handleDeath(player: MatchPlayer) {
        player.dead = true

        val deathMessage = when {
            player.offline -> Locale.PLAYER_DISCONNECTED.getMessage().replace("<player>", player.name)
            player.lastDamager == null -> Locale.PLAYER_DIED.getMessage().replace("<player>", player.name)
            else -> {
                val killer = getMatchPlayer(player.lastDamager!!)!!
                Locale.PLAYED_KILLED.getMessage().replace("<killer>", killer.name).replace("<player>", player.name)
            }
        }

        sendMessage(deathMessage)
        end(mutableListOf(player))
    }

    open fun end(losers: MutableList<MatchPlayer>) {
        val oldWinnerPositions = mutableListOf<OldPosition>()
        val oldLoserPositions = mutableListOf<OldPosition>()

        countdowns.forEach { it.cancel() }

        val winners = players.filterNot { losers.contains(it) }.toMutableList()
        matchState = MatchState.ENDING

        val winner = Bukkit.getPlayer(winners[0].uuid)
        val loser = Bukkit.getPlayer(losers[0].uuid)

        val winnerProfile = PracticePlugin.instance.profileManager.findById(winners[0].uuid)!!
        val loserProfile = PracticePlugin.instance.profileManager.findById(losers[0].uuid)!!

        oldWinnerPositions.add(OldPosition(winnerProfile, PracticePlugin.instance.leaderboards.getLeaderboardPosition(winnerProfile, kit)))
        oldLoserPositions.add(OldPosition(loserProfile, PracticePlugin.instance.leaderboards.getLeaderboardPosition(loserProfile, kit)))

        winners.forEach { winner ->
            losers.forEach { loser -> winner.player.hidePlayer(loser.player) }
        }

        players.forEach { matchPlayer ->
            val profile = matchPlayer.profile
            if (matchPlayer !in losers && !friendly) {
                StatisticManager.win(profile, losers[0].profile, kit, ranked)
            } else if (!friendly) {
                StatisticManager.loss(profile, kit, ranked)
            }

            if (matchPlayer.offline) return@forEach

            val bukkitPlayer = matchPlayer.player
            profile.arrowCooldown?.cancel()
            profile.arrowCooldown = null

            profile.enderPearlCooldown?.cancel()
            profile.enderPearlCooldown = null

            val snapshot = MatchSnapshot(bukkitPlayer, matchPlayer.dead).apply {
                potionsThrown = matchPlayer.potionsThrown
                potionsMissed = matchPlayer.potionsMissed
                longestCombo = matchPlayer.longestCombo
                totalHits = matchPlayer.hits
                opponent = getOpponent(bukkitPlayer.uniqueId)?.uuid
            }

            snapshots.add(snapshot)
            PlayerUtil.reset(bukkitPlayer)
            PlayerUtil.allowMovement(bukkitPlayer)

            if (!friendly) {
                MatchManager.createReQueueItem(bukkitPlayer, this)
            }
        }

        snapshots.forEach { snapshot ->
            snapshot.createdAt = System.currentTimeMillis()
            MatchSnapshot.snapshots.add(snapshot)
        }

        sendTitleBar(winners)
        endMessage(winners, losers)

        if (ranked) {
            winnerProfile.save()
            loserProfile.save()

            val oldWinnerPosition = oldWinnerPositions.first { it.profile == winnerProfile }.position
            val oldLoserPosition = oldLoserPositions.first { it.profile == loserProfile }.position

            val newWinnerPosition = PracticePlugin.instance.leaderboards.getLeaderboardPosition(winnerProfile, kit)
            val newLoserPosition = PracticePlugin.instance.leaderboards.getLeaderboardPosition(loserProfile, kit)

            val numsTotalMovedFromTheLeaderboards = oldWinnerPosition - newWinnerPosition
            val losernumsTotalMovedFromTheLeaderboards = oldLoserPosition - newWinnerPosition
            val nextToReach = PracticePlugin.instance.leaderboards.getTopProfilesByKitElo(kit, newWinnerPosition).getOrNull(newWinnerPosition + 1) // El siguiente en la leaderboard

            val eloNeededToReachNextPlayer = nextToReach?.second?.minus(winnerProfile.getKitStatistic(kit.name)?.elo ?: 0) ?: 0
            val nextToReachInLeaderboard = newWinnerPosition - 1
            val nextToReachInLeaderboardName = nextToReach?.first?.name ?: "N/A"

            val messages = createEloMessages(winners, losers, numsTotalMovedFromTheLeaderboards, losernumsTotalMovedFromTheLeaderboards, nextToReachInLeaderboardName, eloNeededToReachNextPlayer, newWinnerPosition, newLoserPosition)

            sendEloMessages(winner, loser, messages)
        }

        QueueManager.updatePlayingCount(this, -players.size)
        Bukkit.getScheduler().runTaskLater(PracticePlugin.instance, {
            resetMatch()
        }, 60L)
    }

    private fun createEloMessages(
        winners: MutableList<MatchPlayer>,
        losers: MutableList<MatchPlayer>,
        numsTotalMovedFromTheLeaderboards: Int,
        losernumsTotalMovedFromTheLeaderboards: Int,
        nextToReachInLeaderboardName: String,
        eloNeededToReachNextPlayer: Int,
        newWinnerPosition: Int,
        newLoserPosition: Int
    ): Pair<List<String>, List<String>> {
        val winnerMsg = listOf(
            "${CC.PRIMARY}ELO updates:",
            "&7 ▕ &a${getCombinedNames(winners, " &7(&a+<elo>&7)")}",
            "&7 ▕ &c${getCombinedNames(losers, " &7(&c<elo>&7)")}",
            "&7  ",
            "${CC.PRIMARY}Leaderboards:",
            "&7 ▕ &fMoved: &a$numsTotalMovedFromTheLeaderboards &7(#$newWinnerPosition)",
            "&7 ▕ &fYou need $eloNeededToReachNextPlayer ELO to reach #$nextToReachInLeaderboardName"
        )

        val loserMsg = listOf(
            "${CC.PRIMARY}ELO updates:",
            "&7 ▕ &a${getCombinedNames(winners, " &7(&a+<elo>&7)")}",
            "&7 ▕ &c${getCombinedNames(losers, " &7(&c<elo>&7)")}",
            "&7  ",
            "${CC.PRIMARY}Leaderboards:",
            "&7 ▕ &fMoved: &c$losernumsTotalMovedFromTheLeaderboards &7(#$newLoserPosition)",
            "&7 ▕ &fYou lost elo, now your position is &c#$newLoserPosition"
        )

        return Pair(winnerMsg, loserMsg)
    }

    private fun sendEloMessages(winner: Player?, loser: Player?, messages: Pair<List<String>, List<String>>) {
        messages.first.forEach { winner?.sendMessage(CC.translate(it)) }
        messages.second.forEach { loser?.sendMessage(CC.translate(it)) }
        players.filterNot { it.offline || it.player == winner || it.player == loser }.forEach {
            val player = it.player
            player.sendMessage("${CC.PRIMARY}ELO Updates: ${CC.GREEN}${getCombinedNames(players, " (+<elo>)")}${CC.GRAY}, ${CC.RED}${getCombinedNames(players, " (-<elo>)")}")
        }
    }

    private fun resetMatch() {
        players.forEach { matchPlayer ->
            if (matchPlayer.offline) return@forEach

            val bukkitPlayer = matchPlayer.player
            val profile = PracticePlugin.instance.profileManager.findById(matchPlayer.uuid)!!

            players.filterNot { it.offline }.forEach {
                if (rematchingPlayers.contains(it.uuid) && it.profile.match == profile.match) return@forEach
                /*bukkitPlayer.hidePlayer(it.player)
                it.player.hidePlayer(bukkitPlayer)*/
            }

            if (!rematchingPlayers.contains(matchPlayer.uuid)) {
                CustomItemStack.customItemStacks.removeIf { it.uuid == matchPlayer.uuid }
            }

            if (profile.state in listOf(ProfileState.MATCH, ProfileState.QUEUE) && profile.match == uuid) {
                if (profile.state != ProfileState.QUEUE) {
                    profile.state = ProfileState.LOBBY
                }

                profile.match = null

                Constants.SPAWN?.let { bukkitPlayer.teleport(it) }

                Hotbar.giveHotbar(profile)
            }

            ratingMessage(profile)
        }

        spectators.forEach { spectator ->
            removeSpec(spectator.player)
        }

        spectators.clear()
        snapshots.clear()
        reset()
        matches.remove(this.uuid)

        MatchManager.endMatch(this)
        arena.free = true
    }

    open fun handleQuit(matchPlayer: MatchPlayer) {
        matchPlayer.offline = true

        val snapshot = MatchSnapshot(matchPlayer.player, matchPlayer.dead).apply {
            potionsThrown = matchPlayer.potionsThrown
            potionsMissed = matchPlayer.potionsMissed
            longestCombo = matchPlayer.longestCombo
            totalHits = matchPlayer.hits
            opponent = getOpponent(matchPlayer.uuid)?.uuid
            wtapAttempts = matchPlayer.wtapAttempts
            effectiveWTaps = matchPlayer.effectiveWTaps
            wtapAccuracy = matchPlayer.wtapAccuracy
        }

        snapshots.add(snapshot)
        handleDeath(matchPlayer)
    }

    private fun ratingMessage(profile: Profile) {
        if (!profile.settings.mapRating || ArenaRatingManager.hasRated(profile.uuid, arena)) return
        profile.player.takeIf { it.isOnline }?.let { player ->
            FancyMessage().text("${CC.PRIMARY}Rate The Map: ")
                .then().text("${CC.DARK_RED}[1] ").command("/ratemap ${arena.name} 1").tooltip("${CC.PRIMARY}Click to vote!")
                .then().text("${CC.RED}[2] ").command("/ratemap ${arena.name} 2").tooltip("${CC.PRIMARY}Click to vote!")
                .then().text("${CC.YELLOW}[3] ").command("/ratemap ${arena.name} 3").tooltip("${CC.PRIMARY}Click to vote!")
                .then().text("${CC.GREEN}[4] ").command("/ratemap ${arena.name} 4").tooltip("${CC.PRIMARY}Click to vote!")
                .then().text("${CC.DARK_GREEN}[5] ").command("/ratemap ${arena.name} 5").tooltip("${CC.PRIMARY}Click to vote!")
                .send(player)
        }
    }

    open fun endMessage(winners: MutableList<MatchPlayer>, losers: MutableList<MatchPlayer>) {
        players.filterNot { it.offline }.forEach {
        }
        val fancyMessage = TextBuilder()
            .setText("${CC.GREEN}Winner: ").then()

        winners.forEachIndexed { index, matchPlayer ->
            fancyMessage.setText("${CC.PRIMARY}${matchPlayer.name}${if (index < winners.size - 1) "${CC.GRAY}, " else ""}")
                .setCommand("/matchsnapshot ${matchPlayer.uuid}").then()
        }

        fancyMessage.setText("${CC.GRAY} ⎟ ${CC.RED}Loser: ").then()

        losers.forEachIndexed { index, matchPlayer ->
            fancyMessage.setText("${CC.PRIMARY}${matchPlayer.name}${if (index < losers.size - 1) "${CC.GRAY}, " else ""}")
                .setCommand("/matchsnapshot ${matchPlayer.uuid}").then()
        }



        val message = fancyMessage.build()

        players.filterNot { it.offline }.forEach {
            it.player.sendMessage(" ")
            it.player.sendMessage(CC.translate("${CC.PRIMARY}Match Overview &7&o(Click to view inventories)"))
            it.player.spigot().sendMessage(message)
            if (spectators.isNotEmpty()) {
                it.player.sendMessage(" ")
                it.player.sendMessage(" ")
                it.player.sendMessage(CC.translate("${CC.PRIMARY}Spectators ${CC.GRAY}(${spectators.size})${CC.GREEN}: "))
                it.player.sendMessage(CC.translate(Joiner.on("${CC.GRAY}, ${CC.RESET}").join(spectators.map { x -> x.name })))
            }
            it.player.sendMessage(" ")
        }

        spectators.mapNotNull { Bukkit.getPlayer(it.uuid) }.forEach {
            it.sendMessage(" ")
            it.spigot().sendMessage(message)
            if (spectators.isNotEmpty()) {
                it.player.sendMessage(" ")
                it.player.sendMessage(CC.translate("${CC.PRIMARY}Spectators ${CC.GRAY}(${spectators.size})${CC.GREEN}: "))
                it.player.sendMessage(CC.translate(Joiner.on("${CC.GRAY}, ${CC.RESET}").join(spectators.map { x -> x.name })))
            }
            it.sendMessage(" ")
        }
    }

    private fun getNextEloTarget(profile: Profile?, kit: Kit): Pair<Profile, Int>? {
        if (profile == null) return null

        val currentElo = profile.getKitStatistic(kit.name)?.elo ?: return null
        val profiles = PracticePlugin.instance.profileManager.profiles.values

        val targetProfile = profiles
            .filter { it.uuid != profile.uuid }
            .map { it to (it.getKitStatistic(kit.name)?.elo ?: 0) }
            .filter { it.second > currentElo }
            .minByOrNull { it.second }

        return targetProfile?.let { it.first to (it.second - currentElo) }
    }

    private fun sendTitleBar(winners: MutableList<MatchPlayer>) {
        players.filterNot { it.offline }.forEach {
            TitleBar.sendTitleBar(it.player, "${CC.SECONDARY}${getCombinedNames(winners)}${CC.PRIMARY} won!", null, 10, 60, 10)
        }
    }

    fun getCombinedNames(players: MutableList<MatchPlayer>, suffix: String = ""): String {
        return players.joinToString(", ") {
            CC.translate("${it.name} &f${if (ranked) PracticePlugin.instance.profileManager.findById(it.uuid)!!.getKitStatistic(kit.name)!!.elo else ""}$suffix".replace("<elo>", getEloUpdate(it).toString()))
        }
    }

    private fun getEloUpdate(matchPlayer: MatchPlayer): Int {
        return PracticePlugin.instance.profileManager.findById(matchPlayer.uuid)!!.getKitStatistic(kit.name)!!.elo - matchPlayer.initialElo
        /*val profile = PracticePlugin.instance.profileManager.findById(matchPlayer.uuid) ?: return 0
        return profile.getKitStatistic(kit.name)?.elo?.minus(matchPlayer.initialElo) ?: 0*/
    }

    fun getTime(): String {
        return when (matchState) {
            MatchState.STARTING -> "${CC.GREEN}Starting"
            MatchState.ENDING -> "${CC.RED}Ending"
            else -> TimeUtil.millisToTimer(System.currentTimeMillis() - started)
        }
    }

    open fun reset() {
        blocksPlaced.forEach { it.type = Material.AIR }
        droppedItems.forEach { it.remove() }
    }

    fun getMatchPlayer(uuid: UUID): MatchPlayer? {
        return players.firstOrNull { it.uuid == uuid }
    }

    fun getOpponent(uuid: UUID): MatchPlayer? {
        return players.firstOrNull { it.uuid != uuid }
    }

    open fun getOpponentString(uuid: UUID): String? {
        return getOpponent(uuid)?.name
    }

    fun getAlivePlayers(): MutableList<MatchPlayer> {
        return players.filterNot { it.dead || it.offline }.toMutableList()
    }

    companion object {
        val matches: ConcurrentHashMap<UUID, Match> = ConcurrentHashMap()
        val spectators: ConcurrentHashMap<UUID, Match> = ConcurrentHashMap()

        fun getByUUID(uuid: UUID): Match? {
            return matches[uuid]
        }

        fun getSpectator(uuid: UUID): Match? {
            return spectators[uuid]
        }

        fun inMatch(): Int {
            return matches.values.sumOf { it.players.size }
        }
    }
}

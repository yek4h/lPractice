package net.lyragames.practice.manager

import dev.yek4h.spigot.knockback.KnockbackAPI
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.impl.*
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.item.CustomItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer

object MatchManager {

    fun createMatch(kit: Kit, arena: Arena, queueType: QueueType?, friendly: Boolean, firstPlayer: Player, secondPlayer: Player): Match? {

        val match = when {
            kit.mlgRush -> MLGRushMatch(kit, arena, queueType == QueueType.RANKED)
            kit.bedFights -> BedFightMatch(kit, arena, queueType == QueueType.RANKED)
            kit.bridge -> BridgeMatch(kit, arena, queueType == QueueType.RANKED)
            kit.fireballFight -> FireballFightMatch(kit, arena, queueType == QueueType.RANKED)
            else -> Match(kit, arena, queueType == QueueType.RANKED)
        }

        if (arena.arenaType == ArenaType.STANDALONE) {
            arena.free = false
        }

        match.friendly = friendly

        val profile = PracticePlugin.instance.profileManager.findById(firstPlayer.uniqueId)!!
        val profile1 = PracticePlugin.instance.profileManager.findById(secondPlayer.uniqueId)!!

        if (profile.state == ProfileState.MATCH || profile1.state == ProfileState.MATCH) {
            return null
        }

        profile.apply {
            this.match = match.uuid
            matchObject = match
            state = ProfileState.MATCH
        }

        profile1.apply {
            this.match = match.uuid
            matchObject = match
            state = ProfileState.MATCH
        }

        when (arena) {
            else -> {
                match.addPlayer(firstPlayer, arena.l1!!)
                match.addPlayer(secondPlayer, arena.l2!!)
            }
        }

        if (!friendly) {
            generateMatchMessage(firstPlayer, secondPlayer, queueType == QueueType.RANKED, arena, kit, false)
        } else {
            generateMatchMessage(firstPlayer, secondPlayer, false, arena, kit, true)
        }


        KnockbackAPI.applyKnockback(KnockbackAPI.getByName(kit.knockbackProfile), firstPlayer)
        KnockbackAPI.applyKnockback(KnockbackAPI.getByName(kit.knockbackProfile), secondPlayer)

        Match.matches[match.uuid] = match
        QueueManager.updatePlayingCount(match, match.players.size)
        match.players.forEach {
            it.player.maximumNoDamageTicks = kit.damageTicks
            it.player.noDamageTicks = kit.damageTicks
        }
        match.start()

        println("match created")
        return match
    }

    private fun generateMatchMessage(firstPlayer: Player, secondPlayer: Player, ranked: Boolean, arena: Arena, kit: Kit, friendly: Boolean) {
        val messageType = if (ranked) "Competitive" else "Casual"
        val msg = if (friendly) "Private" else messageType
        val firstPlayerPing = PlayerUtil.getPing(secondPlayer)
        val secondPlayerPing = PlayerUtil.getPing(firstPlayer)

        val firstPlayerElo = PracticePlugin.instance.profileManager.findById(firstPlayer.uniqueId)?.getKitStatistic(kit.name)?.elo
        val secondPlayerElo = PracticePlugin.instance.profileManager.findById(secondPlayer.uniqueId)?.getKitStatistic(kit.name)?.elo

        listOf(firstPlayer, secondPlayer).forEach { player ->
            player.sendMessage(" ")
            player.sendMessage("${CC.PRIMARY}${CC.BOLD}$msg ${kit.name}")
            player.sendMessage("${CC.PRIMARY} Map: ${CC.SECONDARY}${arena.name}")
            player.sendMessage("${CC.PRIMARY} Opponent: ${CC.RED}${if (player == firstPlayer) secondPlayer.name else firstPlayer.name}")
            player.sendMessage("${CC.PRIMARY} Ping: ${CC.RED}${if (player == firstPlayer) firstPlayerPing else secondPlayerPing} ms")
            if (ranked) {
                player.sendMessage("${CC.PRIMARY} ELO: ${CC.SECONDARY}${if (player == firstPlayer) secondPlayerElo else firstPlayerElo}")
            }
            player.sendMessage(" ")
        }
    }

    fun createTeamMatch(kit: Kit, arena: Arena, queueType: QueueType?, friendly: Boolean, players: List<UUID>) {
        arena.free = false

        val match = when {
            kit.mlgRush -> MLGRushMatch(kit, arena, queueType == QueueType.RANKED)
            kit.bedFights -> BedFightMatch(kit, arena, queueType == QueueType.RANKED)
            kit.bridge -> BridgeMatch(kit, arena, queueType == QueueType.RANKED)
            kit.fireballFight -> FireballFightMatch(kit, arena, queueType == QueueType.RANKED)
            else -> TeamMatch(kit, arena, queueType == QueueType.RANKED)
        }

        match.friendly = friendly

        val team1 = match.teams[0]
        val team2 = match.teams[1]

        players.chunked(players.size / 2).let { (firstTeam, secondTeam) ->
            firstTeam.forEach { uuid ->
                val profile = PracticePlugin.instance.profileManager.findById(uuid)
                profile?.apply {
                    kitEditorData = null
                    state = ProfileState.MATCH
                    this.match = match.uuid
                    matchObject = match
                }
                match.addPlayer(Bukkit.getPlayer(uuid), team1)
            }

            secondTeam.forEach { uuid ->
                val profile = PracticePlugin.instance.profileManager.findById(uuid)
                profile?.apply {
                    kitEditorData = null
                    state = ProfileState.MATCH
                    this.match = match.uuid
                    matchObject = match
                }
                match.addPlayer(Bukkit.getPlayer(uuid), team2)
            }
        }

        Match.matches[match.uuid] = match

        QueueManager.updatePlayingCount(match, match.players.size)
        match.start()
    }
    fun createTeamMatch(kit: Kit, arena: Arena, queueType: QueueType?, friendly: Boolean, firstTeam: List<UUID>, secondTeam: List<UUID>) {
        arena.free = false

        val match = when {
            kit.mlgRush -> MLGRushMatch(kit, arena, queueType == QueueType.RANKED)
            kit.bedFights -> BedFightMatch(kit, arena, queueType == QueueType.RANKED)
            kit.bridge -> BridgeMatch(kit, arena, queueType == QueueType.RANKED)
            kit.fireballFight -> FireballFightMatch(kit, arena, queueType == QueueType.RANKED)
            else -> TeamMatch(kit, arena, queueType == QueueType.RANKED)
        }

        match.friendly = friendly

        val team1 = match.teams[0]
        val team2 = match.teams[1]

        firstTeam.forEach { uuid ->
            val profile = PracticePlugin.instance.profileManager.findById(uuid)
            profile?.apply {
                kitEditorData = null
                state = ProfileState.MATCH
                this.match = match.uuid
                matchObject = match
            }
            match.addPlayer(Bukkit.getPlayer(uuid), team1)
        }

        secondTeam.forEach { uuid ->
            val profile = PracticePlugin.instance.profileManager.findById(uuid)
            profile?.apply {
                kitEditorData = null
                state = ProfileState.MATCH
                this.match = match.uuid
                matchObject = match
            }
            match.addPlayer(Bukkit.getPlayer(uuid), team2)
        }

        Match.matches[match.uuid] = match
        QueueManager.updatePlayingCount(match, match.players.size)
        match.start()
    }

    fun endMatch(match: Match) {
        Match.matches.remove(match.uuid)
        QueueManager.updatePlayingCount(match, Match.matches.size)
    }

    fun createReQueueItem(player: Player, match: Match) {
        val item = CustomItemStack(player.uniqueId, ItemBuilder(Material.PAPER).name("${CC.SECONDARY}Play again!").build())
        item.apply {
            rightClick = true
            removeOnClick = true
            clicked = Consumer {
                match.rematchingPlayers.add(player.uniqueId)
                val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
                QueueManager.addToQueue(player, match.kit, if (match.ranked) QueueType.RANKED else QueueType.UNRANKED)
                Hotbar.giveHotbar(profile)
            }
        }
        item.create()
        player.inventory.setItemInHand(item.itemStack)
        player.updateInventory()
    }
}
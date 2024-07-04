package net.lyragames.practice.match.impl

import net.lyragames.practice.arena.Arena
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.match.player.MatchPlayer
import net.lyragames.practice.match.player.TeamMatchPlayer
import net.lyragames.practice.match.team.Team
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.impl.StandaloneArena
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.LocationHelper
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.countdown.TitleCountdown
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.material.Bed

class FireballFightMatch(kit: Kit, arena: Arena, ranked: Boolean): TeamMatch(kit, arena, ranked) {

    private val bedBroken: MutableMap<Block, Pair<Boolean, BlockFace>> = mutableMapOf()

    init {
        teams.clear()

        val team1 = Team("Red")
        team1.spawn = (arena as StandaloneArena).l1
        team1.bedLocation = arena.findClosestBlockByLocation(arena.l1, Material.BED_BLOCK)
        team1.color = CC.RED
        team1.coloredName = "${CC.RED}Red"

        val team2 = Team("Blue")
        team2.spawn = arena.l2
        team2.bedLocation = arena.findClosestBlockByLocation(arena.l2, Material.BED_BLOCK)
        team2.color = CC.BLUE
        team2.coloredName = "${CC.BLUE}Blue"

        teams.add(team1)
        teams.add(team2)
    }

    override fun addPlayer(player: Player, location: Location) {
        val team = findTeam()
        val elo = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!!!.getKitStatistic(kit.name)!!.elo

        val teamMatchPlayer = TeamMatchPlayer(player.uniqueId, player.name, team?.spawn!!, team.uuid, elo)

        teamMatchPlayer.coloredName = "${team.color}${player.name}"
        teamMatchPlayer.bedLocations = LocationHelper.findBedLocations(team.bedLocation!!)

        team.players.add(teamMatchPlayer)

        players.stream().map { it.player }.forEach {
            it.showPlayer(player)
            player.showPlayer(it)
        }
        players.add(teamMatchPlayer)
    }

    override fun reset() {
        super.reset()

        bedBroken.forEach { (block, head) ->
            run {
                block.type = Material.BED_BLOCK
                val state = block.state
                val bed = state.data as Bed

                bed.setFacingDirection(head.second)
                bed.isHeadOfBed = head.first
                state.data = bed

                state.update()
            }
        }
    }

    fun handleBreak(event: BlockBreakEvent) {

        val matchPlayer = getMatchPlayer(event.player.uniqueId)

        if (matchPlayer!!.dead || matchPlayer.respawning) {
            event.isCancelled = true
            return
        }

        if (blocksPlaced.contains(event.block)) {
            blocksPlaced.remove(event.block)
        } else {
            event.isCancelled = true
        }

        if (event.block.type == Material.BED || event.block.type == Material.BED_BLOCK) {

            if (matchPlayer.bedLocations.contains(event.block.location)) {
                event.player.sendMessage("${CC.RED}You cannot break your own bed.")
                return
            }

            for (player in players) {
                if (player.bedLocations.contains(event.block.location)) {
                    val team = getTeam((player as TeamMatchPlayer).teamUniqueId)

                    if (team!!.broken) {
                        matchPlayer.player.sendMessage("${CC.RED}The bed has already been broken.")
                        break
                    }

                    for (location in player.bedLocations) {
                        val bed = location.block.state.data as Bed
                        val head = bed.isHeadOfBed

                        bedBroken[location.block] = Pair(head, bed.facing)
                    }

                    event.isCancelled = true

                    event.block.type = Material.AIR
                    event.block.state.update()

                    team.sendTitle("${CC.RED}BED DESTROYED!", "You will no longer respawn!", 10, 40, 10)

                    sendMessage(" ")
                    sendMessage("${CC.SECONDARY}${CC.BOLD}BED DESTRUCTION > ${team.coloredName} Bed ${CC.PRIMARY}was destroyed by ${matchPlayer.coloredName}${CC.PRIMARY}!")
                    sendMessage(" ")

                    team.broken = true
                    break
                }
            }
        }
    }

    override fun handleDeath(player: MatchPlayer) {

        val team = getTeam((player as TeamMatchPlayer).teamUniqueId)

        if (player.offline) {
            sendMessage("&c${player.coloredName} ${CC.PRIMARY}has disconnected!")
        } else if (player.lastDamager == null && !player.offline) {
            sendMessage("&c${player.coloredName} ${CC.PRIMARY}was killed!${if (team?.broken!!) "${CC.GRAY} (${CC.SECONDARY}FINAL${CC.GRAY})" else ""}")
        } else {
            val matchPlayer = getMatchPlayer(player.lastDamager!!)

            sendMessage("&c${player.coloredName} ${CC.PRIMARY}was killed by ${matchPlayer?.coloredName}${CC.PRIMARY}!${if (team?.broken!!) "${CC.GRAY} (${CC.SECONDARY}FINAL${CC.GRAY})" else ""}")
        }

        player.respawning = true

        if (team?.broken!!) {
            PlayerUtil.reset(player.player)

            player.dead = true

            player.player.allowFlight = true
            player.player.isFlying = true

            players.stream().forEach { if (!it.offline) it.player.hidePlayer(player.player) }

            player.player.teleport(arena.bounds.center)

            if (!team.players.any { !it.dead && !it.offline }) {
                end(team.players.map { getMatchPlayer(it.uuid)!! }.toMutableList())
            }

            return
        }

        PlayerUtil.reset(player.player)
        player.player.gameMode = GameMode.SPECTATOR

        player.player.allowFlight = true
        player.player.isFlying = true

        players.stream().forEach { if (!it.offline) it.player.hidePlayer(player.player) }

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

    private fun end(player: TeamMatchPlayer) {
        end(getTeam(player.teamUniqueId)!!.players.map { getMatchPlayer(it.uuid)!! }.toMutableList())
    }

    override fun handleQuit(matchPlayer: MatchPlayer) {
        matchPlayer.offline = true

        if (teams.any { team -> team.players.none { !it.offline } }) {
            val team = teams.firstOrNull { team -> team.players.none { !it.dead && !it.offline } }

            team!!.players.map { getMatchPlayer(it.uuid)!! }.toMutableList().let { end(it) }
        }
    }
}
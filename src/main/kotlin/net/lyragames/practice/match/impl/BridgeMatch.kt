package net.lyragames.practice.match.impl

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.impl.StandaloneArena
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.match.MatchState
import net.lyragames.practice.match.player.MatchPlayer
import net.lyragames.practice.match.player.TeamMatchPlayer
import net.lyragames.practice.match.team.Team
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.countdown.Countdown
import net.lyragames.practice.utils.title.TitleBar
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPortalEnterEvent

class BridgeMatch(kit: Kit, arena: Arena, ranked: Boolean) : TeamMatch(kit, arena, ranked) {

    private var round = 1
    private var threshold: Long = 0L

    init {
        teams.clear()

        val team1 = Team("Red")
        team1.spawn = (arena as StandaloneArena).l1
        team1.portal = arena.findClosestBlock(arena.l1, Material.ENDER_PORTAL)
        team1.color = CC.RED
        team1.coloredName = "${CC.RED}Red"

        for (block in arena.findClosestBlock(arena.l1, Material.ENDER_PORTAL)!!.blocks) {
            block.type = Material.ENDER_PORTAL
        }

        val team2 = Team("Blue")
        team2.spawn = arena.l2
        team2.portal = arena.findClosestBlock(arena.l2, Material.ENDER_PORTAL)
        team2.color = CC.BLUE
        team2.coloredName = "${CC.BLUE}Blue"

        for (block in arena.findClosestBlock(arena.l2, Material.ENDER_PORTAL)!!.blocks) {
            block.type = Material.ENDER_PORTAL
        }

        teams.add(team1)
        teams.add(team2)
    }

    override fun addPlayer(player: Player, location: Location) {
        val team = findTeam()
        val elo = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!!!.getKitStatistic(kit.name)!!.elo

        val teamMatchPlayer = TeamMatchPlayer(player.uniqueId, player.name, team?.spawn!!, team.uuid, elo)

        teamMatchPlayer.coloredName = "${team.color}${player.name}"

        team.players.add(teamMatchPlayer)

        players.stream().map { it.player }.forEach {
            it.showPlayer(player)
            player.showPlayer(it)
        }
        players.add(teamMatchPlayer)
    }

    fun handlePortal(event: EntityPortalEnterEvent) {

        // || event.from.block != null && event.from.block.type == Material.ENDER_PORTAL)

        if (System.currentTimeMillis() - threshold <= 100) {
            return
        }

        if (matchState != MatchState.FIGHTING) return

        val player = event.entity as Player

        val matchPlayer = getMatchPlayer(player.uniqueId) as TeamMatchPlayer

        if (matchPlayer.dead || matchPlayer.respawning) return

        val team = getTeam(matchPlayer.teamUniqueId)
        val oppositeTeam = getOpponentTeam(team!!)

        val blue = team.name == "Blue"

        if (oppositeTeam!!.portal!!.contains(event.location)) {
            team.points++

            players.forEach {
                if (it.offline) return@forEach

                TitleBar.sendTitleBar(
                    it.player,
                    "${matchPlayer.coloredName}${CC.PRIMARY} scored!",
                    "${CC.BLUE}${if (blue) team.points else oppositeTeam.points} ${CC.GRAY}- ${CC.RED}${if (blue) oppositeTeam.points else team.points}",
                    10, 80, 10
                )
            }

            threshold = System.currentTimeMillis()

            resetMatch(team)
        }
    }

    private fun resetMatch(score: Team) {
        if (score.points >= 5) {
            end(getOpponentTeam(score)!!.players.map { it }.toMutableList())
        } else {
            round++

            countdowns.forEach { it.cancel() }

            matchState = MatchState.STARTING

            for (matchPlayer in players) {
                if (matchPlayer.offline) continue

                val player = matchPlayer.player
                val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

                if (profile!!.arrowCooldown != null) {
                    profile.arrowCooldown!!.cancel()
                    profile.arrowCooldown = null
                }

                PlayerUtil.reset(player)
                PlayerUtil.denyMovement(player)

                player.teleport(matchPlayer.spawn)
                profile.getKitStatistic(kit.name)?.generateBooks(player)

                countdowns.add(Countdown(
                    player,
                    "${CC.SECONDARY}<seconds>${CC.PRIMARY}...",
                    6
                ) {
                    player.sendMessage("${CC.PRIMARY}Round started!")
                    matchState = MatchState.FIGHTING
                    PlayerUtil.allowMovement(player)
                })
            }
        }
    }

    fun handlePlace(event: BlockPlaceEvent) {
        var found = false

        for (team in teams) {
            if (team.portal!!.contains(event.block.x, team.portal!!.lowerY, event.block.z)) {
                event.isCancelled = true
                found = true
                break
            }
        }

        if (!found) {
            blocksPlaced.add(event.block)
        }
    }

    override fun handleDeath(matchPlayer: MatchPlayer) {
        if (matchPlayer.offline) {
            sendMessage("&c${matchPlayer.coloredName} ${CC.PRIMARY}has disconnected!")
        } else if (matchPlayer.lastDamager == null && !matchPlayer.offline) {
            sendMessage("&c${matchPlayer.coloredName} ${CC.PRIMARY}was killed!")
        } else {
            val killer = getMatchPlayer(matchPlayer.lastDamager!!)

            sendMessage("${matchPlayer.coloredName} ${CC.PRIMARY}was killed by " + killer?.coloredName + "${CC.PRIMARY}!")
        }

        val profile = PracticePlugin.instance.profileManager.findById(matchPlayer.uuid)

        if (profile!!.arrowCooldown != null) {
            profile.arrowCooldown!!.cancel()
            profile.arrowCooldown = null
        }

        matchPlayer.respawning = true

        Bukkit.getScheduler().runTaskLater(PracticePlugin.instance, {
            matchPlayer.player.teleport(matchPlayer.spawn)
            PlayerUtil.reset(matchPlayer.player)

            profile.getKitStatistic(kit.name)?.generateBooks(matchPlayer.player)

            matchPlayer.respawning = false
        }, 5L)
    }


    override fun handleQuit(matchPlayer: MatchPlayer) {
        matchPlayer.offline = true

        if (teams.any { team -> team.players.none { !it.offline } }) {
            val team = teams.firstOrNull { team -> team.players.none { !it.dead && !it.offline } }

            team!!.players.map { getMatchPlayer(it.uuid)!! }.toMutableList().let { end(it) }
        }
    }
}
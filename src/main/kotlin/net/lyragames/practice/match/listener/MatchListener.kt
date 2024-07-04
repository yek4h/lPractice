package net.lyragames.practice.match.listener

import net.lyragames.practice.Locale
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.MatchState
import net.lyragames.practice.match.impl.*
import net.lyragames.practice.match.player.TeamMatchPlayer
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.utils.Cooldown
import net.lyragames.practice.utils.TimeUtil
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.Potion

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 1/27/2022
 * Project: lPractice
 */

object MatchListener : Listener {

    // TODO: Clean this class.

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)

            if (match?.matchState != MatchState.FIGHTING) {
                event.isCancelled = true
                return
            }

            val matchPlayer = match.getMatchPlayer(player.uniqueId)

            if (matchPlayer?.dead!! || matchPlayer.respawning) {
                event.isCancelled = true
                return
            }

            if (match.kit.build || match.kit.mlgRush || match.kit.bedFights || match.kit.bridge || match.kit.fireballFight) {
                if (!match.arena.bounds.isInCuboid(event.blockPlaced.location)) {
                    event.isCancelled = true
                    player.sendMessage(Locale.CANT_PLACE.getMessage())
                    return
                }

                if (match.kit.bridge) {
                    (match as BridgeMatch).handlePlace(event)
                    return
                }

                if (event.block.type == Material.TNT) {
                    event.block.type = Material.AIR

                    val tnt = player.location.world.spawn(event.block.location, TNTPrimed::class.java) as TNTPrimed
                    tnt.fuseTicks = 4 * 20
                    tnt.setMetadata("match", FixedMetadataValue(PracticePlugin.instance, match.uuid.toString()))
                    return
                }

                match.blocksPlaced.add(event.blockPlaced)
            } else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)

            if (match!!.matchState != MatchState.FIGHTING) {
                event.isCancelled = true
                return
            }

            if (match.kit.mlgRush && match is MLGRushMatch) {
                match.handleBreak(event)
                return
            }

            if (match.kit.bedFights && match is BedFightMatch) {
                match.handleBreak(event)
                return
            }

            if (match.kit.fireballFight && match is FireballFightMatch) {
                match.handleBreak(event)
                return
            }

            if ((match.kit.build || match.kit.bridge) && match.blocksPlaced.contains(event.block)) {
                match.blocksPlaced.remove(event.block)
            } else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLiquidPlace(event: PlayerBucketEmptyEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.match != null) {
            val match = Match.getByUUID(profile.match!!)

            if (match!!.kit.build) {
                match.blocksPlaced.add(event.blockClicked)
            } else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLiquidFill(event: PlayerBucketFillEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)

            if (match!!.kit.build && match.blocksPlaced.contains(event.blockClicked)) {
                match.blocksPlaced.remove(event.blockClicked)
            } else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onItemConsume(event: PlayerItemConsumeEvent) {
        val item = event.item ?: return

        val type = item.type
        val player = event.player

        if (type.id == 373 && PracticePlugin.instance.settingsFile
                .getBoolean("MATCH.REMOVE-EMPTY-BOTTLE")
        ) {
            PracticePlugin.instance.server.scheduler.runTaskLaterAsynchronously(PracticePlugin.instance, {
                player.itemInHand = ItemStack(Material.AIR)
                player.updateInventory()
            }, 1L)
            return
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)
            val matchPlayer = match?.getMatchPlayer(player.uniqueId)

            if (match!!.matchState != MatchState.FIGHTING) {
                event.isCancelled = true
                return
            }

            if (matchPlayer?.dead!! || matchPlayer.respawning) {
                event.isCancelled = true
                return
            }

            // Permitir tirar items al suelo
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPickup(event: PlayerPickupItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)
            val matchPlayer = match?.getMatchPlayer(player.uniqueId)

            if (match!!.matchState != MatchState.FIGHTING) {
                event.isCancelled = true
                return
            }

            if (matchPlayer?.dead!! || matchPlayer.respawning) {
                event.isCancelled = true
                return
            }

            // Permitir recoger items del suelo
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHit(event: EntityDamageByEntityEvent) {
        if (event.damager is Player && event.entity is Player) {
            val player = event.entity as Player
            val damager = event.damager as Player

            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            val profile1 = PracticePlugin.instance.profileManager.findById(damager.uniqueId)!!

            if (profile.state == ProfileState.SPECTATING || profile1.state == ProfileState.SPECTATING) {
                event.isCancelled = true
                return
            }

            if (profile.state != ProfileState.MATCH || profile1.state != ProfileState.MATCH) {
                return
            }

            if (profile.match?.equals(profile1.match)!!) {
                val match = Match.getByUUID(profile.match!!)
                val matchPlayer = match!!.getMatchPlayer(player.uniqueId)
                val matchPlayer1 = match.getMatchPlayer(damager.uniqueId)!!

                if (!match.canHit(player, damager)) {
                    event.isCancelled = true
                } else {
                    matchPlayer!!.wtapAttempts++
                    println("wtap Attempt: ${matchPlayer!!.wtapAttempts}")
                    if (damager.isSprinting) {
                        matchPlayer.effectiveWTaps++
                        println("Effective wtap")
                    }

                    if (matchPlayer!!.dead || matchPlayer1.dead || matchPlayer.respawning || matchPlayer1.respawning) {
                        event.isCancelled = true
                        return
                    }

                    matchPlayer.lastDamager = damager.uniqueId
                    matchPlayer.comboed++
                    matchPlayer1.hits++
                    matchPlayer1.combo++
                    matchPlayer1.comboed = 0

                    if (matchPlayer1.combo > matchPlayer1.longestCombo) {
                        matchPlayer1.longestCombo = matchPlayer1.combo
                    }

                    if (match.kit.boxing) {
                        event.damage = 0.0

                        if (match is TeamMatch) {
                            val team = match.getTeam((matchPlayer1 as TeamMatchPlayer).teamUniqueId)
                            team!!.hits++
                            if (team.hits >= 200) {
                                match.end(team)
                                return
                            }
                        }

                        if (matchPlayer1.hits >= 100) {
                            match.handleDeath(matchPlayer)
                        }
                    }

                    if (match.kit.mlgRush || match.kit.sumo) {
                        event.damage = 0.0
                    }

                    matchPlayer.combo = 0
                }
            } else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!) ?: return

            match.handleDeath(match.getMatchPlayer(player.uniqueId)!!)
            event.drops.clear()  // No dropear items al morir
        }

        player.spigot().respawn()
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (Constants.SPAWN != null) {
            event.respawnLocation = Constants.SPAWN
        } else {
            event.respawnLocation = event.player.location
        }
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        val profile = PracticePlugin.instance.profileManager.findById(event.entity.uniqueId)

        if (profile?.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!) ?: return

            if (!match.kit.hunger) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity is Player) {

            if (event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
            ) {
                event.damage = 0.0
                return
            }

            val profile = PracticePlugin.instance.profileManager.findById((event.entity as Player).player.uniqueId)

            if (profile!!.state == ProfileState.MATCH) {
                val match = Match.getByUUID(profile.match!!)
                val kit = match!!.kit

                if (match.matchState != MatchState.FIGHTING) {
                    event.isCancelled = true
                }

                val matchPlayer = match.getMatchPlayer(profile.uuid)

                if (matchPlayer!!.dead || matchPlayer.respawning) {
                    event.isCancelled = true
                    return
                }

                if (!kit.fallDamage && event.cause == EntityDamageEvent.DamageCause.FALL) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onRegen(event: EntityRegainHealthEvent) {
        if (event.entity is Player) {
            val profile = PracticePlugin.instance.profileManager.findById((event.entity as Player).player.uniqueId)

            if (profile!!.state == ProfileState.MATCH) {
                val match = Match.getByUUID(profile.match!!)
                val kit = match!!.kit

                if (!kit.regeneration && event.regainReason == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onProjectileLaunchEvent(event: ProjectileLaunchEvent) {
        if (event.entity.shooter is Player) {
            val shooter = event.entity.shooter as Player
            val profile = PracticePlugin.instance.profileManager.findById(shooter.uniqueId)

            if (profile?.state == ProfileState.MATCH) {
                val match = Match.getByUUID(profile.match!!)

                if (match?.matchState != MatchState.FIGHTING) {
                    event.isCancelled = true
                    return
                }

                if (event.entity is ThrownPotion) {
                    match.getMatchPlayer(shooter.uniqueId)!!.potionsThrown++
                }

                if (event.entity is Arrow) {
                    if (match.matchState != MatchState.FIGHTING) {
                        event.isCancelled = true
                        shooter.updateInventory()
                        return
                    }

                    if (match.kit.bridge) {
                        profile.arrowCooldown = Cooldown(5) {
                            if (shooter.inventory.getItem(8) == null || shooter.inventory.getItem(8).type == Material.AIR) {
                                shooter.inventory.setItem(8, ItemStack(Material.ARROW))
                            } else {
                                shooter.inventory.addItem(ItemStack(Material.ARROW))
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (event.action == Action.PHYSICAL && event.clickedBlock.type == Material.SOIL)
            event.isCancelled = true

        if (event.action.name.contains("RIGHT")) {

            if (event.hasBlock() && player.gameMode != GameMode.CREATIVE) {
                if (event.clickedBlock.type == Material.CHEST || event.clickedBlock.type == Material.FURNACE
                    || event.clickedBlock.type == Material.TRAPPED_CHEST || event.clickedBlock.type.name.contains("FENCE_GATE")
                    || event.clickedBlock.type.name.contains("DOOR") || event.clickedBlock.type == Material.WORKBENCH
                    || event.clickedBlock.type == Material.ITEM_FRAME
                ) {
                    event.isCancelled = true
                }
            }

            if (profile.state != ProfileState.SPECTATING && profile.state != ProfileState.QUEUE && profile.state != ProfileState.LOBBY) {
                if (profile.state == ProfileState.MATCH && event.hasItem()) {
                    val match = Match.getByUUID(profile.match!!)

                    if ((event.item.type == Material.POTION && Potion.fromItemStack(event.item).isSplash)
                        || event.item.type == Material.FIREBALL
                    ) {
                        if (match!!.matchState != MatchState.FIGHTING) {
                            event.isCancelled = true
                            event.setUseItemInHand(Event.Result.DENY)

                            player.updateInventory()
                            return
                        }

                        if (event.item.type == Material.FIREBALL) {

                            event.isCancelled = true

                            if (profile.fireBallCooldown != null && !profile.fireBallCooldown!!.hasExpired()) {
                                player.sendMessage(Locale.FIREBALL_COOLDOWN.getMessage())
                            } else {
                                val fireBall = player.launchProjectile(Fireball::class.java)
                                fireBall.velocity = player.location.direction.multiply(1.4)
                                fireBall.setIsIncendiary(false)
                                fireBall.setMetadata(
                                    "match",
                                    FixedMetadataValue(PracticePlugin.instance, match.uuid.toString())
                                )

                                if (player.itemInHand.amount - 1 <= 0) {
                                    player.inventory.removeItem(player.itemInHand)
                                } else {

                                    player.itemInHand.amount = player.itemInHand.amount - 1
                                    player.updateInventory()
                                }

                                profile.fireBallCooldown = Cooldown(1) {}
                            }

                        }
                    }
                }

                if (event.hasItem() && event.item.type == Material.ENDER_PEARL) {
                    if (profile.enderPearlCooldown == null || profile.enderPearlCooldown?.hasExpired()!!) {
                        event.player.updateInventory()
                        event.setUseItemInHand(Event.Result.ALLOW)

                        profile.enderPearlCooldown = Cooldown(16) {
                            player.sendMessage(Locale.ENDERPEARL_COOLDOWN_DONE.getMessage())
                            profile.enderPearlCooldown = null
                        }
                    } else {
                        event.player.updateInventory()
                        event.isCancelled = true
                        event.setUseItemInHand(Event.Result.DENY)

                        player.sendMessage(
                            Locale.ENDERPERL_COOLDOWN_TIME.getMessage().replace(
                                "<seconds>",
                                "${profile.enderPearlCooldown?.timeRemaining?.let { TimeUtil.millisToSeconds(it) }}"
                            )
                        )

                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPotionSplashEvent(event: PotionSplashEvent) {
        if (event.potion.shooter is Player) {
            val shooter = event.potion.shooter as Player
            val shooterData = PracticePlugin.instance.profileManager.findById(shooter.uniqueId)

            if (shooterData?.state == ProfileState.FFA) {
                return
            }

            if (shooterData?.state == ProfileState.MATCH &&
                Match.getByUUID(shooterData.match!!)?.matchState == MatchState.FIGHTING
            ) {
                val match = Match.getByUUID(shooterData.match!!)

                if (event.getIntensity(shooter) <= 0.5) {
                    match?.getMatchPlayer(shooter.uniqueId)!!.potionsMissed++
                }
                for (entity in event.affectedEntities) {
                    if (entity is Player) {
                        if (match?.getMatchPlayer(entity.uniqueId) == null) {
                            event.setIntensity(entity as LivingEntity, 0.0)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPortal(event: EntityPortalEnterEvent) {
        if (event.entity is Player) {
            val profile = PracticePlugin.instance.profileManager.findById((event.entity as Player).uniqueId)

            if (profile!!.state != ProfileState.MATCH) {
                return
            }

            val match = Match.getByUUID(profile.match!!)

            if (match!!.kit.bridge) {
                (match as BridgeMatch).handlePortal(event)
            }
        }
    }
}
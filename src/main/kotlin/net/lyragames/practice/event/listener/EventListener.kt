package net.lyragames.practice.event.listener

import net.lyragames.practice.event.EventState
import net.lyragames.practice.event.EventType
import net.lyragames.practice.event.impl.BracketsEvent
import net.lyragames.practice.event.player.EventPlayerState
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.utils.CC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

object EventListener : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            val currentEvent = EventManager.event
            val eventPlayer = currentEvent?.getPlayer(player.uniqueId)

            if (eventPlayer?.state == EventPlayerState.FIGHTING) {
                if (currentEvent.type == EventType.BRACKETS) {
                    val bracketEvent = currentEvent as BracketsEvent

                    if (bracketEvent.kit.build && bracketEvent.state == EventState.FIGHTING) {
                        bracketEvent.blocksPlaced.add(event.blockPlaced)
                    } else {
                        event.isCancelled = true
                    }
                } else {
                    event.isCancelled = true
                }
            } else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            val currentEvent = EventManager.event
            val eventPlayer = currentEvent?.getPlayer(player.uniqueId)

            if (eventPlayer?.state == EventPlayerState.FIGHTING) {
                if (currentEvent.type == EventType.BRACKETS) {
                    val bracketEvent = currentEvent as BracketsEvent

                    if (bracketEvent.kit.build && bracketEvent.state == EventState.FIGHTING && bracketEvent.blocksPlaced.contains(event.block)) {
                        bracketEvent.blocksPlaced.remove(event.block)
                    }else {
                        event.isCancelled = true
                    }
                }else {
                    event.isCancelled = true
                }
            }else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onLiquidPlace(event: PlayerBucketEmptyEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            val currentEvent = EventManager.event
            val eventPlayer = currentEvent?.getPlayer(player.uniqueId)

            if (eventPlayer?.state == EventPlayerState.FIGHTING) {
                if (currentEvent.type == EventType.BRACKETS) {
                    val bracketEvent = currentEvent as BracketsEvent

                    if (bracketEvent.kit.build && bracketEvent.state == EventState.FIGHTING) {
                        bracketEvent.blocksPlaced.add(event.blockClicked)
                    } else {
                        event.isCancelled = true
                    }
                } else {
                    event.isCancelled = true
                }
            } else {
                event.isCancelled = true
            }

            return
        }
    }

    @EventHandler
    fun onLiquidFill(event: PlayerBucketFillEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            val currentEvent = EventManager.event
            val eventPlayer = currentEvent?.getPlayer(player.uniqueId)

            if (eventPlayer?.state == EventPlayerState.FIGHTING) {
                if (currentEvent.type == EventType.BRACKETS) {
                    val bracketEvent = currentEvent as BracketsEvent

                    if (bracketEvent.kit.build && bracketEvent.state == EventState.FIGHTING && bracketEvent.blocksPlaced.contains(event.blockClicked)) {
                        bracketEvent.blocksPlaced.remove(event.blockClicked)
                    }else {
                        event.isCancelled = true
                    }
                }else {
                    event.isCancelled = true
                }
            }else {
                event.isCancelled = true
            }

            return
        }
    }

    /*@EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            val currentEvent = EventManager.event
            val eventPlayer = currentEvent?.getPlayer(player.uniqueId)

            if (eventPlayer?.state == EventPlayerState.FIGHTING) {

                if (currentEvent.state == EventState.FIGHTING) {
                    currentEvent.droppedItems.add(event.itemDrop)
                }else {
                    event.isCancelled = true
                }

            }else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPickup(event: PlayerPickupItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            val currentEvent = EventManager.event
            val eventPlayer = currentEvent?.getPlayer(player.uniqueId)

            if (eventPlayer?.state == EventPlayerState.FIGHTING) {

                if (currentEvent.state == EventState.FIGHTING && currentEvent.droppedItems.contains(event.item)) {
                    currentEvent.droppedItems.remove(event.item)
                }else {
                    event.isCancelled = true
                }

            }else {
                event.isCancelled = true
            }
        }
    }*/

    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {

        if (event.entity is Player && event.damager is Player) {
            val player = event.entity as Player
            val damager = event.damager as Player

            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            val profile1 = PracticePlugin.instance.profileManager.findById(damager.uniqueId)!!

            if (profile.state == ProfileState.EVENT && profile1.state == ProfileState.EVENT) {

                val currentEvent = EventManager.event

                if (currentEvent == null) {
                    event.isCancelled = true
                    return
                }

                if (currentEvent.state != EventState.FIGHTING) {
                    event.isCancelled = true
                    return
                }

                if (currentEvent.type == EventType.TNT_RUN) {
                    event.isCancelled = true
                    return
                }

                if (currentEvent.type == EventType.SUMO) {
                    event.damage = 0.0
                }

                val eventPlayer = currentEvent.getPlayer(player.uniqueId)
                val eventPlayer1 = currentEvent.getPlayer(damager.uniqueId)

                if (currentEvent.playingPlayers.stream().noneMatch { it.uuid == eventPlayer?.uuid }
                    && currentEvent.playingPlayers.stream().noneMatch { it.uuid == eventPlayer1?.uuid }) {
                    event.isCancelled = true
                    return
                }

                if (currentEvent.type == EventType.TNT_TAG) {

                    event.damage = 0.0

                    if (eventPlayer1?.tagged!!) {

                        eventPlayer?.tagged = true
                        eventPlayer?.player?.inventory?.helmet = ItemStack(Material.TNT)
                        eventPlayer1.player.inventory.helmet = null

                        eventPlayer1.player.inventory.clear()
                        eventPlayer1.tagged = false

                        for (x in 0 until 36) {
                            eventPlayer?.player?.inventory?.setItem(x, ItemStack(Material.TNT))
                        }

                        eventPlayer?.player?.updateInventory()

                        currentEvent.sendMessage("${CC.SECONDARY}${eventPlayer?.player?.name}${CC.PRIMARY} is the tagger!")
                    }
                }

                if (!currentEvent.canHit(player, damager)) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory == null) return
        if (event.clickedInventory.type != InventoryType.PLAYER) return

        val profile = PracticePlugin.instance.profileManager.findById(event.whoClicked.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {
            if (EventManager.event!!.type == EventType.TNT_TAG) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state != ProfileState.EVENT) return

        val currentEvent = EventManager.event ?: return

        val eventPlayer = currentEvent.getPlayer(player.uniqueId)

        if (!currentEvent.getAlivePlayers().contains(eventPlayer)) return

        eventPlayer!!.dead = true
        currentEvent.endRound(currentEvent.getOpponent(eventPlayer))
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        val profile = PracticePlugin.instance.profileManager.findById(event.entity.uniqueId)!!

        if (profile.state == ProfileState.EVENT && EventManager.event != null) {
            val currentEvent = EventManager.event

            if (currentEvent?.type != EventType.BRACKETS) {
                event.isCancelled = true
                return
            }

            if (!(currentEvent as BracketsEvent).kit.hunger) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity is Player) {
            val profile = PracticePlugin.instance.profileManager.findById((event.entity as Player).player.uniqueId)!!

            if (profile.state == ProfileState.EVENT) {
                val currentEvent = EventManager.event

                if (currentEvent!!.state != EventState.FIGHTING) {
                    event.isCancelled = true
                    return
                }

                if (currentEvent.type == EventType.SUMO ||
                    currentEvent.type == EventType.TNT_RUN ||
                    currentEvent.type == EventType.TNT_TAG
                ) {
                    if (event.cause == EntityDamageEvent.DamageCause.FALL) {
                        event.isCancelled = true
                    }
                }

                if (currentEvent.type == EventType.BRACKETS) {
                    val bracketEvent = currentEvent as BracketsEvent

                    if (bracketEvent.kit.fallDamage && event.cause == EntityDamageEvent.DamageCause.FALL) {
                        event.isCancelled = true
                    }

                    if (!bracketEvent.isPlaying(profile.uuid)) {
                        event.isCancelled = true
                    }
                }
            }
        }
    }

    @EventHandler
    fun onRegen(event: EntityRegainHealthEvent) {
        if (event.entity is Player) {
            val profile = PracticePlugin.instance.profileManager.findById((event.entity as Player).player.uniqueId)!!

            if (profile.state == ProfileState.EVENT) {
                val currentEvent = EventManager.event

                if (currentEvent!!.state != EventState.FIGHTING) {
                    return
                }

                if (currentEvent.type == EventType.BRACKETS) {

                    val kit = (currentEvent as BracketsEvent).kit

                    event.isCancelled = !(kit.regeneration && event.regainReason == EntityRegainHealthEvent.RegainReason.REGEN)
                }
            }
        }
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        if (event.potion.shooter is Player) {
            val shooterData = PracticePlugin.instance.profileManager.findById((event.potion.shooter as Player).uniqueId)!!

            if (shooterData.state == ProfileState.EVENT) {
                val currentEvent = EventManager.event
                val eventPlayer = currentEvent?.getPlayer(shooterData.uuid)

                if (eventPlayer?.state != EventPlayerState.FIGHTING) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.state == ProfileState.EVENT) {

            val currentEvent = EventManager.event ?: return
            val eventPlayer = currentEvent.getPlayer(player.uniqueId)

            //if (currentEvent.playingPlayers.stream().noneMatch { it.uuid == player.uniqueId }) return
            currentEvent.handleDisconnect(eventPlayer!!)
        }
    }
}
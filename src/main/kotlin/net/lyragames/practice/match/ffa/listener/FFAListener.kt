package net.lyragames.practice.match.ffa.listener

import net.lyragames.practice.constants.Constants
import net.lyragames.practice.manager.FFAManager
import net.lyragames.practice.match.Match
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerQuitEvent

object FFAListener : Listener {

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val profile = Profile.getByUUID(player.uniqueId)

        if (profile?.state == ProfileState.FFA) {
            val ffa = FFAManager.getByUUID(profile.ffa!!) ?: return

            ffa.droppedItems.add(event.itemDrop)
        }
    }

    @EventHandler
    fun onPickup(event: PlayerPickupItemEvent) {
        val player = event.player
        val profile = Profile.getByUUID(player.uniqueId)

        if (profile?.state == ProfileState.FFA) {
            val ffa = FFAManager.getByUUID(profile.ffa!!) ?: return

            if (ffa.droppedItems.contains(event.item)) {
                ffa.droppedItems.remove(event.item)
            }else {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHit(event: EntityDamageByEntityEvent) {
        if (event.entity is Player && event.damager is Player) {
            val player = event.entity as Player
            val damager = event.damager as Player

            val profile = Profile.getByUUID(player.uniqueId)
            val profile1 = Profile.getByUUID(damager.uniqueId)

            if (profile?.state == ProfileState.FFA && profile1?.state == ProfileState.FFA) {

                if (profile.ffa != profile1.ffa) {
                    event.isCancelled = true
                    return
                }

                if (Constants.SAFE_ZONE != null && Constants.SAFE_ZONE!!.l1 != null && Constants.SAFE_ZONE!!.l2 != null) {
                    if (Constants.SAFE_ZONE!!.contains(player.location) || Constants.SAFE_ZONE!!.contains(damager.location)) {
                        event.isCancelled = true
                    }
                }
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity as Player
        val profile = Profile.getByUUID(player.uniqueId)

        if (profile!!.state == ProfileState.FFA) {
            val ffa = FFAManager.getByUUID(profile.ffa!!)
            val ffaPlayer = ffa!!.getFFAPlayer(player.uniqueId)

            val killer = player.killer

            if (killer != null) {
                ffa.handleDeath(ffaPlayer, ffa.getFFAPlayer(killer.uniqueId))
            }else {
                ffa.handleDeath(ffaPlayer, null)
            }
        }
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        val profile = Profile.getByUUID(event.entity.uniqueId)

        if (profile?.state == ProfileState.FFA) {
            val ffaMatch = FFAManager.getByUUID(profile.ffa!!) ?: return

            if (!ffaMatch.kit.kitData.hunger) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onRegen(event: EntityRegainHealthEvent) {
        if (event.entity is Player) {
            val profile = Profile.getByUUID((event.entity as Player).player.uniqueId)

            if (profile!!.state == ProfileState.FFA) {
                val ffa = FFAManager.getByUUID(profile.uuid)
                val kit = ffa!!.kit

                event.isCancelled = !(kit.kitData.regeneration && event.regainReason == EntityRegainHealthEvent.RegainReason.REGEN)
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val profile = Profile.getByUUID(player.uniqueId)

        if (profile?.state == ProfileState.FFA) {

            if (profile.ffa != null) {
                val ffa = FFAManager.getByUUID(profile.ffa!!)

                ffa?.players?.removeIf { it.uuid == player.uniqueId }
                profile.state = ProfileState.LOBBY
                profile.ffa = null
            }
        }
    }
}
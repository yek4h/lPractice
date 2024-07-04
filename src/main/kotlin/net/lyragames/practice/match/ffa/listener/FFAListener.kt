package net.lyragames.practice.match.ffa.listener

import net.lyragames.practice.constants.Constants
import net.lyragames.practice.manager.FFAManager
import net.lyragames.practice.PracticePlugin
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

object FFAListener : Listener {

    /*@EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile?.state == ProfileState.FFA) {
            val ffa = FFAManager.getByUUID(profile.ffa!!) ?: return

            ffa.handleDrop(event)
        }
    }

    @EventHandler
    fun onPickup(event: PlayerPickupItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile?.state == ProfileState.FFA) {
            val ffa = FFAManager.getByUUID(profile.ffa!!) ?: return

            if (ffa.droppedItems.contains(event.item)) {
                ffa.droppedItems.remove(event.item)
            } else {
                event.isCancelled = true
            }
        }
    }*/

    @EventHandler(ignoreCancelled = true)
    fun onHit(event: EntityDamageByEntityEvent) {
        if (event.entity is Player && event.damager is Player) {
            val player = event.entity as Player
            val damager = event.damager as Player

            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            val profile1 = PracticePlugin.instance.profileManager.findById(damager.uniqueId)

            if (profile?.state == ProfileState.FFA && profile1?.state == ProfileState.FFA) {

                if (profile.ffa != profile1.ffa) {
                    event.isCancelled = true
                    return
                }

                if (Constants.SAFE_ZONE != null && Constants.SAFE_ZONE!!.l1 != null && Constants.SAFE_ZONE!!.l2 != null) {
                    if (Constants.SAFE_ZONE!!.contains(player.location) || Constants.SAFE_ZONE!!.contains(damager.location)) {
                        event.isCancelled = true
                    } else {
                        val ffaPlayer = FFAManager.getByUUID(profile.ffa!!)!!.getFFAPlayer(player.uniqueId)

                        ffaPlayer!!.lastDamager = damager.uniqueId
                        ffaPlayer.lastDamaged = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile!!.state == ProfileState.FFA) {
            val ffa = FFAManager.getByUUID(profile.ffa!!)
            val ffaPlayer = ffa!!.getFFAPlayer(player.uniqueId)

            val killer = player.killer

            if (killer != null) {
                ffa.handleDeath(ffaPlayer!!, ffa.getFFAPlayer(killer.uniqueId))
            } else {

                if (System.currentTimeMillis() - ffaPlayer!!.lastDamaged <= 1000 && ffaPlayer.lastDamager != null) {
                    ffa.handleDeath(ffaPlayer, ffa.getFFAPlayer(ffaPlayer.lastDamager!!))
                    return
                }

                ffa.handleDeath(ffaPlayer, null)
            }
        }
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        val profile = PracticePlugin.instance.profileManager.findById(event.entity.uniqueId)

        if (profile?.state == ProfileState.FFA) {
            val ffaMatch = FFAManager.getByUUID(profile.ffa!!) ?: return

            if (!ffaMatch.kit.hunger) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onRegen(event: EntityRegainHealthEvent) {
        if (event.entity is Player) {
            val profile = PracticePlugin.instance.profileManager.findById((event.entity as Player).player.uniqueId)

            if (profile!!.state == ProfileState.FFA) {
                val ffa = FFAManager.getByUUID(profile.ffa!!)
                val kit = ffa!!.kit

                if (!kit.regeneration && event.regainReason == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.isCancelled = true
                }
            }
        }
    }
}
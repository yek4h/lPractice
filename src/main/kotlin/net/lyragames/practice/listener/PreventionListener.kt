package net.lyragames.practice.listener

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*

object PreventionListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        if (profile.state == ProfileState.SPECTATING) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onSleep(event: PlayerBedEnterEvent) {
        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        if (profile.state == ProfileState.LOBBY || profile.state == ProfileState.QUEUE || profile.state == ProfileState.SPECTATING) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHunger(event: FoodLevelChangeEvent) {
        val entity = event.entity as? Player ?: return
        val profile = PracticePlugin.instance.profileManager.findById(entity.uniqueId) ?: return

        if (profile.state == ProfileState.LOBBY || profile.state == ProfileState.QUEUE || profile.state == ProfileState.SPECTATING) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onSpawn(event: EntitySpawnEvent) {
        val type = event.entity.type
        if (type == EntityType.PLAYER || !type.isAlive || !type.isSpawnable) {
            event.isCancelled = true
            event.entity.remove()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPickup(event: PlayerPickupItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        if (profile.state == ProfileState.LOBBY || profile.state == ProfileState.QUEUE || profile.state == ProfileState.SPECTATING) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        if (profile.state == ProfileState.LOBBY || profile.state == ProfileState.QUEUE || profile.state == ProfileState.SPECTATING) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLiquidFill(event: PlayerBucketFillEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        if (profile.state == ProfileState.LOBBY || profile.state == ProfileState.QUEUE || profile.state == ProfileState.SPECTATING || profile.state == ProfileState.FFA) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLiquidPlace(event: PlayerBucketEmptyEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        if (profile.state == ProfileState.LOBBY || profile.state == ProfileState.QUEUE || profile.state == ProfileState.SPECTATING || profile.state == ProfileState.FFA) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        when (profile.state) {
            ProfileState.SPECTATING -> event.isCancelled = true
            ProfileState.LOBBY, ProfileState.QUEUE -> if (!profile.canBuild) event.isCancelled = true
            ProfileState.FFA -> event.isCancelled = true
            else -> {
                // Handle other states if necessary
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        val player = event.player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId) ?: return

        when (profile.state) {
            ProfileState.SPECTATING -> event.isCancelled = true
            ProfileState.LOBBY, ProfileState.QUEUE -> if (player.gameMode != GameMode.CREATIVE && !profile.canBuild) event.isCancelled = true
            ProfileState.FFA -> event.isCancelled = true
            else -> {
                // Handle other states if necessary
            }
        }
    }
}
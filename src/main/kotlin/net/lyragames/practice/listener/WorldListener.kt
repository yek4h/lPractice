package net.lyragames.practice.listener

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.MatchState
import org.bukkit.Bukkit
import org.bukkit.entity.Fireball
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.util.Vector
import java.util.*

object WorldListener : Listener {

    @EventHandler
    fun weatherChange(event: WeatherChangeEvent) {
        if (event.toWeatherState()) event.isCancelled = true
    }

    @EventHandler
    fun leavesChange(event: LeavesDecayEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun hangingBreak(event: HangingBreakEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun explode(event: EntityExplodeEvent) {
        if (event.entity is Fireball || event.entity is TNTPrimed) {
            if (!event.entity.hasMetadata("match")) {
                event.blockList().clear()
                return
            }

            val match = Match.getByUUID(UUID.fromString(event.entity.getMetadata("match")[0].asString()))

            if (match!!.matchState != MatchState.FIGHTING) {
                event.blockList().clear()
                return
            }

            if (!match.kit.fireballFight && !match.kit.bedFights) {
                event.blockList().clear()
                return
            }

            /*event.blockList().forEach {

                if (match.blocksPlaced.contains(it)) {
                    match.droppedItems.add(it.location.world.dropItemNaturally(it.location, it.state.data.toItemStack(1)))
                    it.type = Material.AIR
                }

            }*/

            event.blockList().removeIf { !match.blocksPlaced.contains(it) }
        }
    }

    @EventHandler
    fun damage(event: EntityDamageByEntityEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            val entity = event.entity

            Bukkit.getScheduler().runTaskLater(PracticePlugin.instance, {
                val knockback = entity.location.subtract(event.damager.location).toVector()

                knockback.normalize()
                knockback.multiply(Vector(1.7, 1.6, 1.7)) // Modify this to adjust the knockback strength

                entity.velocity = knockback
            }, 1L)
        }
    }

    /*@EventHandler
    fun damage(event: ProjectileHitEvent) {
        if (event.entity is Fireball) {

        }
    } */

    @EventHandler
    fun fire(event: ExplosionPrimeEvent) {
        event.fire = false
    }

    /*@EventHandler
    fun blockPhysics(event: BlockPhysicsEvent) {
        event.isCancelled = true
    } */

    @EventHandler
    fun frame(event: EntityDamageByEntityEvent) {
        if (event.entity is ItemFrame) {
            val profile = PracticePlugin.instance.profileManager.findById((event.damager as Player).uniqueId)

            if (!profile!!.canBuild) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun frame(event: PlayerInteractEntityEvent) {
        if (event.rightClicked is ItemFrame) {
            val profile = PracticePlugin.instance.profileManager.findById(event.player.uniqueId)

            if (!profile!!.canBuild) {
                event.isCancelled = true
            }
        }
    }

    /*@EventHandler
    fun interact(event: PlayerInteractEvent) {
        val profile = PracticePlugin.instance.profileManager.findById(event.player.uniqueId)

        event.isCancelled = !(profile?.canBuild!! && profile.match == null)
    } */

}
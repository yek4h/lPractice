package net.lyragames.practice.match.snapshot

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import java.util.*
import kotlin.math.roundToInt


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/25/2022
 * Project: lPractice
 */

class MatchSnapshot(player: Player, dead: Boolean) {

    var uuid: UUID? = null
    var username: String? = null
    var opponent: UUID? = null
    var health = 0.0
    var hunger = 0
    var armor: Array<ItemStack?>
    var contents: Array<ItemStack?>
    var effects: Collection<PotionEffect>? = null
    var potionsThrown = 0
    var potionsMissed = 0
    var longestCombo = 0
    var totalHits = 0
    var wtapAttempts = 0
    var effectiveWTaps = 0
    var wtapAccuracy = 100.0
    var createdAt: Long = 0

    init {
        uuid = player.uniqueId
        username = player.name
        health = if (dead) 0.0 else (if (player.health == 0.0) 0.0 else (player.health / 2).roundToInt()).toDouble()
        hunger = player.foodLevel
        armor = player.inventory.armorContents
        contents = player.inventory.contents
        effects = player.activePotionEffects
        createdAt = System.currentTimeMillis()
    }

    fun getRemainingPotions(): Int {
        var amount = 0
        for (itemStack in contents) {
            if (itemStack != null && itemStack.type == Material.POTION && itemStack.durability == 16421.toShort()) {
                amount++
            }
        }
        return amount
    }

    fun shouldDisplayRemainingPotions(): Boolean {
        return getRemainingPotions() > 0 || potionsThrown > 0 || potionsMissed > 0
    }

    fun getPotionAccuracy(): Double {
        if (potionsMissed == 0) {
            return 100.0
        } else if (potionsThrown == potionsMissed) {
            return 50.0
        }
        return (100.0 - potionsMissed.toDouble() / potionsThrown.toDouble() * 100.0).roundToInt().toDouble()
    }



    fun isExpired(): Boolean {
        return createdAt + 3_000 * 60 <= System.currentTimeMillis()
    }

    companion object {
        @JvmStatic
        val snapshots: MutableList<MatchSnapshot> = mutableListOf()

        @JvmStatic
        fun getByUuid(uuid: UUID?): MatchSnapshot? {
            return snapshots.stream().filter { it.uuid == uuid }
                .findFirst().orElse(null)
        }

        @JvmStatic
        fun getByName(name: String?): MatchSnapshot? {
            return snapshots.stream().filter { it.username.equals(name, true) }
                .findFirst().orElse(null)
        }
    }
}
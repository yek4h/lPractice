package net.lyragames.practice.kit

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * This Project is property of Zowpy © 2021
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 12/19/2021
 * Project: Practice
 *
 * yek4h was here <3
 * 
 */

data class Kit @JvmOverloads constructor(
    var name: String,
    var displayItem: ItemStack = ItemStack(Material.PAPER),
    var content: Array<ItemStack> = arrayOf(),
    var armorContent: Array<ItemStack> = arrayOf(),
    var editorItems: Array<ItemStack> = arrayOf(),
    var enabled: Boolean = false,
    var unrankedPosition: Int = 0,
    var rankedPosition: Int = 0,
    var combo: Boolean = false,
    var sumo: Boolean = false,
    var build: Boolean = false,
    var hcf: Boolean = false,
    var ffa: Boolean = false,
    var ranked: Boolean = true,
    var boxing: Boolean = false,
    var mlgRush: Boolean = false,
    var bedFights: Boolean = false,
    var bridge: Boolean = false,
    var fireballFight: Boolean = false,
    var hunger: Boolean = true,
    var regeneration: Boolean = true,
    var fallDamage: Boolean = true,
    var knockbackProfile: String = "default",
    var displayName: String? = null,
    var damageTicks: Int = 20
)
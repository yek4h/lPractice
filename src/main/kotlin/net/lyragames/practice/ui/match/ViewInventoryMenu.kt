package net.lyragames.practice.ui.match

import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.utils.*
import org.apache.commons.lang.StringEscapeUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import rip.katz.api.menu.buttons.DisplayButton
import kotlin.math.roundToInt

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 * Created: 2/25/2022
 * Project: lPractice
 *
 * Recoded by yek4h
 *
 */

class ViewInventoryMenu(private val target: Player) : Menu() {

    override fun getTitle(player: Player?): String {
        return "${CC.PRIMARY}${target.name}'s Inventory"
    }

    override fun getButtons(player: Player?): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        player ?: return buttons

        val fixedContents = InventoryUtil.fixInventoryOrder(target.inventory.contents)

        fixedContents.forEachIndexed { index, itemStack ->
            if (itemStack.type != Material.AIR) {
                buttons[index] = DisplayButton(itemStack, true)
            }
        }

        target.inventory.armorContents.forEachIndexed { index, itemStack ->
            if (itemStack != null && itemStack.type != Material.AIR) {
                buttons[39 - index] = DisplayButton(itemStack, true)
            }
        }

        var pos = 45
        buttons[pos++] = HealthButton(if (target.health == 0.0) 0 else (target.health / 2.0).roundToInt())
        buttons[pos++] = HungerButton(target.foodLevel)
        buttons[pos] = EffectsButton(target.activePotionEffects)

        return buttons
    }

    /*override fun isAutoUpdate(): Boolean {
        return true
    }*/

    private class HealthButton(private val health: Int) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.MELON)
                .name("${CC.PRIMARY}Health: ${CC.SECONDARY}$health/10 ${StringEscapeUtils.unescapeJava("\u2764")}")
                .amount(if (health == 0) 1 else health)
                .build()
        }
    }

    private class HungerButton(private val hunger: Int) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.COOKED_BEEF)
                .name("${CC.PRIMARY}Hunger: ${CC.SECONDARY}$hunger/20")
                .amount(if (hunger == 0) 1 else hunger)
                .build()
        }
    }

    private class EffectsButton(private val effects: Collection<PotionEffect>) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            val builder = ItemBuilder(Material.POTION).name("${CC.PRIMARY}Potion Effects")

            if (effects.isEmpty()) {
                builder.lore("${CC.PRIMARY}No effects")
            } else {
                val lore = effects.map { effect ->
                    val name = "${PotionUtil.getName(effect.type)} ${effect.amplifier + 1}"
                    val duration = " (${TimeUtil.millisToTimer((effect.duration / 20 * 1000).toLong())})"
                    "${CC.PRIMARY}$name${CC.SECONDARY}$duration"
                }
                builder.lore(lore)
            }

            return builder.build()
        }
    }
}
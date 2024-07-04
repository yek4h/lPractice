package net.lyragames.practice.ui.kit.buttons

import net.lyragames.practice.kit.Kit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 13/06/2024
*/

class KitDescriptionButton(
    val kit: Kit
) : Button() {
    override fun getButtonItem(player: Player?): ItemStack {
        val modes = mapOf(
            "Ranked Mode" to kit.ranked,
            "BedFights Mode" to kit.bedFights,
            "Combo Mode" to kit.combo,
            "Sumo Mode" to kit.sumo,
            "Build Mode" to kit.build,
            "HCF Mode" to kit.hcf,
            "Boxing Mode" to kit.boxing,
            "FFA Mode" to kit.ffa,
            "MLGRush Mode" to kit.mlgRush,
            "Bridge Mode" to kit.bridge,
            "Fireball Fight Mode" to kit.fireballFight,
            "Hunger Event" to kit.hunger,
            "Regeneration Event" to kit.regeneration,
            "Fall Damage Event" to kit.fallDamage
        )

        val modeDescriptions = modes.map { (mode, isEnabled) ->
            "&b$mode: &f${if (isEnabled) "&atrue" else "&cfalse"}"
        }

        return ItemBuilder(Material.SIGN)
            .name("&b&lKit Description")
            .lore(listOf(
                "",
                "&bYou're currently editing this kit!",
                "",
                "&bKit name: &f${kit.name}",
                "&bDisplay Name: &f${kit.displayName}",
                "&bUnranked Position: &f${kit.unrankedPosition}",
                "&bRanked Position: &f${kit.rankedPosition}"
            ) + modeDescriptions)
            .build()
    }
}
package net.lyragames.practice.ui.kit.buttons

import net.lyragames.practice.kit.Kit
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

class KitInfoButton(
    val kit: Kit
): Button() {
    override fun getButtonItem(p0: Player?): ItemStack {
        return ItemBuilder(kit.displayItem.type)
            .durability(kit.displayItem.durability.toInt())
            .clearLore()
            .amount(1)
            .name("&b&l${kit.name} " +
                    (if (kit.enabled) "&7(&aEnabled&7)" else "&7(&cDisabled&7)")
            ).build()
    }
}
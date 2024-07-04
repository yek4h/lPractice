package net.lyragames.practice.ui.kit.buttons

import net.lyragames.practice.kit.Kit
import net.lyragames.practice.ui.kit.KitMetadataList
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 14/06/2024
*/

class KitMetadataButton(
  val kit: Kit
) : Button() {
    override fun getButtonItem(p0: Player?): ItemStack {
        return ItemBuilder(Material.ENCHANTED_BOOK)
            .enchantment(Enchantment.DURABILITY, 10)
            .name("&b&lKit Metadata")
            .lore(listOf(
                "",
                "&7Click to edit the metadata of this kit",
                "&7this option is for enable or disable some values",
                ""
            ))
            .build()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
        player.updateInventory()
        player.closeInventory()

        KitMetadataList(kit).openMenu(player)
    }
}
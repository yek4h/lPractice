package net.lyragames.practice.kit.admin

import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class AdminKitManageMenu(private val kit: Kit): Menu() {

    override fun getTitle(p0: Player?): String {
        return "Managing ${kit.name} kit"
    }

    override fun getSize(): Int {
        return 27
    }

    override fun getButtons(p0: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        toReturn[11] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NAME_TAG)
                    .name("${CC.SECONDARY}Choose from our presets")
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                KitPresetMenu(kit).openMenu(player)
            }
        }

        toReturn[15] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.REDSTONE_BLOCK)
                    .name("${CC.SECONDARY}Settings")
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                KitSettingsMenu(kit).openMenu(player)
            }
        }

        return toReturn
    }
}
package net.lyragames.practice.kit.editor

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/21/2022
 * Project: lPractice
 */

class KitEditorSelectKitMenu: Menu() {

    override fun getTitle(player: Player?): String {
        return "Select a kit"
    }

    override fun getButtons(player: Player?): Map<Int, Button> {
        val buttons: MutableMap<Int, Button> = mutableMapOf()
        PracticePlugin.instance.kitManager.kits.values.forEach { kit ->
            if (kit.enabled) {
                buttons[buttons.size] = KitDisplayButton(kit)
           }
        }
        return buttons
    }

    private class KitDisplayButton(private val kit: Kit) : Button() {

        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(kit.displayItem)
                .clearLore()
                .name("${CC.PRIMARY}${kit.name}")
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
            player.closeInventory()
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            profile.kitEditorData?.kit = kit
            KitManagementMenu(kit).openMenu(player)
        }
    }
}
package net.lyragames.practice.ui.kit

import net.lyragames.practice.kit.Kit
import net.lyragames.practice.ui.kit.buttons.KitDescriptionButton
import net.lyragames.practice.ui.kit.buttons.KitInfoButton
import net.lyragames.practice.ui.kit.buttons.KitMetadataButton
import net.lyragames.practice.ui.kit.buttons.KitNameButton
import net.lyragames.practice.ui.kit.buttons.misc.KitArmorButton
import net.lyragames.practice.ui.kit.buttons.misc.KitContentsButton
import net.lyragames.practice.ui.kit.buttons.misc.KitDisplayNameButton
import net.lyragames.practice.ui.kit.buttons.misc.KitQueuePosButton
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu
import rip.katz.api.utils.CC
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 13/06/2024
*/

class KitCommandMenuEditor(
    val kit: Kit
): Menu() {
    override fun getTitle(p0: Player?): String {
         return CC.color("&bKit editor prompt!")
    }

    override fun isUpdateAfterClick(): Boolean {
        return true
    }

    override fun getButtons(p0: Player?): MutableMap<Int, Button> {
        val buttons = HashMap<Int, Button>()
        for (i in 0..35) {
            buttons[i] = object : Button() {
                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .durability(7)
                        .clearLore()
                        .clearEnchantments()
                        .build()
                }
            }
        }

        buttons[10] = KitInfoButton(kit)
        buttons[19] = KitDescriptionButton(kit)

        buttons[13] = KitNameButton(kit)
        buttons[14] = KitDisplayNameButton(kit)
        buttons[15] = KitMetadataButton(kit)
        buttons[16] = KitQueuePosButton(kit)

        buttons[23] = KitArmorButton(kit)
        buttons[24] = KitContentsButton(kit)
        return buttons
    }

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 9 * 4
    }
}
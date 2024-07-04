package net.lyragames.practice.ui.kit

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.prompt.KitCreatePrompt
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.NullConversationPrefix
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu
import rip.katz.api.menu.pagination.PaginatedMenu
import rip.katz.api.utils.CC
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 12/06/2024
*/

class KitMenu: PaginatedMenu() {
    override fun getPrePaginatedTitle(p0: Player): String {
        return CC.color("&bKit menu editor!")
    }

    override fun getAllPagesButtons(p0: Player): MutableMap<Int, Button> {
        val buttons = HashMap<Int, Button>()
        val sorted = PracticePlugin.instance.kitManager.getKits().sortedBy { it.name }

        for ((index, kit) in sorted.withIndex()) {
            buttons[index] = object : Button() {
                override fun getButtonItem(p0: Player): ItemStack {
                    return ItemBuilder(kit.displayItem)
                        .amount(1)
                        .name(CC.color(kit.displayName ?: kit.name))
                        .lore(CC.color(
                            listOf(
                                "",
                                "&7Click to edit this kit!"
                            )))
                        .build()
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
                    if (clickType.isLeftClick) {
                        KitCommandMenuEditor(kit).openMenu(player)
                    } else if (clickType.isRightClick) {
                        PracticePlugin.instance.kitManager.deleteKit(kit.name)
                        player.sendMessage(CC.color("&aThe kit has been successfully deleted!"))
                    }
                }
            }
        }
        return buttons
    }

    override fun getGlobalButtons(player: Player?): MutableMap<Int, Button> {
        val buttons = HashMap<Int, Button>()

        buttons[4] = object : Button () {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.GOLD_NUGGET)
                    .name("&bClick here to create a kit!")
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
                player.closeInventory()
                player.beginConversation(
                    ConversationFactory(PracticePlugin.instance).withModality(true).withPrefix(NullConversationPrefix())
                        .withFirstPrompt(KitCreatePrompt(player)).withEscapeSequence("/no").withLocalEcho(false)
                        .withTimeout(25).thatExcludesNonPlayersWithMessage("Go away evil console").buildConversation(player)
                )
            }
        }

        return buttons
    }
}
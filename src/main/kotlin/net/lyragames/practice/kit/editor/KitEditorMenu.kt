package net.lyragames.practice.kit.editor

import lombok.AllArgsConstructor
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.EditedKit
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import net.lyragames.practice.utils.PlayerUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu
import rip.katz.api.menu.buttons.DisplayButton
import java.util.*


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/20/2022
 * Project: lPractice
 */

class KitEditorMenu(private val index: Int): Menu() {

    private val ITEM_POSITIONS = intArrayOf(
        20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35, 38, 39, 40, 41, 42, 43, 44, 47, 48, 49, 50, 51, 52,
        53
    )
    private val BORDER_POSITIONS = intArrayOf(1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 28, 37, 46)
    private val BORDER_BUTTON = Button.placeholder(Material.COAL_BLOCK, 0.toByte(), " ")

    override fun getTitle(player: Player): String {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        return "${CC.PRIMARY}Editing: ${CC.SECONDARY}" + profile.kitEditorData?.kit?.name
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons: MutableMap<Int, Button> = mutableMapOf()
        for (border in BORDER_POSITIONS) {
            buttons[border] = BORDER_BUTTON
        }
        buttons[0] = CurrentKitButton()
        buttons[2] = SaveButton()
        buttons[6] = LoadDefaultKitButton()
        buttons[7] = ClearInventoryButton()
        buttons[8] = CancelButton(index)

        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        //val kit = profile.kitEditorData?.kit
        val kitLoadout: EditedKit? = profile.kitEditorData?.selectedKit

        buttons[18] = ArmorDisplayButton(kitLoadout?.armorContent?.get(3))
        buttons[27] = ArmorDisplayButton(kitLoadout?.armorContent?.get(2))
        buttons[36] = ArmorDisplayButton(kitLoadout?.armorContent?.get(1))
        buttons[45] = ArmorDisplayButton(kitLoadout?.armorContent?.get(0))


        Arrays.stream(kitLoadout?.editContents).forEach {
            for (i in 20 until 26) {
                var itemStack = it
                itemStack = kitLoadout?.editContents?.get(i - 20) ?: return@forEach
                if (itemStack != null) {
                    buttons.remove(i)
                    buttons[i] = RefillableItemButton(itemStack)
                }
            }
        }
      /*  val items: Array<ItemStack>? = kit?.content//kit.getEditRules().getEditorItems()
        if (!items?.isNotEmpty()!!) {
            for (i: Int in items.indices) {
                buttons[ITEM_POSITIONS[i]] = InfiniteItemButton(items[i])
            }
        }*/
        return buttons
    }

    override fun onOpen(player: Player) {
        if (!isClosedByMenu) {
            PlayerUtil.reset(player)
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            profile.kitEditorData?.active = true

            if (profile.kitEditorData?.selectedKit != null && profile.kitEditorData?.selectedKit?.content != null) {
                player.inventory.contents = profile.kitEditorData?.selectedKit?.content
            }
            player.updateInventory()
        }
    }

    override fun onClose(player: Player) {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        profile.kitEditorData?.active = false
        if (profile.state != ProfileState.MATCH) {
            object : BukkitRunnable() {
                override fun run() {
                    Hotbar.giveHotbar(profile)
                }
            }.runTask(PracticePlugin.instance)
        }
    }

    private class ArmorDisplayButton(private val itemStack: ItemStack?) : Button() {

        override fun getButtonItem(player: Player?): ItemStack {
            return if (itemStack == null || itemStack.type === Material.AIR) {
                ItemStack(Material.AIR)
            } else ItemBuilder(itemStack.clone())
                .name(CC.AQUA + itemStack.type.name)
                .lore(CC.YELLOW + "This is automatically equipped.")
                .build()
        }
    }

    private class CurrentKitButton : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            return ItemBuilder(Material.NAME_TAG)
                .name("${CC.PRIMARY}Editing: ${CC.SECONDARY}${profile.kitEditorData!!.kit!!.name}")
                .build()
        }
    }

    private class ClearInventoryButton : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.STAINED_CLAY)
                .durability(7)
                .name("${CC.PRIMARY}Clear Inventory")
                .lore(
                    listOf(
                        "&eThis will clear your inventory",
                        "&eso you can start over."
                    )
                )
                .build()
        }

        override fun clicked(player: Player, i: Int, clickType: ClickType?, hb: Int) {
            playNeutral(player)
            player.inventory.contents = arrayOfNulls<ItemStack>(36)
            player.updateInventory()
        }

        override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
            return true
        }
    }

    @AllArgsConstructor
    private class LoadDefaultKitButton : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.STAINED_CLAY)
                .durability(7)
                .name("${CC.PRIMARY}Load default kit")
                .lore(
                    listOf(
                        "${CC.PRIMARY}Click this to load the default kit",
                        "${CC.PRIMARY}into the kit editing menu."
                    )
                )
                .build()
        }

        override fun clicked(player: Player, i: Int, clickType: ClickType?, hb: Int) {
            playNeutral(player)
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            player.inventory.contents = profile.kitEditorData!!.kit!!.content
            player.updateInventory()
        }

        override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
            return true
        }
    }

    private class SaveButton : Button() {

        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.STAINED_CLAY)
                .durability(5)
                .name("&aSave")
                .lore("${CC.PRIMARY}Click this to save your kit.")
                .build()
        }

        override fun clicked(player: Player, i: Int, clickType: ClickType?, hb: Int) {
            playNeutral(player)
            player.closeInventory()
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            if (profile.kitEditorData?.kit != null) {
                profile.kitEditorData!!.selectedKit?.content = player.inventory.contents
            }
            //Hotbar.giveHotbarItems(player)
            KitManagementMenu(profile.kitEditorData?.kit!!).openMenu(player)
        }
    }

    private class CancelButton(private val index: Int) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.STAINED_CLAY)
                .durability(14)
                .name("&cCancel")
                .lore(
                    listOf(
                        "${CC.PRIMARY}Click this to abort editing your kit,",
                        "${CC.PRIMARY}and return to the kit menu."
                    )
                )
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
            playNeutral(player)
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            if (profile.kitEditorData?.kit != null) {
                val kitData = profile.getKitStatistic(profile.kitEditorData!!.kit!!.name)
                kitData?.replaceKit(index, null)
                KitManagementMenu(profile.kitEditorData?.kit!!).openMenu(player)
            }
        }


    }

    private class InfiniteItemButton(itemStack: ItemStack?) :
        DisplayButton(itemStack, false) {
        override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbar: Int) {
            val inventory: Inventory = player.openInventory.topInventory
            val itemStack: ItemStack = inventory.getItem(slot)
            inventory.setItem(slot, itemStack)
            player.itemOnCursor = itemStack
            player.updateInventory()
        }
    }

    inner class RefillableItemButton(itemStack: ItemStack?, cancel: Boolean = false) : DisplayButton(itemStack, cancel) {
        override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
            val inv = player.openInventory.topInventory
            val item = inv.getItem(slot)

            inv.setItem(slot, item)

            player.itemOnCursor = item
            player.updateInventory()
        }
    }
}
package net.lyragames.practice.kit.editor

import lombok.AllArgsConstructor
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.kit.EditedKit
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.buttons.BackButton


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/20/2022
 * Project: lPractice
 */

class KitManagementMenu(val kit: Kit): Menu() {

    private val PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, 7.toByte(), " ")

    init {
        isPlaceholder = true
        isUpdateAfterClick = false
    }

    override fun getTitle(player: Player): String {
        return "Viewing " + kit.name + " kits"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons: MutableMap<Int, Button> = mutableMapOf()

        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val kitLoadouts: MutableList<EditedKit?>? = profile.getKitStatistic(kit.name)?.editedKits

        var startPos = -1

        for (i in 0..3) {
            startPos += 2

            var kitLoadout: EditedKit? = null

            if (kitLoadouts!!.size > i) {
                kitLoadout = kitLoadouts[i]
            }

            buttons[startPos] = if (kitLoadout == null) CreateKitButton(i) else KitDisplayButton(kitLoadout)
            buttons[startPos + 18] = LoadKitButton(i)
            buttons[startPos + 27] = if (kitLoadout == null) PLACEHOLDER else RenameKitButton(kit, kitLoadout)
            buttons[startPos + 36] = if (kitLoadout == null) PLACEHOLDER else DeleteKitButton(kit, kitLoadout)
        }

        buttons[36] = BackButton(KitEditorSelectKitMenu())

        return buttons
    }

    override fun onClose(player: Player) {
        if (!isClosedByMenu) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            profile.kitEditorData?.kit = null

            profile.save(true)
        }
    }

    private class DeleteKitButton(private val kit: Kit?, private val kitLoadout: EditedKit) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.STAINED_CLAY)
                .name("&cDelete")
                .durability(14)
                .lore(
                    listOf(
                        "&cClick to delete this kit.",
                        "&cYou will &lNOT &cbe able to",
                        "&crecover this Kit Loadout."
                    )
                )
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

            profile.getKitStatistic(kit!!.name)?.deleteKit(kitLoadout)
            KitManagementMenu(kit).openMenu(player)
        }
    }

    private class CreateKitButton(private val index: Int) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.IRON_SWORD)
                .name("${CC.PRIMARY}Create Kit")
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            val kit = profile.kitEditorData?.kit

            if (kit == null) {
                player.closeInventory()
                return
            }

            val kitLoadout = EditedKit("Kit " + (index + 1))

            kitLoadout.armorContent = kit.armorContent
            kitLoadout.content = kit.content
            kitLoadout.editContents = kit.editorItems

            profile.getKitStatistic(kit.name)?.replaceKit(index, kitLoadout)
            profile.kitEditorData?.selectedKit = kitLoadout

            profile.save(true)

            KitEditorMenu(index).openMenu(player)
        }
    }

    private class RenameKitButton(private val kit: Kit, private val kitLoadout: EditedKit) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.SIGN)
                .name("${CC.PRIMARY}Rename")
                .lore(CC.translate("${CC.PRIMARY}Click to rename this kit."))
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarSlot: Int) {
            //currentlyOpenedMenus[player.uniqueId]?.isClosedByMenu = true
            player.updateInventory()
            player.closeInventory()
           // player.sendMessage(Locale.KIT_EDITOR_START_RENAMING.format(kitLoadout.getCustomName()))
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
            profile.kitEditorData?.kit = kit
            profile.kitEditorData?.selectedKit = kitLoadout
            profile.kitEditorData?.active = true
            profile.kitEditorData?.rename = true
        }
    }

    private class LoadKitButton(private val index: Int) : Button() {

        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.BOOK)
                .name("${CC.PRIMARY}Load/Edit")
                .lore(CC.translate("${CC.PRIMARY}Click to edit this kit."))
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarSlot: Int) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

            if (profile.kitEditorData?.kit == null) {
                player.closeInventory()
                return
            }

            var kit = profile.kitEditorData?.kit?.name?.let { profile.getKitStatistic(it) }?.editedKits?.get(index)

            if (kit == null) {
                kit = EditedKit("Kit " + (index + 1))
            }

            if (kit.content == null || kit.armorContent == null) {
                kit.content = profile.kitEditorData?.kit?.content
                kit.armorContent = profile.kitEditorData?.kit?.armorContent
            }

            if (kit.editContents == null) {
                kit.editContents = profile.kitEditorData?.kit?.editorItems
            }

            profile.getKitStatistic(kit.name)?.replaceKit(index, kit)

            profile.kitEditorData?.selectedKit = kit

            profile.save(true)

            KitEditorMenu(index).openMenu(player)
        }
    }

    @AllArgsConstructor
    private class KitDisplayButton(private val kitLoadout: EditedKit) : Button() {
        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(Material.BOOK)
                .name("${CC.PRIMARY}${kitLoadout.name}")
                .build()
        }
    }
}
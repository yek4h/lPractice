package net.lyragames.practice.ui.events.brackets

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.event.impl.BracketsEvent
import net.lyragames.practice.event.procedure.BracketEventProcedure
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.EventManager
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/21/2022
 * Project: lPractice
 */

class EventSelectKitMenu: Menu() {

    override fun getTitle(player: Player): String {
        return "Select a kit!"
    }

    override fun onClose(player: Player?) {
        if (!isClosedByMenu) {
            BracketEventProcedure.procedures.remove(player?.uniqueId)
        }
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            if (kit.sumo) continue

            toReturn[toReturn.size] = object : Button() {

                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(kit.displayItem)
                        .name("${CC.YELLOW}${kit.name}")
                        .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)
                        .build()
                }

                override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    val procedure = BracketEventProcedure.procedures[player?.uniqueId]

                    if (procedure == null) {
                        player?.sendMessage("${CC.RED}Something went wrong!")
                        return
                    }

                    procedure.kit = kit

                    val event = BracketsEvent(player?.uniqueId!!, procedure.eventMap, kit)

                    BracketEventProcedure.procedures.remove(procedure.uuid)
                    isClosedByMenu = true
                    player.closeInventory()

                    EventManager.event = event
                    event.addPlayer(player)
                }
            }
        }

        return toReturn
    }
}
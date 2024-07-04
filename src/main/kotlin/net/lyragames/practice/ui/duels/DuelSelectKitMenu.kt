package net.lyragames.practice.ui.duels

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.duel.procedure.DuelProcedure
import net.lyragames.practice.duel.procedure.DuelProcedureStage
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.ArenaManager
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
 * Created: 2/28/2022
 * Project: lPractice
 */

class DuelSelectKitMenu: Menu() {

    override fun getTitle(p0: Player?): String {
        return "Select a kit"
    }

    override fun onClose(player: Player?) {
        if (!isClosedByMenu) {

            DuelProcedure.duelProcedures.removeIf { it.uuid == player?.uniqueId }

        }
    }

    override fun getButtons(p0: Player?): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            toReturn[toReturn.size] = object : Button() {
                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(kit.displayItem)
                        .clearLore()
                        .name(kit.displayName)
                        .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build()
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (clickType?.isLeftClick!!) {

                        val duelProcedure = DuelProcedure.getByUUID(player.uniqueId)

                        if (duelProcedure == null) {
                            player.sendMessage("${CC.RED}Something went wrong!")
                            player.closeInventory()
                            return
                        }

                        duelProcedure.kit = kit

                        if (player.hasPermission("lpractice.duel.arena")) {
                            duelProcedure.stage = DuelProcedureStage.ARENA
                            isClosedByMenu = true
                            DuelSelectArenaMenu().openMenu(player)
                        }else {
                            val arena = ArenaManager.getFreeArena(kit)

                            if (arena == null) {
                                player.sendMessage("${CC.RED}There is no free arena!")
                                isClosedByMenu = false
                                player.closeInventory()
                                return
                            }

                            duelProcedure.arena = arena
                            isClosedByMenu = true
                            player.closeInventory()

                            duelProcedure.create().send()

                            player.sendMessage("${CC.GREEN}Successfully sent duel request!")
                        }

                    }
                }
            }
        }

        return toReturn
    }
}
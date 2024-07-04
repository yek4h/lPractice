package net.lyragames.practice.party.duel.procedure.menu

import rip.katz.api.menu.Button
import rip.katz.api.menu.pagination.PaginatedMenu
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.duel.procedure.DuelProcedure
import net.lyragames.practice.party.duel.procedure.PartyDuelProcedure
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/4/2022
 * Project: lPractice
 */

class PartyDuelArenaSelectMenu: PaginatedMenu() {

    override fun getPrePaginatedTitle(p0: Player?): String {
        return "Select an arena"
    }

    override fun onClose(player: Player?) {
        if (!isClosedByMenu) {

            DuelProcedure.duelProcedures.removeIf { it.uuid == player?.uniqueId }

        }
    }

    override fun getAllPagesButtons(player: Player?): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        val duelProcedure = PartyDuelProcedure.getByUUID(player?.uniqueId!!)

        val kit = duelProcedure?.kit

        for (arena in Arena.arenas) {
            if (!arena.isSetup || arena.duplicate) continue
            if (kit?.build!! && arena.arenaType != ArenaType.STANDALONE) continue
            if (kit.mlgRush && arena.arenaType != ArenaType.STANDALONE) continue
            if (kit.bedFights && arena.arenaType != ArenaType.STANDALONE) continue
            if (arena.arenaType != ArenaType.SHARED) continue

            toReturn[toReturn.size] = object : Button() {

                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(Material.PAPER)
                        .name("${CC.PRIMARY}${arena.name}")
                        .build()
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (clickType?.isLeftClick!!) {

                        if (duelProcedure == null) {
                            player.sendMessage("${CC.RED}Something went wrong!")
                            player.closeInventory()
                            return
                        }

                        if (!arena.isFree()) {
                            player.sendMessage("${CC.RED}This arena is not free!")
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

        return toReturn
    }
}
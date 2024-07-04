package net.lyragames.practice.party.duel.procedure.menu

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Button
import rip.katz.api.menu.pagination.PaginatedMenu
import net.lyragames.practice.manager.PartyManager
import net.lyragames.practice.party.duel.procedure.PartyDuelProcedure
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 * Created: 3/5/2022
 * Project: lPractice
 *
 * Recoded by yek4h
 *
 */

class PartyDuelSelectPartyMenu : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player?): String {
        return "Select a party!"
    }

    override fun getAllPagesButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        PartyManager.parties.filter { it.uuid != profile.party }.forEach { party ->
            buttons[buttons.size] = object : Button() {

                override fun getButtonItem(player: Player?): ItemStack {
                    return ItemBuilder(Material.NETHER_STAR)
                        .name("${CC.PRIMARY}${Bukkit.getPlayer(party.leader)?.name}")
                        .lore(
                            listOf(
                                "${CC.PRIMARY}Member Count: ${CC.SECONDARY}${party.players.size}",
                                "${CC.PRIMARY}Members: ${CC.SECONDARY}${party.players.joinToString(", ${CC.SECONDARY}") { Bukkit.getPlayer(it)?.name ?: "Unknown" }}"
                            )
                        ).build()
                }

                override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (clickType?.isLeftClick == true) {
                        val duelProcedure = PartyDuelProcedure.getByUUID(player?.uniqueId!!)
                        duelProcedure?.party = party.uuid
                        isClosedByMenu = true
                        PartyDuelKitSelectMenu().openMenu(player)
                    }
                }
            }
        }

        return buttons
    }
}

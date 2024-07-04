package net.lyragames.practice.ui.party

import rip.katz.api.menu.Button
import rip.katz.api.menu.pagination.PaginatedMenu
import net.lyragames.practice.party.Party
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.hotbar.Hotbar
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
 * Created: 2/24/2022
 * Project: lPractice
 *
 * Recoded by yek4h
 *
 */

class PartyPlayersMenu(private val party: Party) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player?): String {
        return "Party Players"
    }

    override fun getSize(): Int {
        return 36
    }

    override fun getAllPagesButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        party.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { partyPlayer ->
            buttons[buttons.size] = object : Button() {

                override fun getButtonItem(player: Player?): ItemStack {
                    return ItemBuilder(Material.IRON_SWORD)
                        .name("&e${partyPlayer.name}")
                        .lore(
                            listOf(
                                "",
                                "&e&o(( left click to kick player ))",
                                "&e&o(( right click to ban player ))"
                            )
                        )
                        .skullBuilder()
                        .setOwner(partyPlayer.name)
                        .buildSkull()
                }

                override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (player?.uniqueId != party.leader) {
                        player?.sendMessage("${CC.RED}You can't do this!")
                        return
                    }

                    val profile = PracticePlugin.instance.profileManager.findById(partyPlayer.uniqueId)!!

                    when {
                        clickType?.isRightClick == true -> {
                            party.banned.add(partyPlayer.uniqueId)
                            party.players.remove(partyPlayer.uniqueId)
                            profile.party = null
                            Hotbar.giveHotbar(profile)
                            party.sendMessage("${CC.SECONDARY}${partyPlayer.name}${CC.PRIMARY} has been banned from the party!")
                        }
                        clickType?.isLeftClick == true -> {
                            party.players.remove(partyPlayer.uniqueId)
                            profile.party = null
                            Hotbar.giveHotbar(profile)
                            party.sendMessage("${CC.SECONDARY}${partyPlayer.name}${CC.PRIMARY} has been kicked from the party!")
                        }
                    }

                    player.closeInventory()
                    Hotbar.giveHotbar(profile)
                }
            }
        }

        return buttons
    }
}

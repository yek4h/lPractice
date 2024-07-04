package net.lyragames.practice.ui.party

import com.google.common.base.Joiner
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.party.Party
import net.lyragames.practice.party.PartyType
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.stream.Collectors

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/24/2022
 * Project: lPractice
 */

class PartyInformationMenu(private val party: Party): Menu() {

    override fun getTitle(player: Player): String {
        return "Party Information"
    }

    override fun getSize(): Int {
        return 27
    }

    override fun isUpdateAfterClick(): Boolean {
        return true
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        toReturn[10] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).name("${CC.PRIMARY}Party Leader")
                    .lore(listOf(
                        "${CC.PRIMARY}Leader: ${CC.SECONDARY}${Bukkit.getPlayer(party.leader).name}"
                    )).build()
            }
        }

        toReturn[13] = object : Button() {

            override fun getButtonItem(player: Player?): ItemStack {
                return ItemBuilder(Material.HOPPER).name("${CC.PRIMARY}Privacy")
                    .lore(listOf(
                        if (party.partyType == PartyType.PRIVATE) "&a⚫ &ePrivate" else "&7⚫ &ePrivate",
                        if (party.partyType == PartyType.PRIVATE) "&7⚫ &ePublic" else "&a⚫ &ePublic"
                    ))
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                if (party.leader != player.uniqueId) {
                    player.sendMessage("${CC.RED}You can't change the party's privacy mode!")
                    return
                }

                if (party.partyType == PartyType.PRIVATE) {
                    party.partyType = PartyType.PUBLIC
                }else {
                    party.partyType = PartyType.PRIVATE
                }

                player.sendMessage("${CC.GREEN}Successfully changed privacy mode!")
            }

            override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
                return true
            }
        }

        toReturn[16] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.BOOK).name("${CC.PRIMARY}Players")
                    .lore(listOf(
                        "${CC.PRIMARY}Players in party: ${CC.SECONDARY}${Joiner.on("&7, ${CC.SECONDARY}").join(party.players.stream()
                            .map { Bukkit.getPlayer(it).name }.collect(Collectors.toList()))}"
                    )).build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                PartyPlayersMenu(party).openMenu(player)
            }
        }

        return toReturn
    }
}
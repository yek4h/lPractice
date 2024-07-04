package net.lyragames.practice.ui.party.event

import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.manager.PartyManager
import net.lyragames.practice.ui.party.ffa.PartyFFAKitSelect
import net.lyragames.practice.ui.party.split.PartySplitKitSelect
import net.lyragames.practice.PracticePlugin
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
 * Created: 3/5/2022
 * Project: lPractice
 */

class PartyStartEventMenu: Menu() {

    override fun getTitle(player: Player): String {
        return "Start a party event!"
    }

    override fun getSize(): Int {
        return 27
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        toReturn[11] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.GOLD_AXE)
                    .name("${CC.PRIMARY}FFA")
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                if (clickType?.isLeftClick!!) {
                    val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

                    if (profile.party != null) {
                        val party = PartyManager.getByUUID(profile.party!!)

                        if (party?.leader == player.uniqueId) {

                            PartyFFAKitSelect(party!!).openMenu(player)
                        }else {
                            player.sendMessage("${CC.RED}Only party leader can start party events!")
                        }
                    }
                }
            }
        }

        toReturn[15] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.DIAMOND_AXE)
                    .name("${CC.PRIMARY}Split")
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
                if (clickType.isLeftClick) {
                    val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

                    if (profile.party != null) {
                        val party = PartyManager.getByUUID(profile.party!!)

                        if (party?.leader == player.uniqueId) {

                            PartySplitKitSelect(party!!).openMenu(player)

                        }else {
                            player.sendMessage("${CC.RED}Only party leader can start party events!")
                        }
                    }
                }
            }
        }

        return toReturn
    }
}
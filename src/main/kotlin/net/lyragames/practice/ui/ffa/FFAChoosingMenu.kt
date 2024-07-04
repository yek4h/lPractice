package net.lyragames.practice.ui.ffa

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.FFAManager
import net.lyragames.practice.match.ffa.FFAPlayer
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/26/2022
 * Project: lPractice
 */

class FFAChoosingMenu: Menu() {

    override fun getTitle(p0: Player?): String {
        return "FFA"
    }

    override fun getButtons(p0: Player?): MutableMap<Int, Button> {

        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            if (!kit.ffa || kit.build) continue

            val ffa = FFAManager.getByKit(kit)

            toReturn[toReturn.size] = object : Button() {

                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(kit.displayItem.clone()).name("${CC.PRIMARY}${kit.name}")
                        .lore("${CC.PRIMARY}Currently playing: ${CC.SECONDARY}${ffa.players.size}")
                        .build()
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (clickType?.isLeftClick!!) {
                        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

                        profile.state = ProfileState.FFA
                        profile.ffa = ffa.uuid

                        if (Constants.FFA_SPAWN != null ){
                            player.teleport(Constants.FFA_SPAWN)
                        }

                        val ffaPlayer = FFAPlayer(player.uniqueId, player.name)
                        ffa.players.add(ffaPlayer)

                        ffa.setup(ffaPlayer)
                        ffa.firstSetup(ffaPlayer)

                        player.closeInventory()
                        player.sendMessage("${CC.GREEN}Successfully joined FFA!")
                    }
                }
            }
        }

        return toReturn
    }
}
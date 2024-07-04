package net.lyragames.practice.ui.party.ffa

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.ArenaManager
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.impl.PartyFFAMatch
import net.lyragames.practice.party.Party
import net.lyragames.practice.party.PartyMatchType
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Bukkit
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

class PartyFFAKitSelect(private val party: Party): Menu() {

    override fun getTitle(p0: Player?): String {
        return "Select a kit!"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {

        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            if (kit.boxing || kit.bedFights || kit.mlgRush || kit.bridge || kit.fireballFight) continue

            toReturn[toReturn.size] = object : Button() {

                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(kit.displayItem).name("${CC.PRIMARY}${kit.name}").build()
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (clickType?.isLeftClick!!) {

                        if (party.players.size < 2) {
                            player.sendMessage("${CC.RED}You need at least 2 players to start a FFA match!")
                            return
                        }

                        val arena = ArenaManager.getFreeArena(kit)

                        if (arena == null) {
                            player.sendMessage("${CC.RED}There is no free arenas!")
                            return
                        }

                        val match = PartyFFAMatch(kit, arena)

                        for (uuid in party.players) {
                            val partyPlayer = Bukkit.getPlayer(uuid) ?: continue
                            val profile = PracticePlugin.instance.profileManager.findById(uuid)!!

                            profile.match = match.uuid
                            profile.matchObject = match
                            profile.state = ProfileState.MATCH
                            party.partyMatchType = PartyMatchType.FFA
                            match.addPlayer(partyPlayer, arena.l1!!)
                        }

                        Match.matches[match.uuid] = match

                        player.closeInventory()
                        match.start()
                    }
                }
            }
        }

        return toReturn
    }
}
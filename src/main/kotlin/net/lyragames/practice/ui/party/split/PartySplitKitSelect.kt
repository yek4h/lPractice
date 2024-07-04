package net.lyragames.practice.ui.party.split

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.manager.ArenaManager
import net.lyragames.practice.manager.MatchManager
import net.lyragames.practice.party.Party
import net.lyragames.practice.party.PartyMatchType
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class PartySplitKitSelect(private val party: Party): Menu() {

    override fun getTitle(player: Player?): String {
        return "Select a kit!"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        for (kit in PracticePlugin.instance.kitManager.kits.values) {
            buttons[buttons.size] = KitButton(kit, party)
        }

        return buttons
    }

    private class KitButton(
        private val kit: Kit,
        private val party: Party
    ) : Button() {

        override fun getButtonItem(player: Player?): ItemStack {
            return ItemBuilder(kit.displayItem).name("${CC.PRIMARY}${kit.name}").build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
            if (clickType?.isLeftClick == true) {
                if (party.players.size < 2) {
                    player.sendMessage("${CC.RED}You need at least 2 players to start a Split match!")
                    return
                }

                val arena = ArenaManager.getFreeArena(kit)

                if (arena == null) {
                    player.sendMessage("${CC.RED}There are no free arenas!")
                    return
                }

                player.closeInventory()
                MatchManager.createTeamMatch(kit, arena, null, true, party.players)
                party.partyMatchType = PartyMatchType.SPLIT
            }
        }
    }
}

package net.lyragames.practice.ui.kit.buttons.misc

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.prompt.KitContentsEditPrompt
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.NullConversationPrefix
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 13/06/2024
*/

class KitContentsButton(
    val kit: Kit
): Button() {
    override fun getButtonItem(p0: Player?): ItemStack {
        return ItemBuilder(Material.DIAMOND_SWORD)
            .name("&b&lContents Editor")
            .lore(listOf(
                "",
                "&7Click to edit the kit contents (includes armor and full inventory)",
                "&cYOU MUST BE HAVE THE ARMOR EQUIPPED",
                "&cIN YOUR INVENTORY",
                ""
            ))
            .build()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
        player.closeInventory()
        player.beginConversation(
            ConversationFactory(PracticePlugin.instance).withModality(true).withPrefix(NullConversationPrefix())
                .withFirstPrompt(KitContentsEditPrompt(kit, player)).withEscapeSequence("/no").withLocalEcho(false)
                .withTimeout(25).thatExcludesNonPlayersWithMessage("Go away evil console").buildConversation(player)
        )
    }
}
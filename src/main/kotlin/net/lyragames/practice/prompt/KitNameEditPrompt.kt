package net.lyragames.practice.prompt

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.ui.kit.KitCommandMenuEditor
import net.lyragames.practice.ui.kit.KitMenu
import net.lyragames.practice.ui.kit.KitMetadataList
import net.lyragames.practice.utils.CC
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 13/06/2024
*/

class KitNameEditPrompt(
    val kit: Kit,
    val player: Player
) : StringPrompt() {
    override fun getPromptText(p0: ConversationContext?): String {
        return "${ChatColor.YELLOW}Please type the new name to be set, or type ${ChatColor.RED}cancel ${ChatColor.YELLOW}to cancel."
    }

    override fun acceptInput(context: ConversationContext, input: String): Prompt? {
        if (input.equals("cancel", false)) {
            context.forWhom.sendRawMessage("${ChatColor.RED}Cancelled creating kit.")

            KitMenu().openMenu(player)
            return Prompt.END_OF_CONVERSATION
        }

        try {
            val kitManager = PracticePlugin.instance.kitManager
            val oldKitName = kit.name
            kit.name = input
            kitManager.save()
            context.forWhom.sendRawMessage("${CC.GREEN}The kit $oldKitName has been renamed to ${ChatColor.BOLD}$input")
            KitCommandMenuEditor(kit).openMenu(player)
        } catch (e: Exception) {
            e.printStackTrace()
            context.forWhom.sendRawMessage("${ChatColor.RED}There was an issue renaming this kit.")
        }
        return Prompt.END_OF_CONVERSATION
    }
}
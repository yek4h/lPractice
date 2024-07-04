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

class KitArmorPrompt(
    val kit: Kit,
    val player: Player
): StringPrompt() {
    override fun getPromptText(p0: ConversationContext?): String {
        return CC.translate("&ePlease type &aYes &eif you want to set the armor of the kit named &f${kit.name} &e, type &cNo &eor &ccancel &eif you don't want to set the armor \n&c&lYOU NEED TO HAVE THE ARMOR IN YOUR INVENTORY")
    }

    override fun acceptInput(context: ConversationContext, input: String): Prompt? {

        if (input.equals("cancel", true) || input.equals("no", true)) {
            context.forWhom.sendRawMessage("${ChatColor.RED}Kit armor wasn't set because you cancelled the procedure")

            KitCommandMenuEditor(kit).openMenu(player)
            return Prompt.END_OF_CONVERSATION
        }

        try {
            if (input.equals("yes", true)) {
                context.forWhom.sendRawMessage("${ChatColor.RED}Kit armor wasn't set because you cancelled the procedure")

                val kitManager = PracticePlugin.instance.kitManager

                kit.armorContent = player.inventory.armorContents
                kitManager.save()
                context.forWhom.sendRawMessage(CC.translate("&aYou successfully set the armor to the kit named &f${kit.name}"))
                KitCommandMenuEditor(kit).openMenu(player)
            }


        } catch (e: Exception) {
            e.printStackTrace()
            context.forWhom.sendRawMessage("${ChatColor.RED}There was an issue setting the armor of this kit.")
        }

        return Prompt.END_OF_CONVERSATION
    }

}
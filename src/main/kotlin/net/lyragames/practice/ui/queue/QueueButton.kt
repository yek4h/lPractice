package net.lyragames.practice.ui.queue

import rip.katz.api.menu.Button
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.queue.Queue
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class QueueButton(private val queue: Queue) : Button() {

    override fun getButtonItem(player: Player?): ItemStack {
        val playing = QueueManager.getPlayingCount(queue.kit, queue.type)

        return ItemBuilder(queue.kit.displayItem.clone())
            .amount(if (playing <= 0) 1 else playing)
            .name(queue.kit.displayName)
            .addFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS)
            .lore(
                listOf(
                    "${CC.PRIMARY}Playing: ${CC.SECONDARY}$playing",
                    "${CC.PRIMARY}Queuing: ${CC.GREEN}${queue.getPlayerCount()}",
                    "",
                    "${CC.PRIMARY}Click to play!"
                )
            ).build()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
        if (clickType?.isLeftClick == true) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

            if (profile.state == ProfileState.QUEUE) {
                player.sendMessage("${CC.RED}You are already in a queue!")
                return
            }

            QueueManager.addToQueue(player, queue.kit, queue.type)

            player.sendMessage(" ")
            player.sendMessage("${CC.PRIMARY}${CC.BOLD}${queue.type.name}")
            player.sendMessage("${CC.PRIMARY} ⚫ Ping Range: ${CC.SECONDARY}[${if (profile.settings.pingRestriction == 0) "Unrestricted" else profile.settings.pingRestriction}]")
            player.sendMessage("${CC.GRAY}${CC.ITALIC} Searching for match...")
            player.sendMessage(" ")

            Hotbar.giveHotbar(profile)
            player.closeInventory()
        }
    }
}

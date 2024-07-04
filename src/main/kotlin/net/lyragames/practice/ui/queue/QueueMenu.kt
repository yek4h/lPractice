package net.lyragames.practice.ui.queue

import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.queue.Queue
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import rip.katz.api.utils.ItemBuilder

class QueueMenu(private val queueType: QueueType) : Menu() {

    override fun getTitle(player: Player?): String {
        return when (queueType) {
            QueueType.RANKED -> "${CC.SECONDARY}${CC.BOLD}Ranked Queue"
            QueueType.UNRANKED -> "${CC.SECONDARY}${CC.BOLD}Unranked Queue"
        }
    }

    override fun getSize(): Int {
        return 45
    }

    override fun getButtons(player: Player?): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        val queues = QueueManager.queues.values
            .filter { it.type == queueType && it.kit.enabled }

        var queueIndex = 0

        for (i in 0 until 45) {
            if (BLACKLISTED_SLOTS.contains(i)) continue
            if (queueIndex >= queues.size) break

            toReturn[i] = QueueButton(queues[queueIndex])
            queueIndex++
        }

        for (slot in BLACKLISTED_SLOTS) {
            toReturn[slot] = object : Button() {
                override fun getButtonItem(player: Player): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .durability(7)
                        .name(" ").build()
                }
            }
        }

        return toReturn
    }

    companion object {
        val BLACKLISTED_SLOTS: List<Int> = listOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            17, 18, 26, 27, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44
        )
    }
}

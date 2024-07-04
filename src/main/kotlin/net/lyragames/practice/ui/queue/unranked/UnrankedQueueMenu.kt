package net.lyragames.practice.ui.queue.unranked

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.queue.QueueType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 15/06/2024
*/

class UnrankedQueueMenu : Menu() {

    private val menuSize = 9 * 4

    init {
        isAutoUpdate = true
        println("Initialized UnrankedQueueMenu with size: $menuSize")
    }

    override fun getTitle(player: Player?): String {
        return "Unranked Queue Menu"
    }

    override fun getSize(): Int {
        return menuSize
    }

    override fun getButtons(player: Player?): MutableMap<Int, Button> {
        val buttons = HashMap<Int, Button>()
        val kitManager = PracticePlugin.instance.kitManager

        for (size in 0 until menuSize) {
            buttons[size] = object : Button() {
                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .name("")
                        .durability(7)
                        .build()
                }
            }
        }

        kitManager.kits.values.forEach { kit ->
            val queue = QueueManager.findQueue(kit, QueueType.UNRANKED)
            if (queue != null) {
                buttons[kit.unrankedPosition] = UnrankedQueueButton(queue, kit)
            }
        }
        return buttons
    }
}
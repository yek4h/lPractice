package net.lyragames.practice.utils.item

import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

class CustomItemStack(val uuid: UUID, val itemStack: ItemStack) {
    var rightClick = false
    var removeOnClick = false
    var clicked: Consumer<PlayerInteractEvent>? = null

    fun create() {
        customItemStacks.add(this)
    }

    companion object {
        val customItemStacks: MutableList<CustomItemStack> = CopyOnWriteArrayList()

        @JvmStatic
        fun getByInteraction(event: PlayerInteractEvent): CustomItemStack? {
            val itemStack = event.item ?: return null
            return customItemStacks.firstOrNull {
                it.uuid == event.player.uniqueId && it.itemStack.isSimilar(itemStack)
            }
        }

        @JvmStatic
        fun removeAllByPlayer(playerUuid: UUID) {
            customItemStacks.removeIf { it.uuid == playerUuid }
        }

        @JvmStatic
        fun removeCustomItemStack(customItemStack: CustomItemStack) {
            customItemStacks.remove(customItemStack)
        }
    }
}
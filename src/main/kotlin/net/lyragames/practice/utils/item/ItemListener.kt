package net.lyragames.practice.utils.item

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.EnumSet

class ItemListener : Listener {
    companion object {
        private val RIGHT_ACTIONS = EnumSet.of(
            Action.RIGHT_CLICK_AIR,
            Action.RIGHT_CLICK_BLOCK
        )
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val customItemStack = CustomItemStack.getByInteraction(event) ?: return

        if (customItemStack.rightClick && event.action !in RIGHT_ACTIONS) return

        customItemStack.clicked?.accept(event)

        if (customItemStack.removeOnClick) {
            CustomItemStack.removeCustomItemStack(customItemStack)
        }
    }
}
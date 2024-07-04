package net.lyragames.practice.kit.admin

import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.queue.Queue
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu

class KitSettingsMenu(private val kit: Kit) : Menu() {

    override fun getTitle(player: Player): String {
        return "${CC.PRIMARY}Editing ${kit.name}"
    }

    override fun isUpdateAfterClick(): Boolean {
        return true
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        buttons[0] = ToggleButton(kit, "Enabled", Material.ENCHANTED_BOOK, { it.enabled }, { it.enabled = !it.enabled }, QueueType.UNRANKED)
        buttons[1] = ToggleButton(kit, "Build", Material.COBBLESTONE, { it.build }, { it.build = !it.build }, QueueType.UNRANKED)
        buttons[2] = ToggleButton(kit, "HCF", Material.FENCE, { it.hcf }, { it.hcf = !it.hcf }, QueueType.UNRANKED)
        buttons[3] = ToggleButton(kit, "Combo", Material.RAW_FISH, { it.combo }, { it.combo = !it.combo }, QueueType.UNRANKED, 3)
        buttons[4] = ToggleButton(kit, "Ranked", Material.DIAMOND_SWORD, { it.ranked }, { it.ranked = !it.ranked }, QueueType.RANKED)
        buttons[5] = ToggleButton(kit, "Sumo", Material.LEASH, { it.sumo }, { it.sumo = !it.sumo }, QueueType.UNRANKED)
        buttons[6] = ToggleButton(kit, "Boxing", Material.DIAMOND_CHESTPLATE, { it.boxing }, { it.boxing = !it.boxing }, QueueType.UNRANKED)
        buttons[7] = ToggleButton(kit, "MLGRush", Material.STICK, { it.mlgRush }, { it.mlgRush = !it.mlgRush }, QueueType.UNRANKED)
        buttons[8] = ToggleButton(kit, "Bed Fights", Material.BED, { it.bedFights }, { it.bedFights = !it.bedFights }, QueueType.UNRANKED)
        buttons[9] = ToggleButton(kit, "FFA", Material.GOLD_SWORD, { it.ffa }, { it.ffa = !it.ffa }, QueueType.UNRANKED)
        buttons[10] = ToggleButton(kit, "Hunger", Material.COOKED_BEEF, { it.hunger }, { it.hunger = !it.hunger }, QueueType.UNRANKED)
        buttons[11] = ToggleButton(kit, "Regeneration", Material.BONE, { it.regeneration }, { it.regeneration = !it.regeneration }, QueueType.UNRANKED)
        buttons[12] = ToggleButton(kit, "Fall Damage", Material.FEATHER, { it.fallDamage }, { it.fallDamage = !it.fallDamage }, QueueType.UNRANKED)
        buttons[13] = ToggleButton(kit, "Bridge", Material.STAINED_CLAY, { it.bridge }, { it.bridge = !it.bridge }, QueueType.UNRANKED, 11)
        buttons[14] = ToggleButton(kit, "Fireball Fight", Material.FIREBALL, { it.fireballFight }, { it.fireballFight = !it.fireballFight }, QueueType.UNRANKED)

        return buttons
    }

    private class ToggleButton(
        private val kit: Kit,
        private val name: String,
        private val material: Material,
        private val stateGetter: (Kit) -> Boolean,
        private val stateSetter: (Kit) -> Unit,
        private val queueType: QueueType,
        private val durability: Short = 0
    ) : Button() {

        override fun getButtonItem(player: Player?): ItemStack {
            val state = stateGetter(kit)
            return ItemBuilder(material)
                .durability(durability.toInt())
                .name("${CC.PRIMARY}$name")
                .lore(
                    listOf(
                        if (state) "${CC.GREEN}⚫ Enabled" else "${CC.RED}⚫ Enabled",
                        if (!state) "${CC.GREEN}⚫ Disabled" else "${CC.RED}⚫ Disabled"
                    )
                ).build()
        }

        override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
            stateSetter(kit)
            PracticePlugin.instance.kitManager.save()

            if (name == "Enabled") {
                handleQueueState()
            } else if (name == "Ranked") {
                handleRankedState()
            }
        }

        override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
            return true
        }

        private fun handleQueueState() {
            if (!kit.enabled) {
                QueueManager.queues.filter { it.key.first.name.equals(kit.name, true) }
                    .forEach { (_, queue) ->
                        queue.getQueueingPlayers().mapNotNull { PracticePlugin.instance.profileManager.findById(it.uuid) }
                            .forEach {
                                it.state = ProfileState.LOBBY
                                it.queuePlayer = null
                                Hotbar.giveHotbar(it)
                                it.player.sendMessage("${CC.RED}You have been removed from the queue.")
                            }
                        queue.kit.enabled = false
                    }
            } else {
                QueueManager.queues.filter { it.key.first.name.equals(kit.name, true) }
                    .forEach { (_, queue) -> queue.kit.enabled = true }
            }
        }

        private fun handleRankedState() {
            if (kit.ranked) {
                val queue = Queue(kit, QueueType.RANKED)
                QueueManager.queues[Pair(kit, QueueType.RANKED)] = queue
            } else {
                QueueManager.queues.remove(Pair(kit, QueueType.RANKED))
            }
        }
    }
}
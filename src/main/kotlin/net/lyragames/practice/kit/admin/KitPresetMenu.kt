package net.lyragames.practice.kit.admin

import net.lyragames.practice.PracticePlugin
import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class KitPresetMenu(val kit: Kit): Menu() {

    override fun getTitle(p0: Player?): String {
        return "${CC.SECONDARY}${CC.BOLD}Presets"
    }

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 36
    }

    override fun getButtons(p0: Player?): MutableMap<Int, Button> {
        val toReturn = mutableMapOf<Int, Button>()

        toReturn[10] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.LEASH)
                    .name("${CC.SECONDARY}Sumo")
                    .lore(listOf(
                        CC.CHAT_BAR,
                        "${CC.GRAY}Load the optimal settings for Sumo",
                        CC.CHAT_BAR
                    )).build()
            }
            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                kit.sumo = true
                kit.fallDamage = false
                kit.hunger = false
                kit.ffa = false

                PracticePlugin.instance.kitManager.save()
                player.sendMessage("${CC.PRIMARY}You have loaded the ${CC.SECONDARY}Sumo${CC.PRIMARY} preset!")
            }
        }

        toReturn[12] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.BED)
                    .name("${CC.SECONDARY}Bed Fights ${CC.GRAY}(Bedwars)")
                    .lore(listOf(
                        CC.CHAT_BAR,
                        "${CC.GRAY}Load the optimal settings for",
                        "${CC.GRAY}Bed Fights (Bedwars)",
                        CC.CHAT_BAR
                    )).build()
            }
            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                kit.bedFights = true
                kit.hunger = false
                kit.fallDamage = true
                kit.ffa = false
                kit.regeneration = true

                PracticePlugin.instance.kitManager.save()
                player.sendMessage("${CC.PRIMARY}You have loaded the ${CC.SECONDARY}BedFights${CC.PRIMARY} preset!")
            }
        }

        toReturn[14] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.STICK)
                    .name("${CC.SECONDARY}MLGRush")
                    .lore(listOf(
                        CC.CHAT_BAR,
                        "${CC.GRAY}Load the optimal settings for",
                        "${CC.GRAY}MLGRush",
                        CC.CHAT_BAR
                    )).build()
            }
            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                kit.mlgRush = true
                kit.hunger = false
                kit.fallDamage = true
                kit.ffa = false
                kit.regeneration = true

                PracticePlugin.instance.kitManager.save()
                player.sendMessage("${CC.PRIMARY}You have loaded the ${CC.SECONDARY}MLGRush${CC.PRIMARY} preset!")
            }
        }

        toReturn[16] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.CLAY)
                    .durability(11)
                    .name("${CC.SECONDARY}Bridge")
                    .lore(listOf(
                        CC.CHAT_BAR,
                        "${CC.GRAY}Load the optimal settings for",
                        "${CC.GRAY}Bridge",
                        CC.CHAT_BAR
                    )).build()
            }
            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                kit.bridge = true
                kit.hunger = false
                kit.fallDamage = false
                kit.ffa = false
                kit.regeneration = true

                PracticePlugin.instance.kitManager.save()
                player.sendMessage("${CC.PRIMARY}You have loaded the ${CC.SECONDARY}Bridge${CC.PRIMARY} preset!")
            }
        }

        toReturn[19] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.FIREBALL)
                    .name("${CC.SECONDARY}Fireball Fight")
                    .lore(listOf(
                        CC.CHAT_BAR,
                        "${CC.GRAY}Load the optimal settings for",
                        "${CC.GRAY}Fireball fights",
                        CC.CHAT_BAR
                    )).build()
            }
            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                kit.fireballFight = true
                kit.hunger = false
                kit.fallDamage = false
                kit.ffa = false
                kit.regeneration = true

                PracticePlugin.instance.kitManager.save()
                player.sendMessage("${CC.PRIMARY}You have loaded the ${CC.SECONDARY}Fireball fights${CC.PRIMARY} preset!")
            }
        }

        return toReturn
    }
}
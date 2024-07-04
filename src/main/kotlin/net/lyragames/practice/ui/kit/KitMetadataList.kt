package net.lyragames.practice.ui.kit

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.ui.kit.buttons.KitDescriptionButton
import net.lyragames.practice.ui.kit.buttons.KitInfoButton
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu
import rip.katz.api.menu.pagination.PaginatedMenu
import rip.katz.api.utils.CC
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 13/06/2024
*/

class KitMetadataList(private val kit: Kit) : PaginatedMenu() {

    override fun getPrePaginatedTitle(p0: Player?): String {
        return CC.color("&b&lKit Metadata Editor")
    }

    override fun isUpdateAfterClick(): Boolean {
        return true
    }

    override fun getAllPagesButtons(p0: Player?): MutableMap<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        // Default filler button
        val fillerButton = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.STAINED_GLASS_PANE)
                    .durability(7)
                    .name("").build()
            }
        }

        // Add filler buttons
        for (i in 0..35) {
            buttons[i] = fillerButton
        }

        // Add specific buttons
        buttons[1] = KitInfoButton(kit)
        buttons[10] = KitDescriptionButton(kit)

        buttons[3] = createToggleButton(Material.DIAMOND_SWORD, "&b&lRanked Mode", "&7This value toggles if the kit can be playable on ranked to win elo", kit.ranked) {
            kit.ranked = !kit.ranked
        }

        buttons[4] = createToggleButton(Material.COOKED_BEEF, "&b&lNo Hunger Value", "&7This value enables or disables the hunger on the matches using this kit", kit.hunger) {
            kit.hunger = !kit.hunger
        }

        buttons[5] = createInfoButton(Material.INK_SACK, 11, "&b&lPlayable on Party", "&7This value toggles if the kit can be playable on Party Games", "&bWe're working on this feature!")

        buttons[6] = createInfoButton(Material.INK_SACK, 3, "&b&lPlayable on Party FFA", "&7This value toggles if the kit can be playable on Party FFA Games", "&bWe're working on this feature!")

        buttons[7] = createToggleButton(Material.BED, "&b&lBedFight", "&7This value enables bed fights. The first player breaking the bed of the opponent can do a final kill and win the match. Funny, no?", kit.bedFights) {
            kit.bedFights = !kit.bedFights
        }

        buttons[12] = createInfoButton(Material.NAME_TAG, 0, "&b&lKnockback Select", "&7Click to select what knockback you want for your kit. This value enables custom knockbacks in a match!", "&bThe current knockback set for this kit is: &cnull")

        buttons[14] = createToggleButton(Material.FEATHER, "&b&lNo Fall Damage Value", "&7This value enables or disables the fall damage on the matches using this kit.", kit.fallDamage) {
            kit.fallDamage = !kit.fallDamage
        }

        buttons[16] = createToggleButton(Material.WOOD, "&b&lBuild Mode", "&7This value enables the build mode. With this, you can build in the match. &cTHIS VALUE REQUIRES STANDALONE ARENAS", kit.build) {
            kit.build = !kit.build
        }

        buttons[19] = KitInfoButton(kit)
        buttons[28] = KitDescriptionButton(kit)

        buttons[21] = createInfoButton(Material.WATER_LILY, 0, "&b&lBattle Rush", "", "&cWe're working on this, sorry :c")

        buttons[22] = createToggleButton(Material.DIAMOND_CHESTPLATE, "&b&lBoxing Mode", "&7This value enables the boxing type to a kit. When boxing is enabled, the first player who reaches 100 hits will win :D", kit.boxing) {
            kit.boxing = !kit.boxing
        }

        buttons[23] = createInfoButton(Material.DIAMOND_HOE, 0, "&b&lSpleef Mode", "&7This value enables the spleef mode. You can make your opponent fall down using shovels!", "&cWe're working on this :c sorry!")

        buttons[24] = createToggleButton(Material.FENCE, "&b&lHCF Mode", "&7Enables the build on the fight and the players need to trap the opponent and kill him to win", kit.hcf) {
            kit.hcf = !kit.hcf
        }

        buttons[25] = createToggleButton(Material.WATER_BUCKET, "&b&lMLGRush Mode", "&7Enables MLGRush mode.", kit.mlgRush) {
            kit.mlgRush = !kit.mlgRush
        }

        buttons[31] = createToggleButton(Material.LEASH, "&b&lSumo Mode", "&7This value enables the sumo mode. If you fall down from the platform into the water, you will die.", kit.sumo) {
            kit.sumo = !kit.sumo
        }

        buttons[33] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.RAW_FISH).durability(3)
                    .name("&b&lDamage Ticks Value")
                    .lore(CC.color(listOf(
                        "&7This values modifies the damageticks value in kits",
                        "&7Current value: &f${kit.damageTicks}"
                    )))
                    .build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                when (clickType) {
                    ClickType.LEFT -> {
                        kit.damageTicks++
                    }
                    ClickType.RIGHT -> {
                        kit.damageTicks--
                    }
                    else -> {}
                }
            }

        }

        return buttons
    }

    private fun createToggleButton(material: Material, name: String, description: String, value: Boolean, toggleAction: () -> Unit): Button {
        return object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                val status = if (value) "&aEnabled" else "&cDisabled"
                return ItemBuilder(material)
                    .name(CC.color(name))
                    .lore("", CC.color(description), "", "&bThe current value is: $status", "")
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
                PracticePlugin.instance.kitManager.save()
                toggleAction()
                player.sendMessage(CC.color("$name is now: ${if (value) "&aEnabled" else "&cDisabled"}"))
            }
        }
    }

    private fun createInfoButton(material: Material, durability: Int, name: String, vararg description: String): Button {
        return object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(material)
                    .durability(durability)
                    .name(CC.color(name))
                    .lore(*description.map { CC.color(it) }.toTypedArray())
                    .build()
            }
        }
    }

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 9 * 4
    }
}
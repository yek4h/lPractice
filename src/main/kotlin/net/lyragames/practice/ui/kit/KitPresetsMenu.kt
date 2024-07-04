package net.lyragames.practice.ui.kit

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.CC
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionType
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 16/06/2024
*/

class KitPresetsMenu(
    val kit: Kit,
    val player: Player
) : Menu() {
    override fun getTitle(p0: Player?): String {
        return CC.translate("&bKit Preset")
    }

    override fun getButtons(p0: Player?): MutableMap<Int, Button> {
        val buttons = HashMap<Int, Button>()
        repeat(36) { i ->
            buttons[i] = createEmptyButton()
        }

        buttons[10] = createPresetButton(
            Material.POTION, "&b&lNoDebuff Preset", 16421,
            listOf("", "&7Click to apply the NoDebuff preset to the &f${kit.name} &bkit", ""),
            createNoDebuffPreset()
        )

        buttons[12] = createPresetButton(
            Material.GOLDEN_APPLE, "&b&lGApple Preset", 1,
            listOf("", "&7Click to enable the GApple preset to the ${kit.name} kit", ""),
            createGApplePreset()
        )

        buttons[14] = createPresetButton(
            Material.BED, "&b&lBedFight Preset", 0,
            listOf("", "&7Click to enable the BedFight preset to the ${kit.name} kit", ""),
            createBedFightPreset()
        )

        buttons[16] = createPresetButton(
            Material.FLINT_AND_STEEL, "&b&lClassic Kit", 0,
            listOf("", "&7Click to enable the Classic preset to the ${kit.name} kit", ""),
            createClassicPreset()
        )

        buttons[20] = createPresetButton(
            Material.LAVA_BUCKET, "&b&lBuildUHC Preset", 0,
            listOf("", "&7Click to enable the BuildUHC preset to the ${kit.name} kit", ""),
            createBuildUHCPreset()
        )

        buttons[22] = createPresetButton(
            Material.DIAMOND_PICKAXE, "&b&lFinalUHC Preset", 0,
            listOf("", "&7Click to enable the FinalUHC preset to the ${kit.name} kit", ""),
            createFinalUHCPreset()
        )

        buttons[24] = createPresetButton(
            Material.FENCE, "&b&lHCF Preset", 0,
            listOf("", "&7Click to enable the HCF preset to the ${kit.name} kit", ""),
            createHCFPreset()
        )

        return buttons
    }

    private fun createEmptyButton(): Button {
        return object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.STAINED_GLASS_PANE).durability(7)
                    .name("")
                    .build()
            }
        }
    }

    private fun createPresetButton(
        material: Material, name: String, durability: Int, lore: List<String>, presetAction: () -> Unit
    ): Button {
        return object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(material).durability(durability)
                    .name(name)
                    .lore(lore)
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, hotbarButton: Int) {
                presetAction()
                player.sendMessage(CC.translate("&aThe preset has been successfully set to the kit ${kit.name}"))
                PracticePlugin.instance.kitManager.save()
                player.closeInventory()
                KitCommandMenuEditor(kit).openMenu(player)
            }
        }
    }

    private fun createArmorPiece(material: Material, enchants: Map<Enchantment, Int>, durability: Int? = null): ItemStack {
        return ItemStack(material).apply {
            enchants.forEach { (enchant, level) -> addEnchantment(enchant, level) }
            durability?.let { this.durability = it.toShort() }
        }
    }

    private fun createWeapon(material: Material, enchants: Map<Enchantment, Int>): ItemStack {
        return ItemStack(material).apply {
            enchants.forEach { (enchant, level) -> addEnchantment(enchant, level) }
        }
    }

    private fun createBedFightPreset(): () -> Unit {
        return {
            val helmet = ItemStack(Material.LEATHER_HELMET)
            val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            val leggings = ItemStack(Material.LEATHER_LEGGINGS)
            val boots = ItemStack(Material.LEATHER_BOOTS)
            val sword = ItemStack(Material.WOOD_SWORD)
            val pickaxe = ItemStack(Material.WOOD_PICKAXE)
            val axe = ItemStack(Material.WOOD_AXE)
            val shears = ItemStack(Material.SHEARS)
            val wool = ItemStack(Material.WOOL, 64)

            val contents = arrayOfNulls<ItemStack>(36)

            fixInventorySlots(contents)

            contents[0] = sword
            contents[1] = pickaxe
            contents[2] = axe
            contents[3] = wool

            contents[8] = shears

            kit.content = contents.filterNotNull().toTypedArray()
            kit.armorContent = arrayOf(boots, leggings, chestplate, helmet)
            kit.build = true
            kit.ranked = true
            kit.hunger = true

        }
    }
    private fun createClassicPreset(): () -> Unit {
        return {
            val helmet = ItemStack(Material.DIAMOND_HELMET)
            val chestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
            val leggings = ItemStack(Material.DIAMOND_LEGGINGS)
            val boots = ItemStack(Material.DIAMOND_BOOTS)
            val arrow = ItemStack(Material.ARROW, 12)
            val bow = ItemStack(Material.BOW)
            val sword = ItemStack(Material.DIAMOND_SWORD)
            val rod = ItemStack(Material.BOW)
            val gapple = ItemStack(Material.GOLDEN_APPLE, 8)

            val contents = arrayOfNulls<ItemStack>(36)

            fixInventorySlots(contents)

            contents[0] = sword
            contents[1] = rod
            contents[2] = bow
            contents[3] = gapple

            contents[9] = arrow

            kit.content = contents.filterNotNull().toTypedArray()
            kit.armorContent = arrayOf(boots, leggings, chestplate, helmet)
            kit.build = false
            kit.ranked = true
            kit.hunger = true
        }
    }
    private fun createNoDebuffPreset(): () -> Unit {
        return {
            val enchants = mapOf(
                Enchantment.PROTECTION_ENVIRONMENTAL to 2,
                Enchantment.DURABILITY to 3
            )
            val boots = createArmorPiece(Material.DIAMOND_BOOTS, enchants + (Enchantment.PROTECTION_FALL to 4))
            val sword = createWeapon(Material.DIAMOND_SWORD, mapOf(
                Enchantment.DAMAGE_ALL to 3,
                Enchantment.DURABILITY to 3,
                Enchantment.FIRE_ASPECT to 2
            ))
            val enderPearls = ItemStack(Material.ENDER_PEARL, 16)
            val healingPotion = ItemBuilder(Material.POTION).durability(16421).name("").build()
            val speedPotion = Potion(PotionType.SPEED, 2).toItemStack(1)
            val strengthPotion = Potion(PotionType.FIRE_RESISTANCE, 1, false, true).toItemStack(1)
            val steak = ItemStack(Material.COOKED_BEEF, 64)
            val content = Array(36) { healingPotion }

            content[0] = sword
            content[1] = enderPearls
            content[2] = speedPotion
            content[3] = strengthPotion
            content[8] = steak
            content[17] = speedPotion
            content[26] = speedPotion
            content[35] = speedPotion

            kit.armorContent = arrayOf(boots, createArmorPiece(Material.DIAMOND_LEGGINGS, enchants),
                createArmorPiece(Material.DIAMOND_CHESTPLATE, enchants), createArmorPiece(Material.DIAMOND_HELMET, enchants))
            kit.content = content
            kit.build = false
            kit.ranked = true
            kit.hunger = true
        }
    }

    private fun createBuildUHCPreset(): () -> Unit {
        return {
            val enchants = mapOf(
                Enchantment.PROTECTION_ENVIRONMENTAL to 2
            )
            val boots = createArmorPiece(Material.DIAMOND_BOOTS, enchants)
            val sword = createWeapon(Material.DIAMOND_SWORD, mapOf(
                Enchantment.DAMAGE_ALL to 3
            ))
            val bow = createWeapon(Material.BOW, mapOf(
                Enchantment.ARROW_DAMAGE to 3
            ))
            val fishingRod = ItemStack(Material.FISHING_ROD)
            val goldenHead = ItemBuilder(Material.GOLDEN_APPLE).name("&eGolden Head").amount(3).build()
            val apple = ItemStack(Material.GOLDEN_APPLE, 6)
            val lavaBucket = ItemStack(Material.LAVA_BUCKET)
            val waterBucket = ItemStack(Material.WATER_BUCKET)
            val stoneBricks = ItemStack(Material.COBBLESTONE, 64)
            val planks = ItemStack(Material.WOOD, 64)
            val arrow = ItemStack(Material.ARROW, 20)
            val steak = ItemStack(Material.COOKED_BEEF, 64)
            val axe = ItemStack(Material.DIAMOND_AXE)
            val pickaxe = ItemStack(Material.DIAMOND_PICKAXE)
            val content = arrayOfNulls<ItemStack>(36)

            fixInventorySlots(content)

            content[0] = sword
            content[1] = fishingRod
            content[2] = bow
            content[3] = goldenHead
            content[4] = apple
            content[5] = stoneBricks
            content[6] = steak
            content[7] = lavaBucket
            content[8] = waterBucket
            content[18] = axe
            content[27] = pickaxe
            content[29] = arrow
            content[32] = planks
            content[34] = lavaBucket
            content[35] = waterBucket

            kit.armorContent = arrayOf(boots, createArmorPiece(Material.DIAMOND_LEGGINGS, enchants),
                createArmorPiece(Material.DIAMOND_CHESTPLATE, enchants), createArmorPiece(Material.DIAMOND_HELMET, enchants))
            kit.content = content.filterNotNull().toTypedArray()
            kit.build = true
            kit.ranked = true
            kit.hunger = true
        }
    }

    private fun createGApplePreset(): () -> Unit {
        return {
            val enchants = mapOf(
                Enchantment.PROTECTION_ENVIRONMENTAL to 4,
                Enchantment.DURABILITY to 3
            )
            val boots = createArmorPiece(Material.DIAMOND_BOOTS, enchants + (Enchantment.PROTECTION_FALL to 4))
            val sword = createWeapon(Material.DIAMOND_SWORD, mapOf(
                Enchantment.DAMAGE_ALL to 5,
                Enchantment.DURABILITY to 3,
                Enchantment.FIRE_ASPECT to 2
            ))
            val apple = ItemStack(Material.GOLDEN_APPLE, 64, 0, 1.toByte())
            val speedPotion = Potion(PotionType.SPEED, 2, false, true).toItemStack(1)
            val strengthPotion = Potion(PotionType.STRENGTH, 2, false, true).toItemStack(1)
            val content = arrayOfNulls<ItemStack>(36)

            fixInventorySlots(content)

            content[0] = sword
            content[1] = apple
            content[7] = speedPotion
            content[6] = strengthPotion
            content[15] = strengthPotion
            content[24] = strengthPotion
            content[33] = strengthPotion
            content[16] = speedPotion
            content[25] = speedPotion
            content[34] = speedPotion

            kit.armorContent = arrayOf(boots, createArmorPiece(Material.DIAMOND_LEGGINGS, enchants),
                createArmorPiece(Material.DIAMOND_CHESTPLATE, enchants), createArmorPiece(Material.DIAMOND_HELMET, enchants))
            kit.content = content.filterNotNull().toTypedArray()
            kit.build = false
            kit.ranked = true
            kit.hunger = true
        }
    }

    private fun createHCFPreset(): () -> Unit {
        return {
            val enchants = mapOf(
                Enchantment.PROTECTION_ENVIRONMENTAL to 2,
                Enchantment.DURABILITY to 3
            )
            val boots = createArmorPiece(Material.DIAMOND_BOOTS, enchants + (Enchantment.PROTECTION_FALL to 4))
            val sword = createWeapon(Material.DIAMOND_SWORD, mapOf(
                Enchantment.DAMAGE_ALL to 3,
                Enchantment.DURABILITY to 3,
                Enchantment.FIRE_ASPECT to 2
            ))
            val enderPearls = ItemStack(Material.ENDER_PEARL, 16)
            val healingPotion = ItemBuilder(Material.POTION).durability(16421).build()
            val speedPotion = Potion(PotionType.SPEED, 2).toItemStack(1)
            val rod = ItemStack(Material.FISHING_ROD)
            val apple = ItemStack(Material.GOLDEN_APPLE, 16)
            val strengthPotion = Potion(PotionType.FIRE_RESISTANCE, 1, false, true).toItemStack(1)
            val steak = ItemStack(Material.COOKED_BEEF, 64)
            val content = Array(36) { healingPotion }

            content[0] = sword
            content[1] = enderPearls
            content[2] = speedPotion
            content[3] = strengthPotion
            content[4] = apple
            content[5] = rod
            content[8] = steak
            content[17] = speedPotion
            content[26] = speedPotion
            content[35] = speedPotion

            kit.armorContent = arrayOf(boots, createArmorPiece(Material.DIAMOND_LEGGINGS, enchants),
                createArmorPiece(Material.DIAMOND_CHESTPLATE, enchants), createArmorPiece(Material.DIAMOND_HELMET, enchants))
            kit.content = content
            kit.build = false
            kit.ranked = true
            kit.hunger = true
        }
    }

    private fun createFinalUHCPreset(): () -> Unit {
        return {
            val enchants = mapOf(
                Enchantment.PROTECTION_ENVIRONMENTAL to 4
            )
            val boots = createArmorPiece(Material.DIAMOND_BOOTS, enchants, 240)
            val sword = createWeapon(Material.DIAMOND_SWORD, mapOf(
                Enchantment.DAMAGE_ALL to 3
            ))
            val fishingRod = ItemStack(Material.FISHING_ROD)
            val goldenHead = ItemBuilder(Material.GOLDEN_APPLE).name("&eGolden Head").amount(4).build()
            val apple = ItemStack(Material.GOLDEN_APPLE, 24)
            val lavaBucket = ItemStack(Material.LAVA_BUCKET)
            val waterBucket = ItemStack(Material.WATER_BUCKET)
            val stoneBricks = ItemBuilder(Material.COBBLESTONE).amount(64).build()
            val planks = ItemBuilder(Material.WOOD).amount(64).build()
            val flintAndSteel = ItemStack(Material.FLINT_AND_STEEL).apply { durability = 50 }
            val steak = ItemStack(Material.COOKED_BEEF, 64)
            val axe = ItemStack(Material.DIAMOND_AXE)
            val pickaxe = ItemStack(Material.DIAMOND_PICKAXE)
            val helmetInv = createArmorPiece(Material.DIAMOND_HELMET, enchants, 100)
            val chestplateInv = createArmorPiece(Material.DIAMOND_CHESTPLATE, enchants, 145)
            val leggingsInv = createArmorPiece(Material.DIAMOND_LEGGINGS, enchants, 145)
            val bootsInv = createArmorPiece(Material.DIAMOND_BOOTS, enchants, 120)
            val content = arrayOfNulls<ItemStack>(36)

            fixInventorySlots(content)

            content[0] = sword
            content[1] = fishingRod
            content[2] = goldenHead
            content[3] = apple
            content[4] = lavaBucket
            content[5] = stoneBricks
            content[6] = helmetInv
            content[7] = flintAndSteel
            content[8] = waterBucket
            content[14] = planks
            content[15] = chestplateInv
            content[18] = axe
            content[22] = lavaBucket
            content[23] = stoneBricks
            content[24] = leggingsInv
            content[26] = waterBucket
            content[27] = pickaxe
            content[29] = steak
            content[31] = lavaBucket
            content[32] = planks
            content[33] = bootsInv
            content[35] = waterBucket

            kit.armorContent = arrayOf(boots, createArmorPiece(Material.DIAMOND_LEGGINGS, enchants, 266),
                createArmorPiece(Material.DIAMOND_CHESTPLATE, enchants, 275), createArmorPiece(Material.DIAMOND_HELMET, enchants, 208))
            kit.content = content.filterNotNull().toTypedArray()
            kit.build = true
            kit.ranked = true
            kit.hunger = true
        }
    }

    private fun fixInventorySlots(content: Array<ItemStack?>) {
        val air = ItemStack(Material.AIR)
        repeat(content.size) { i ->
            content[i] = air
        }
    }
}
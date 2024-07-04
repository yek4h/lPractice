package net.lyragames.practice.profile.statistics

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.EditedKit
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.MatchType
import net.lyragames.practice.match.impl.TeamMatch
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.InventoryUtil
import net.lyragames.practice.utils.ItemBuilder
import net.lyragames.practice.utils.item.CustomItemStack
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 * Created: 2/15/2022
 * Project: lPractice
 *
 * Recoded by yek4h
 *
 */

class KitStatistic(val kit: String) {

    var elo = 1000
    var peakELO = 1000
    var wins = 0
    var losses = 0

    var rankedLosses = 0
    var rankedWins = 0
    var rankedStreak = 0
    var rankedBestStreak = 0
    var rankedDailyStreak = 0
    var currentStreak = 0
    var bestStreak = 0
    var currentCasualStreak = 0
    var bestCasualStreak = 0
    var currentDailyStreak = 0
    var bestDailyStreak = 0

    var editedKits: MutableList<EditedKit?> = mutableListOf(null, null, null, null)

    fun replaceKit(index: Int, loadout: EditedKit?) {
        editedKits[index] = loadout
    }

    fun deleteKit(loadout: EditedKit?) {
        editedKits.indices
            .firstOrNull { editedKits[it] == loadout }
            ?.let { editedKits[it] = null }
    }

    fun generateBooks(player: Player) {
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val kit = PracticePlugin.instance.kitManager.getKit(kit)

        if (editedKits.all { it == null }) {
            giveContents(player, profile, kit!!.content, kit.armorContent, false)
            return
        }

        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)
            val matchPlayer = match!!.getMatchPlayer(profile.uuid)

            if (matchPlayer!!.selectedKitArmor != null || matchPlayer.selectedKitContent != null) {
                giveContents(player, profile, matchPlayer.selectedKitContent!!, matchPlayer.selectedKitArmor!!, false)
                return
            }
        }

        val customItemStack = createCustomItemStack(player, "Default", kit!!.content, kit.armorContent)
        player.inventory.setItem(8, customItemStack.itemStack)

        profile.getKitStatistic(kit.name)?.editedKits?.forEachIndexed { i, editedKit ->
            if (editedKit != null) {
                val item = createCustomItemStack(player, editedKit.name, editedKit.content!!, editedKit.armorContent!!)
                player.inventory.setItem(i, item.itemStack)
            }
        }
    }

    private fun createCustomItemStack(player: Player, name: String, content: Array<ItemStack>, armorContent: Array<ItemStack>): CustomItemStack {
        val customItemStack = CustomItemStack(
            player.uniqueId,
            ItemBuilder(Material.BOOK)
                .enchantment(Enchantment.DURABILITY)
                .addFlags(ItemFlag.HIDE_ENCHANTS)
                .name("${CC.RED}$name").build()
        )
        customItemStack.rightClick = true
        customItemStack.removeOnClick = true
        customItemStack.clicked = Consumer {
            giveContents(player, PracticePlugin.instance.profileManager.findById(player.uniqueId)!!, content, armorContent, true)
        }
        customItemStack.create()
        return customItemStack
    }

    private fun giveContents(
        player: Player,
        profile: Profile,
        contents: Array<ItemStack>,
        armorContent: Array<ItemStack>,
        edit: Boolean
    ) {
        if (profile.state == ProfileState.MATCH) {
            val match = Match.getByUUID(profile.match!!)

            if (edit) {
                val matchPlayer = match!!.getMatchPlayer(player.uniqueId)
                matchPlayer!!.selectedKitContent = contents
                matchPlayer.selectedKitArmor = armorContent
            }

            if (match!!.getMatchType() in listOf(MatchType.TEAM, MatchType.BEDFIGHTS)) {
                val team = (match as TeamMatch).getTeamByPlayer(player.uniqueId)

                val content = contents.map { it.clone() }.toTypedArray()
                content.filter { it.type in listOf(Material.WOOL, Material.STAINED_CLAY) }
                    .forEach { it.durability = if (team!!.name.equals("Red", true)) 14 else 11 }

                player.inventory.contents = content

                val armorContents = armorContent.map { it.clone() }.toTypedArray()
                armorContents.filter { it.type.name.contains("LEATHER") }
                    .forEach { InventoryUtil.changeColor(it, if (team!!.name.equals("Red", true)) Color.RED else Color.BLUE) }

                player.inventory.armorContents = armorContents
                return
            }
        }

        player.inventory.contents = contents
        player.inventory.armorContents = armorContent

        player.updateInventory()
    }

    fun getKitCount(): Int {
        return editedKits.count { it != null }
    }
}

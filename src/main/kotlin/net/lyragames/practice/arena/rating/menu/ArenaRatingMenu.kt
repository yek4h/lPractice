package net.lyragames.practice.arena.rating.menu

import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.manager.ArenaRatingManager
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 6/3/2022
 * Project: lPractice
 */
class ArenaRatingMenu(val arena: Arena): Menu() {

    override fun getTitle(player: Player): String {
        return "${arena.name}'s Ratings"
    }

    override fun getSize(): Int {
        return 45
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        toReturn[4] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.STONE_BUTTON).name("${CC.PRIMARY}Average Rating: ${CC.SECONDARY}${ArenaRatingManager.getAverageRating(arena)}").build()
            }

        }

        toReturn[18] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).name("${CC.PRIMARY}1 star: ${CC.SECONDARY}${ArenaRatingManager.getUsersRated(1, arena)}").build()
            }
        }

        toReturn[20] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).amount(2).name("${CC.PRIMARY}2 stars: ${CC.SECONDARY}${ArenaRatingManager.getUsersRated(2, arena)}").build()
            }
        }

        toReturn[22] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).amount(3).name("${CC.PRIMARY}3 stars: ${CC.SECONDARY}${ArenaRatingManager.getUsersRated(3, arena)}").build()
            }
        }

        toReturn[24] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).amount(4).name("${CC.PRIMARY}4 stars: ${CC.SECONDARY}${ArenaRatingManager.getUsersRated(4, arena)}").build()
            }
        }

        toReturn[26] = object : Button() {
            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).amount(5).name("${CC.PRIMARY}5 stars: ${CC.SECONDARY}${ArenaRatingManager.getUsersRated(5, arena)}").build()
            }
        }

        return toReturn
    }
}
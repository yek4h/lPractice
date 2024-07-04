package net.lyragames.practice.ui

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.menu.Menu


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 3/23/2022
 * Project: lPractice
 */

class SettingsMenu: Menu() {

    override fun getTitle(p0: Player?): String {
        return "Settings"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val settings = profile?.settings

        toReturn[0] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.PAINTING).name("${CC.PRIMARY}Scoreboard")
                    .lore(listOf(
                        if (settings?.scoreboard!!) "${CC.GREEN}⚫ Enabled" else "${CC.RED}⚫ Enabled",
                        if (!settings.scoreboard) "${CC.GREEN}⚫ Disabled" else "${CC.RED}⚫ Disabled"
                    )).build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                settings?.scoreboard = !settings?.scoreboard!!
                profile.save(true)
            }

            override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
                return true
            }
        }

        toReturn[1] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.DIAMOND_SWORD).name("${CC.PRIMARY}Duels")
                    .lore(listOf(
                        if (settings?.duels!!) "${CC.GREEN}⚫ Enabled" else "${CC.RED}⚫ Enabled",
                        if (!settings.duels) "${CC.GREEN}⚫ Disabled" else "${CC.RED}⚫ Disabled"
                    )).build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                settings?.duels = !settings?.duels!!
                profile.save(true)
            }

            override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
                return true
            }
        }

        toReturn[2] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.REDSTONE).name("${CC.PRIMARY}Spectators")
                    .lore(listOf(
                        if (settings?.spectators!!) "${CC.GREEN}⚫ Enabled" else "${CC.RED}⚫ Enabled",
                        if (!settings.spectators) "${CC.GREEN}⚫ Disabled" else "${CC.RED}⚫ Disabled"
                    )).build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                settings?.spectators = !settings?.spectators!!
                profile.save(true)
            }

            override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
                return true
            }
        }

        toReturn[3] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.ENCHANTED_BOOK).name("${CC.PRIMARY}Ping Restriction")
                    .lore(listOf(
                        "${CC.PRIMARY}Ping Restriction: ${CC.SECONDARY}${if (settings?.pingRestriction == 0) "Unrestricted" else settings?.pingRestriction}"
                    )).build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                if (clickType?.isLeftClick!!) {
                    when(settings?.pingRestriction) {
                        300 -> {
                            settings.pingRestriction = 0
                        }

                        0 -> {
                            settings.pingRestriction = 50
                        }

                        50 -> {
                            settings.pingRestriction = 75
                        }

                        75 -> {
                            settings.pingRestriction = 100
                        }

                        100 -> {
                            settings.pingRestriction = 125
                        }

                        125 -> {
                            settings.pingRestriction = 150
                        }

                        150 -> {
                            settings.pingRestriction = 200
                        }

                        200 -> {
                            settings.pingRestriction = 250
                        }

                        250 -> {
                            settings.pingRestriction = 300
                        }
                    }
                    profile.save()
                }else if (clickType.isRightClick) {
                    when(settings?.pingRestriction) {
                        300 -> {
                            settings.pingRestriction = 250
                        }

                        0 -> {
                            settings.pingRestriction = 300
                        }

                        50 -> {
                            settings.pingRestriction = 0
                        }

                        75 -> {
                            settings.pingRestriction = 50
                        }

                        100 -> {
                            settings.pingRestriction = 75
                        }

                        125 -> {
                            settings.pingRestriction = 100
                        }

                        150 -> {
                            settings.pingRestriction = 125
                        }

                        200 -> {
                            settings.pingRestriction = 150
                        }

                        250 -> {
                            settings.pingRestriction = 200
                        }
                    }
                    profile?.save()
                }

            }

            override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
                return true
            }
        }

        toReturn[4] = object : Button() {

            override fun getButtonItem(p0: Player?): ItemStack {
                return ItemBuilder(Material.NETHER_STAR).name("${CC.PRIMARY}Map Rating")
                    .lore(listOf(
                        if (settings?.mapRating!!) "${CC.GREEN}⚫ Enabled" else "${CC.RED}⚫ Enabled",
                        if (!settings.mapRating) "${CC.GREEN}⚫ Disabled" else "${CC.RED}⚫ Disabled"
                    )).build()
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                settings?.mapRating = !settings?.mapRating!!
                profile.save(true)
            }

            override fun shouldUpdate(player: Player?, slot: Int, clickType: ClickType?): Boolean {
                return true
            }
        }



        return toReturn
    }


}
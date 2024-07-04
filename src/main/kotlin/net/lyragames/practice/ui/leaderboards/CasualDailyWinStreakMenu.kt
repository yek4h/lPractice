package net.lyragames.practice.ui.leaderboards

import dev.ryu.core.bukkit.CoreAPI
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.utils.CC
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
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
 * Date: 28/06/2024
*/

class CasualDailyWinStreakMenu(val p: PracticePlugin): Menu() {

    val GLASS_PANE = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37 ,38, 39, 40, 41 ,42 ,43, 44)

    override fun getTitle(p0: Player?): String {
        return CC.translate("&7Casual Daily Wins Leaderboards")
    }

    override fun getButtons(p0: Player): MutableMap<Int, Button> {
        val buttons = HashMap<Int, Button>()
        val occupiedSlots = GLASS_PANE.toMutableList()

        GLASS_PANE.forEach {

            buttons[it] = object : Button() {
                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(Material.STAINED_GLASS_PANE)
                        .name(" ")
                        .durability(7)
                        .build()
                }
            }


        }
        buttons[3] = object : Button() {
            override fun getButtonItem(p0: Player): ItemStack {
                val lb = p.leaderboards.getTopProfilesByGlobalElo()
                    .take(10)
                    .mapIndexed { index, (profile, elo) ->
                        val bestRank = CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(profile.uuid))
                        val color = ChatColor.valueOf(bestRank.color)
                        "${CC.PRIMARY}#${index + 1}&f. $color${profile.name} &7- &f$elo"
                            .replace("<top>", (index + 1).toString())
                            .replace("<name>", "$color${profile.name}")
                            .replace("<elo>", elo.toString())
                    }
                val lore= mutableListOf(
                    "&bYour score: ${p.profileManager.findById(p0.uniqueId)!!.globalStatistic.elo}"
                )
                lore.addAll(lb)

                return ItemBuilder(Material.NETHER_STAR)
                    .name("${CC.PRIMARY}Global Leaderboards")
                    .lore(CC.translate(lore))
                    .build()
            }


        }

        buttons[5] = object : Button() {
            override fun getButtonItem(p0: Player): ItemStack {
                return ItemBuilder(Material.SKULL_ITEM).setSkullTexture(p0.name)
                    .name("&bToggle View")
                    .lore(
                        CC.translate(listOf(
                            "&7Select one of the following",
                            "&7views to see other leaderboards!",
                            "",
                            "&fCurrent view:",
                            "&7Ranked ELO",
                            "&7Casual Wins",
                            "&7Ranked Wins",
                            "&a▸ Best Casual Daily Win Streak",
                            "&7Best Competitive Daily Win Streak",
                            "",
                            "&aClick to scroll though!"
                        )))
                    .build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                when (clickType) {
                    ClickType.LEFT -> {
                        player.updateInventory()
                        player.closeInventory()
                        CompetitiveDailyWinStreakMenu(p).openMenu(player)
                    }
                    ClickType.RIGHT -> {
                        player.updateInventory()
                        player.closeInventory()
                        RankedWinsMenu(p).openMenu(player)
                    }
                    else -> {}
                }
            }
        }

        var slot = 0
        for (kits in p.kitManager.kits.values) {
                while (occupiedSlots.contains(slot)) {
                    slot++
                }
                val lore = listOf(
                    "${CC.PRIMARY}Your score: &f${
                        p.profileManager.findById(p0.uniqueId)!!.getKitStatistic(kits.name)!!.bestCasualStreak
                    }"
                ).toMutableList()
                lore.addAll(p.leaderboards.getTopProfilesByKitCasualDailyWins(kits).sortedByDescending { it.second }
                    .take(10)
                    .mapIndexed { index, (profile, wins) ->
                        "${CC.PRIMARY}#<top>. &r<name> &7- &f<wins>"
                            .replace("<top>", (index + 1).toString())
                            .replace(
                                "<name>",
                                "${
                                    ChatColor.valueOf(
                                        CoreAPI.grantSystem.findBestRank(
                                            CoreAPI.grantSystem.repository.findAllByPlayer(profile.uuid)
                                        ).color
                                    )
                                }${profile.name}"
                            )
                            .replace("<wins>", wins.toString())
                    })
                buttons[slot] = object : Button() {
                    override fun getButtonItem(p0: Player?): ItemStack {
                        return ItemBuilder(kits.displayItem)
                            .durability(kits.displayItem.durability.toInt())
                            .name(CC.translate(kits.displayName))
                            .lore(lore)
                            .build()
                    }
                }
                occupiedSlots.add(slot)

        }

        return buttons
    }

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 9 * 5
    }
}
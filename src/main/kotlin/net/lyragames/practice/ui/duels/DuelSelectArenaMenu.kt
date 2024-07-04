package net.lyragames.practice.ui.duels

import rip.katz.api.menu.Button
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.duel.procedure.DuelProcedure
import net.lyragames.practice.manager.ArenaRatingManager
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.pagination.PaginatedMenu


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/28/2022
 * Project: lPractice
 */

class DuelSelectArenaMenu : PaginatedMenu() {

    override fun getPrePaginatedTitle(p0: Player?): String {
        return "Select an arena"
    }

    override fun onClose(player: Player?) {
        if (!isClosedByMenu) {
            DuelProcedure.duelProcedures.removeIf { it.uuid == player?.uniqueId }
        }
    }

    override fun getAllPagesButtons(player: Player): MutableMap<Int, Button> {
        val toReturn: MutableMap<Int, Button> = mutableMapOf()

        val duelProcedure = DuelProcedure.getByUUID(player.uniqueId) ?: return toReturn

        // Obtener arenas ordenadas por calificación promedio
        val sortedArenas = Arena.arenas.filter { it.isSetup && !it.duplicate }
            .sortedByDescending { ArenaRatingManager.getAverageRating(it) }

        for (arena in sortedArenas) {
            if (arena.arenaType == ArenaType.STANDALONE && !duelProcedure.kit!!.build) continue
            if (arena.arenaType == ArenaType.STANDALONE && !duelProcedure.kit!!.mlgRush) continue
            if (arena.arenaType == ArenaType.STANDALONE && !duelProcedure.kit!!.bedFights) continue
            if (arena.arenaType == ArenaType.STANDALONE && !duelProcedure.kit!!.bridge) continue
            if (arena.arenaType == ArenaType.STANDALONE && !duelProcedure.kit!!.fireballFight) continue

            val kitData = duelProcedure.kit!!

            if (arena.arenaType == ArenaType.SHARED && (kitData.build
                        || kitData.bedFights
                        || kitData.mlgRush
                        || kitData.sumo
                        || kitData.bridge
                        || kitData.fireballFight)) continue

            val averageRating = ArenaRatingManager.getAverageRating(arena)
            val color = when {
                averageRating >= 4.5 -> CC.GREEN
                averageRating >= 3.5 -> CC.YELLOW
                else -> CC.RED
            }
            val ratingText = "$averageRating❤"

            toReturn[toReturn.size] = object : Button() {

                override fun getButtonItem(p0: Player?): ItemStack {
                    return ItemBuilder(Material.PAPER)
                        .name("${CC.PRIMARY}${arena.name} $color$ratingText")
                        .build()
                }

                override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
                    if (clickType?.isLeftClick!!) {

                        if (!arena.isFree()) {
                            player.sendMessage("${CC.RED}This arena is not free!")
                            return
                        }

                        duelProcedure.arena = arena

                        isClosedByMenu = true
                        player.closeInventory()

                        duelProcedure.create().send()

                        player.sendMessage("${CC.GREEN}Successfully sent duel request!")
                    }
                }
            }
        }

        return toReturn
    }
}
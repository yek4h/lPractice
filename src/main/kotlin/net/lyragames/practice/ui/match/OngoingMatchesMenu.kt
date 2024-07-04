package net.lyragames.practice.ui.match

import rip.katz.api.menu.Menu
import rip.katz.api.menu.Button
import net.lyragames.practice.match.Match
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import rip.katz.api.menu.buttons.DisplayButton

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 05/06/2024
 */

class OngoingMatchesMenu : Menu() {
    override fun getTitle(player: Player?) = "${CC.PRIMARY}Ongoing Matches"

    override fun getButtons(player: Player?): MutableMap<Int, Button> {
        val buttonMap = mutableMapOf<Int, Button>()
        val ongoingMatches = Match.matches.values

        ongoingMatches.forEachIndexed { index, match ->
            val itemBuilder = ItemBuilder(Material.DIAMOND_SWORD)
                .name("${CC.PRIMARY}Match: ${CC.SECONDARY}${match.uuid}")
                .lore(
                    listOf(
                        "${CC.PRIMARY}Type: ${CC.SECONDARY}${match.getMatchType()}",
                        "${CC.PRIMARY}Kit: ${CC.SECONDARY}${match.kit.name}",
                        "${CC.PRIMARY}Arena: ${CC.SECONDARY}${match.arena.name}",
                        "${CC.PRIMARY}Players: ${CC.SECONDARY}${match.players.size}",
                        "${CC.PRIMARY}State: ${CC.SECONDARY}${match.matchState}",
                        "${CC.PRIMARY}Time: ${CC.SECONDARY}${match.getTime()}"
                    )
                )
                .amount(1)

            buttonMap[index] = DisplayButton(itemBuilder.build(), false)
        }

        return buttonMap
    }
}
package net.lyragames.practice.ui.queue.unranked

import dev.ryu.core.bukkit.CoreAPI
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.match.Match
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.leaderboards.Leaderboards
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.queue.Queue
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import rip.katz.api.menu.Button
import rip.katz.api.utils.ItemBuilder


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 15/06/2024
*/

class UnrankedQueueButton(
    val queue: Queue,
    val kit: Kit
): Button() {
    override fun getButtonItem(p0: Player): ItemStack {
        val playing = QueueManager.getPlayingCount(kit, QueueType.UNRANKED)

        val profile = PracticePlugin.instance.profileManager.findByIdRealTime(p0.uniqueId)!!
        val topThree = PracticePlugin.instance.leaderboards.getTopProfilesByDailyWinStreak(3)

        val lore = mutableListOf(
            "",
            "&fIn Queue: ${CC.PRIMARY}${queue.getPlayerCount()}",
            "&fFighting: ${CC.PRIMARY}$playing",
            "&fYour daily streak: ${CC.PRIMARY}${profile.globalStatistic.dailyWinStreak}",
        )

        lore.add("")
        lore.add("${CC.PRIMARY}Top Daily Win Streaks:")

        // Construir el top 3 lore con espacios vacíos si es necesario
        for (i in 0 until 3) {
            val line = if (i < topThree.size) {
                val (profile, winstreak) = topThree[i]
                "&f${i + 1}.- ${ChatColor.valueOf(CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(profile.uuid)).color)}${profile.name}&7: ${CC.PRIMARY}$winstreak"
            } else {
                "&f${i + 1}.- &c???&7: ${CC.PRIMARY}0"
            }
            lore.add(line)
        }

        lore.add("")
        lore.add("&aClick here to select ${kit.displayName}")
        return ItemBuilder(kit.displayItem.type)
            .name(kit.displayName ?: kit.name)
            .durability(kit.displayItem.durability.toInt())
            .lore(lore)
            .build()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType?, hotbarButton: Int) {
        if (clickType?.isLeftClick == true) {
            val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

            if (profile.state == ProfileState.QUEUE) {
                player.sendMessage("${CC.RED}You are already in a queue!")
                return
            }

            QueueManager.addToQueue(player, queue.kit, queue.type)
            player.sendMessage(CC.translate("&aYou're now queued for Unranked ${kit.displayName}"))

            /*player.sendMessage(" ")
            player.sendMessage("${CC.PRIMARY}${CC.BOLD}${queue.type.name}")
            player.sendMessage("${CC.PRIMARY} ⚫ Ping Range: ${CC.SECONDARY}[${if (profile.settings.pingRestriction == 0) "Unrestricted" else profile.settings.pingRestriction}]")
            player.sendMessage("${CC.GRAY}${CC.ITALIC} Searching for match...")
            player.sendMessage(" ")*/

            Hotbar.giveHotbar(profile)
            player.closeInventory()
        }
    }


}

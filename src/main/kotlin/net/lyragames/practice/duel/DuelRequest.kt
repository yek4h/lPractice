package net.lyragames.practice.duel

import dev.ryu.core.bukkit.CoreAPI
import net.lyragames.practice.Locale
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.PlayerUtil
import net.lyragames.practice.utils.TextBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/28/2022
 * Project: lPractice
 */

class DuelRequest(var uuid: UUID, var target: UUID, var kit: Kit, var arena: Arena) {

    var executedAt = System.currentTimeMillis()

    fun isExpired(): Boolean {
        return System.currentTimeMillis() - executedAt >= 60_000
    }

    fun send() {

        val player = Bukkit.getPlayer(target)
        val sender = Bukkit.getPlayer(uuid)

        val profile = PracticePlugin.instance.profileManager.findById(target)!!
        profile.duelRequests.add(this)

        val message = TextBuilder()             //.replace("<sender>", "${/*CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(sender.uniqueId)).color*/}${sender.name}").replace("<kit>", kit.displayName ?: kit.name)))

            .setText(CC.translate(Locale.DUEL_REQUEST.getMessage()
                .replace("<ping>", "${PlayerUtil.getPing(sender)}")
                .replace("<sender>", "${ChatColor.valueOf(CoreAPI.grantSystem.findBestRank(CoreAPI.grantSystem.repository.findAllByPlayer(sender.uniqueId)).color)}${sender.name}").replace("<kit>", kit.displayName ?: kit.name)))
            .then()
            .setText(Locale.DUEL_REQUEST_FOOTER.getMessage().replace("<arena>", arena.name))
            .then()
            .setText(Locale.CLICK_TO_ACCEPT.getMessage())
            .setCommand("/duel accept ${sender.name}")
            .then()
            .build()

        player.spigot().sendMessage(message)
    }
}
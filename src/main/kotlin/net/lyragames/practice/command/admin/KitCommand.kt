package net.lyragames.practice.command.admin

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.kit.admin.AdminKitManageMenu
import net.lyragames.practice.manager.QueueManager
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.statistics.KitStatistic
import net.lyragames.practice.ui.kit.KitMenu
import net.lyragames.practice.utils.CC
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class KitCommand {

    @Command(name = "", desc = "Kit setup commands")
    @Require("practice.command.kit.setup")
    fun help(@Sender sender: CommandSender) {
        sender.sendMessage("""
            ${CC.PRIMARY}Kit Commands:
            ${CC.SECONDARY}/create <name> - Create a Kit
            ${CC.SECONDARY}/content <kit> - Set the content of a Kit
            ${CC.SECONDARY}/items <kit> - Receive the item of a Kit
            ${CC.SECONDARY}/icon <kit> - Set the icon of a Kit
            ${CC.SECONDARY}/displayname <kit> <name> - Set the display name of a Kit
            ${CC.SECONDARY}/admin <kit> - Open the kit management menu
            ${CC.SECONDARY}/manage - Open the general management menu
        """.trimIndent())
    }

    @Command(name = "create", desc = "Create a Kit")
    @Require("practice.command.kit.create")
    fun create(@Sender sender: CommandSender, name: String) {
        if (PracticePlugin.instance.kitManager.getKit(name) != null) {
            sender.sendMessage(CC.RED + "That kit already exists!")
            return
        }

        PracticePlugin.instance.kitManager.createKit(name)

        CompletableFuture.runAsync {
            for (document in PracticePlugin.instance.mongoManager.profileCollection.find()) {
                val profile = Profile(UUID.fromString(document.getString("_id")), null)
                profile.load(document)

                profile.kitStatistics.add(KitStatistic(name))
                profile.save()
            }
        }

        sender.sendMessage("${CC.PRIMARY}Successfully created ${CC.SECONDARY}$name${CC.PRIMARY}!")
    }

    @Command(name = "content", desc = "Set the content of a kit")
    @Require("practice.command.kit.content")
    fun setContent(@Sender sender: CommandSender, kit: Kit) {
        val player = sender as? Player ?: return

        if (player.gameMode != GameMode.SURVIVAL) {
            player.sendMessage("${CC.RED}You must be in survival mode to set inventory contents!")
            return
        }

        kit.content = player.inventory.contents.clone()
        kit.armorContent = player.inventory.armorContents.clone()
        PracticePlugin.instance.kitManager.save()

        CompletableFuture.runAsync {
            for (document in PracticePlugin.instance.mongoManager.profileCollection.find()) {
                val profile = Profile(UUID.fromString(document.getString("uuid")), document.getString("name"))
                profile.load(document)

                val editedKits = profile.getKitStatistic(kit.name)?.editedKits ?: continue

                editedKits.fill(null)

                profile.save()
            }
        }

        player.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${kit.name}${CC.PRIMARY}'s item contents!")
    }

    @Command(name = "items", desc = "Receive the item of a Kit")
    @Require("practice.command.kit.items")
    fun getItems(@Sender sender: CommandSender, kit: Kit) {
        val player = sender as? Player ?: return

        player.inventory.contents = kit.content
        player.inventory.armorContents = kit.armorContent
        player.sendMessage("${CC.PRIMARY}Successfully retrieved ${CC.SECONDARY}${kit.name}${CC.PRIMARY}'s item contents!")
    }

    @Command(name = "kb", desc = "Receive the item of a Kit")
    @Require("practice.command.kit.kb")
    fun setKb(@Sender sender: CommandSender, kit: Kit, string: String) {
        val player = sender as? Player ?: return

        kit.knockbackProfile = string
        player.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${kit.name}${CC.PRIMARY} the kb named ${CC.SECONDARY}$string${CC.PRIMARY}!")
    }

    @Command(name = "icon", desc = "Set the icon of a Kit")
    @Require("practice.command.kit.icon")
    fun setIcon(@Sender sender: CommandSender, kit: Kit) {
        val player = sender as? Player ?: return

        if (player.itemInHand == null || player.itemInHand.type == Material.AIR) {
            player.sendMessage("${CC.RED}You are not holding an item!")
            return
        }
        kit.displayItem = player.itemInHand
        PracticePlugin.instance.kitManager.save()

        QueueManager.queues.filter { it.key.first.name.equals(kit.name, ignoreCase = true) }.forEach { (_, queue) ->
            queue.kit.displayItem = player.itemInHand
        }

        player.sendMessage("${CC.PRIMARY}Successfully set ${CC.SECONDARY}${kit.name}${CC.PRIMARY}'s display item!")
    }

    @Command(name = "admin", desc = "Open the kit management menu")
    @Require("practice.command.kit.admin")
    fun edit(@Sender sender: CommandSender, kit: Kit) {
        val player = sender as? Player ?: return
        AdminKitManageMenu(kit).openMenu(player)
    }

    @Command(name = "displayname", desc = "Set the name of a Kit")
    @Require("practice.command.kit.displayname")
    fun setDisplayName(@Sender sender: CommandSender, kit: Kit, name: String) {
        val player = sender as? Player ?: return

        kit.displayName = name
        player.sendMessage("${CC.YELLOW}You have updated ${CC.AQUA}${kit.name}${CC.YELLOW} to display as ${CC.GREEN}${kit.displayName}")
    }

    @Command(name = "manage", desc = "Open the general management menu")
    @Require("practice.command.kit.manage")
    fun manage(@Sender sender: CommandSender) {
        val player = sender as? Player ?: return
        KitMenu().openMenu(player)
    }
}
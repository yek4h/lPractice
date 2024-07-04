package net.lyragames.practice.command

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Require
import com.jonahseguin.drink.annotation.Sender
import net.lyragames.practice.Locale
import net.lyragames.practice.manager.MatchManager
import net.lyragames.practice.manager.PartyManager
import net.lyragames.practice.party.Party
import net.lyragames.practice.party.PartyType
import net.lyragames.practice.party.invitation.PartyInvitation
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.profile.hotbar.Hotbar
import net.lyragames.practice.utils.TextBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import rip.katz.api.utils.CC

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 17/06/2024
*/

class PartyCommand {

    @Command(name = "", desc = "Shows help for party commands")
    fun help(@Sender sender: CommandSender) {
        val player = sender as Player
        val helpMessages = """
            &bParty Commands - Page 1 of 1
            &f/party create &b- Create a new party
            &f/party disband &b- Disband your current party
            &f/party leave &b- Leave your current party
            &f/party invite <player> &b- Invite a player to your party
            &f/party join <player> &b- Join a player's party
            &f/party accept <player> &b- Accept a party invitation
            &f/party help &b- Shows this help message""".trimIndent()
        player.sendMessage(CC.color(helpMessages))
    }

    @Command(name = "create", desc = "Create a new party")
    fun create(@Sender sender: CommandSender) {
        val player = sender as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.party != null) {
            player.sendMessage(Locale.ALREADY_IN_PARTY.getMessage())
            return
        }

        val party = Party(player.uniqueId)
        party.players.add(player.uniqueId)

        PartyManager.parties.add(party)
        profile.party = party.uuid

        Hotbar.giveHotbar(profile)
        player.sendMessage(Locale.CREATED_PARTY.getMessage())
    }

    @Command(name = "disband", desc = "Disband your current party")
    fun disband(@Sender sender: CommandSender) {
        val player = sender as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.party == null) {
            player.sendMessage(Locale.NOT_IN_A_PARTY.getMessage())
            return
        }

        val party = PartyManager.getByUUID(profile.party!!)

        party?.players?.map { PracticePlugin.instance.profileManager.findById(it)!! }
            ?.forEach {
                it.party = null
                it.player.sendMessage(Locale.DISBANDED_PARTY.getMessage())
                Hotbar.giveHotbar(it)
            }

        PartyManager.parties.remove(party)
    }

    @Command(name = "leave", desc = "Leave your current party")
    fun leave(@Sender sender: CommandSender) {
        val player = sender as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.party == null) {
            player.sendMessage(Locale.NOT_IN_A_PARTY.getMessage())
            return
        }

        val party = PartyManager.getByUUID(profile.party!!)

        if (party?.leader == player.uniqueId) {
            party?.players?.filter { Bukkit.getPlayer(it) != null }?.map { PracticePlugin.instance.profileManager.findById(it)!! }
                ?.forEach {
                    it.party = null
                    it.player.sendMessage(Locale.DISBANDED_PARTY.getMessage())
                    Hotbar.giveHotbar(it)
                }

            PartyManager.parties.remove(party)
        } else {
            party?.players?.remove(player.uniqueId)
            profile.party = null
            Hotbar.giveHotbar(profile)
            party?.sendMessage(Locale.LEFT_PARTY.getMessage()
                .replace("<player>", player.name))
        }
    }

    @Command(name = "invite", desc = "Invite a player to your party")
    fun invite(@Sender sender: CommandSender, target: Player) {
        val player = sender as Player

        if (player.uniqueId == target.uniqueId) {
            player.sendMessage(Locale.CANT_INVITE_YOURSELF.getMessage())
            return
        }

        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!

        if (profile.party == null) {
            player.sendMessage(Locale.NOT_IN_A_PARTY.getMessage())
            return
        }

        val profile1 = PracticePlugin.instance.profileManager.findById(target.uniqueId)!!

        if (profile1.party != null) {
            player.sendMessage(Locale.PLAYER_ALREADY_IN_PARTY.getMessage())
            return
        }

        if (profile1.getPartyInvite(profile.party!!) != null) {
            player.sendMessage(Locale.ALREADY_INVITED_PLAYER.getMessage())
            return
        }

        val partyInvite = PartyInvitation(profile.party!!, target.uniqueId)
        profile1.partyInvites.add(partyInvite)

        val message = TextBuilder()
            .setText(Locale.PARTY_INVITED_MESSAGE.getMessage()
                .replace("<player>", Bukkit.getPlayer(PartyManager.getByUUID(profile.party!!)!!.leader).name))
            .then()
            .setText(Locale.CLICK_TO_JOIN.getMessage())
            .setCommand("/party join ${player.name}")
            .then()
            .build()

        target.spigot().sendMessage(message)
        player.sendMessage(Locale.PARTY_INVITED_MESSAGE.getMessage())
    }

    @Command(name = "join", desc = "Join a player's party")
    fun join(@Sender sender: CommandSender, target: Player) {
        val player = sender as Player

        if (player.uniqueId == target.uniqueId) {
            player.sendMessage(Locale.JOIN_OWN_PARTY.getMessage())
            return
        }

        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val profile1 = PracticePlugin.instance.profileManager.findById(target.uniqueId)!!

        if (profile.party != null) {
            player.sendMessage(Locale.ALREADY_IN_PARTY.getMessage())
            return
        }

        if (profile1.party == null) {
            player.sendMessage(Locale.ISNT_IN_PARTY.getMessage())
            return
        }

        val party = PartyManager.getByUUID(profile1.party!!)!!

        if (party.leader != target.uniqueId) {
            player.sendMessage(Locale.ISNT_IN_PARTY.getMessage())
            return
        }

        val partyInvitation = profile.getPartyInvite(profile1.party!!)

        if (party.banned.contains(player.uniqueId) == true) {
            player.sendMessage(Locale.BANNED_FROM_PARTY.getMessage())
            return
        }

        if (party.partyType == PartyType.PRIVATE && partyInvitation == null) {
            player.sendMessage(Locale.NOT_INVITED.getMessage())
            return
        }

        if (partyInvitation != null && partyInvitation.isExpired() && party.partyType == PartyType.PRIVATE) {
            player.sendMessage(Locale.PARTY_EXPIRED.getMessage())
            return
        }

        party.players.add(player.uniqueId)
        profile.party = party.uuid

        if (partyInvitation != null) {
            profile.partyInvites.remove(partyInvitation)
        }

        Hotbar.giveHotbar(profile)

        party.sendMessage(Locale.JOIN_PARTY_BROADCAST.getMessage().replace("<party>", player.name))
    }

    @Command(name = "accept", desc = "Accept a party invitation")
    fun partyaccept(@Sender sender: CommandSender, target: Player) {
        val player = sender as Player
        val profile = PracticePlugin.instance.profileManager.findById(player.uniqueId)!!
        val profile1 = PracticePlugin.instance.profileManager.findById(target.uniqueId)!!

        if (profile.state != ProfileState.LOBBY || profile1.state != ProfileState.LOBBY) {
            player.sendMessage(Locale.CANT_DO_THIS.getMessage())
            return
        }

        if (profile.party == null) {
            player.sendMessage(Locale.NOT_IN_A_PARTY.getNormalMessage())
            return
        }

        if (profile1.party == null) {
            player.sendMessage(Locale.OTHER_NOT_IN_A_PARTY.getMessage())
            return
        }

        if (profile.party == profile1.party) {
            player.sendMessage(Locale.JOINED_PARTY.getMessage()
                .replace("<target>", Bukkit.getPlayer(PartyManager.getByUUID(profile.party!!)!!.leader).name))
            return
        }

        val party = PartyManager.getByUUID(profile.party!!)!!
        val party1 = PartyManager.getByUUID(profile1.party!!)

        if (party.leader != player.uniqueId) {
            player.sendMessage(Locale.CANT_ACCEPT_PARTY_DUEL.getMessage())
            return
        }

        val duelRequest = party.getDuelRequest(profile1.uuid)

        if (duelRequest == null) {
            player.sendMessage(Locale.INVALID_DUEL.getMessage())
            return
        }

        MatchManager.createTeamMatch(
            duelRequest.kit!!,
            duelRequest.arena!!,
            null,
            true,
            party.players,
            party1!!.players
        )
    }
}

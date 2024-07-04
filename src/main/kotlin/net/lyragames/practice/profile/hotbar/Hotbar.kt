package net.lyragames.practice.profile.hotbar

import com.cryptomorin.xseries.XMaterial
import net.lyragames.practice.Locale
import net.lyragames.practice.event.EventState
import net.lyragames.practice.kit.editor.KitEditorSelectKitMenu
import net.lyragames.practice.manager.*
import net.lyragames.practice.match.Match
import net.lyragames.practice.party.Party
import net.lyragames.practice.party.duel.procedure.PartyDuelProcedure
import net.lyragames.practice.party.duel.procedure.menu.PartyDuelSelectPartyMenu
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.profile.ProfileState
import net.lyragames.practice.ui.ffa.FFAChoosingMenu
import net.lyragames.practice.ui.leaderboards.LeaderboardRankedMenu
import net.lyragames.practice.ui.party.PartyInformationMenu
import net.lyragames.practice.ui.party.event.PartyStartEventMenu
import net.lyragames.practice.ui.queue.ranked.RankedQueueMenu
import net.lyragames.practice.ui.queue.unranked.UnrankedQueueMenu
import net.lyragames.practice.utils.CC
import net.lyragames.practice.utils.ItemBuilder
import net.lyragames.practice.utils.item.CustomItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

object Hotbar {

    fun giveHotbar(profile: Profile) {
        val player = Bukkit.getPlayer(profile.uuid) ?: return
        player.inventory.clear()

        when (profile.state) {
            ProfileState.LOBBY -> setupLobbyHotbar(player, profile)
            ProfileState.QUEUE -> setupQueueHotbar(player, profile)
            ProfileState.EVENT -> setupEventHotbar(player, profile)
            ProfileState.SPECTATING -> setupSpectatingHotbar(player, profile)
            else -> {}
        }
    }

    private fun setupLobbyHotbar(player: Player, profile: Profile) {
        val items = mutableListOf<HotbarItem>()

        if (profile.party != null) {
            items.addAll(getPartyHotbarItems(profile))
        } else {
            items.addAll(getLobbyHotbarItems(profile))
        }

        setHotbarItems(player, items)
    }

    private fun setupQueueHotbar(player: Player, profile: Profile) {
        setHotbarItems(player, listOf(
            HotbarItem(8, XMaterial.RED_DYE.parseMaterial()!!, "&cLeave Queue") {
                profile.state = ProfileState.LOBBY
                profile.queuePlayer = null
                QueueManager.getQueue(profile.uuid)?.queuePlayers?.removeIf { it.uuid == player.uniqueId }
                giveHotbar(profile)
            }
        ))
    }

    private fun setupEventHotbar(player: Player, profile: Profile) {
        val items = mutableListOf<HotbarItem>()

        if (player.hasPermission("lpractice.command.event.forcestart")) {
            EventManager.event?.takeIf { it.state == EventState.ANNOUNCING }?.let {
                items.add(HotbarItem(0, Material.HOPPER, "&eForce Start") {
                    player.chat("/event forcestart")
                })
            }
        }

        items.add(HotbarItem(8, XMaterial.RED_DYE.parseMaterial()!!, "&cLeave Event") {
            EventManager.event?.removePlayer(player)
            Bukkit.broadcastMessage("${CC.GREEN}${player.name}${CC.YELLOW} has left the event. ${CC.GRAY}(${EventManager.event?.players?.size}/${EventManager.event?.requiredPlayers})")
        })

        setHotbarItems(player, items)
    }

    private fun setupSpectatingHotbar(player: Player, profile: Profile) {
        setHotbarItems(player, listOf(
            HotbarItem(8, XMaterial.RED_DYE.parseMaterial()!!, "&cLeave Spectating") {
                profile.state = ProfileState.LOBBY
                profile.spectatingMatch?.let { matchUUID ->
                    Match.getSpectator(matchUUID)?.removeSpectator(player)
                }
            }
        ))
    }

    private fun setHotbarItems(player: Player, items: List<HotbarItem>) {
        player.inventory.apply {
            items.forEach { item ->
                setItem(item.slot, createCustomItem(player, item.material, item.name, item.unbreakable, item.action))
            }
        }
    }

    private fun createCustomItem(player: Player, material: Material, name: String, unbreakable: Boolean = false, consumer: Consumer<PlayerInteractEvent>): ItemStack {
        val itemStack = ItemBuilder(material).name(name).apply {
            if (unbreakable) {
                addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)
                setUnbreakable(true)
            }
        }.build()

        return CustomItemStack(player.uniqueId, itemStack).apply {
            rightClick = true
            clicked = consumer
            create()
        }.itemStack
    }

    private fun getPartyHotbarItems(profile: Profile): List<HotbarItem> {
        return listOf(
            HotbarItem(0, Material.NETHER_STAR, "&eParty Information") {
                PartyManager.getByUUID(profile.party!!)?.let { PartyInformationMenu(it).openMenu(Bukkit.getPlayer(profile.uuid)) }
            },
            HotbarItem(4, Material.GOLD_AXE, "&eStart Party Event", true) {
                PartyStartEventMenu().openMenu(Bukkit.getPlayer(profile.uuid))
            },
            HotbarItem(5, Material.DIAMOND_AXE, "&eParty Duel", true) {
                PartyDuelProcedure(profile.uuid).apply {
                    PartyDuelProcedure.duelProcedures.add(this)
                }
                PartyDuelSelectPartyMenu().openMenu(Bukkit.getPlayer(profile.uuid))
            },
            HotbarItem(8, XMaterial.RED_DYE.parseMaterial()!!, "&cLeave Party") {
                val party = PartyManager.getByUUID(profile.party!!)!!
                if (party.leader == profile.uuid) {
                    party.players.forEach {
                        val memberProfile = PracticePlugin.instance.profileManager.findById(it)!!
                        memberProfile.party = null
                        memberProfile.player.sendMessage(Locale.DISBANDED_PARTY.getMessage())
                        giveHotbar(memberProfile)
                    }
                    PartyManager.parties.remove(party)
                } else {
                    party.players.remove(profile.uuid)
                    profile.party = null
                    giveHotbar(profile)
                    party.sendMessage(Locale.LEFT_PARTY.getMessage())
                }
            }
        )
    }

    private fun getLobbyHotbarItems(profile: Profile): List<HotbarItem> {
        return listOf(
            HotbarItem(0, Material.IRON_SWORD, "&aPlay Casual &7(Right Click)", true) {
                if (profile.state == ProfileState.LOBBY) UnrankedQueueMenu().openMenu(Bukkit.getPlayer(profile.uuid))
            },
            HotbarItem(1, Material.DIAMOND_SWORD, "&cPlay Competitive &7(Right Click)", true) {
                if (profile.state == ProfileState.LOBBY) RankedQueueMenu().openMenu(Bukkit.getPlayer(profile.uuid))
            },
            HotbarItem(2, Material.BOOK, "&6Edit Kit &7(Right Click)") {
                if (profile.state == ProfileState.LOBBY) KitEditorSelectKitMenu().openMenu(Bukkit.getPlayer(profile.uuid))
            },
            HotbarItem(4, Material.NETHER_STAR, "&dCreate Party &7(Right Click)") {
                if (profile.state == ProfileState.LOBBY) {
                    val party = Party(profile.uuid)
                    party.players.add(profile.uuid)
                    PartyManager.parties.add(party)
                    profile.party = party.uuid
                    giveHotbar(profile)
                    Bukkit.getPlayer(profile.uuid)?.sendMessage(Locale.CREATED_PARTY.getMessage())
                }
            },
            HotbarItem(6, Material.ITEM_FRAME, "&eView Leaderboards &7(Right Click)") {
                LeaderboardRankedMenu(PracticePlugin.instance).openMenu(profile.player)
            },
            HotbarItem(7, Material.EYE_OF_ENDER, "&bHost Events &7(Right Click)") {
                if (profile.state == ProfileState.LOBBY) Bukkit.getPlayer(profile.uuid)?.chat("/event host")
            },
            HotbarItem(8, Material.REDSTONE_COMPARATOR, "&bSettings &7(Right Click)") {
                if (profile.state == ProfileState.LOBBY) Bukkit.getPlayer(profile.uuid)?.chat("/settings")
            }
        )
    }

    private data class HotbarItem(
        val slot: Int,
        val material: Material,
        val name: String,
        val unbreakable: Boolean = false,
        val action: Consumer<PlayerInteractEvent>
    )
}
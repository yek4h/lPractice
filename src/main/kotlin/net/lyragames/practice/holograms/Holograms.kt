package net.lyragames.practice.holograms

import dev.yek4h.spigot.util.CC
import net.lyragames.practice.PracticePlugin
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * Author: yek4h © 2024
 * Date: 23/06/2024
 */

abstract class Holograms(val location: Location, val time: Int, val refreshHologram: Int, var name: String? = null, val distance: Double, val distanceCalcByListener: Boolean) {

    private val armorStands: MutableMap<Int, EntityArmorStand> = ConcurrentHashMap()
    private val previousLines: MutableList<String> = mutableListOf()
    val itemEntities: MutableMap<EntityItem, ItemStack> = ConcurrentHashMap()

    lateinit var run: BukkitTask
    val lines: MutableList<String> = ArrayList()
    var actualTime: Int = time
    var updatable: Boolean = true
    var updated = false
    var itemTop: Boolean = true

    fun start() {
        update()
        updateLines()

        if (updatable) {
            run = object : BukkitRunnable() {
                override fun run() {
                    try {
                        tick()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.runTaskTimerAsynchronously(PracticePlugin.instance, 20L, 20L * refreshHologram.toLong())
        } else {
            tick()
        }
    }

    fun stop() {
        if (this::run.isInitialized) {
            run.cancel()
        }
        armorStands.values.forEach { armorStand ->
            Bukkit.getOnlinePlayers().forEach { player ->
                hide(player, mapOf(armorStand to armorStand.customName))
            }
        }
        armorStands.clear()
    }

    abstract fun update()

    abstract fun updateLines()

    open fun tick() {
        updated = true
        actualTime--

        if (actualTime < 1) {
            actualTime = time
            update()
        }

        updateLines()
    }

    open fun updateLine(index: Int, line: String) {
        lines[index] = line
        val armorStand = armorStands[index]
        armorStand?.let {
            it.customName = line
            it.customNameVisible = !line.equals("<void>", ignoreCase = true)
            Bukkit.getOnlinePlayers().forEach { player ->
                updateArmorStand(player, it)
            }
        }
    }

    open fun addLine(line: String) {
        var y = location.y - (lines.size * 0.25)
        if (line.isBlank()) {
            y -= 0.25 // Add extra space for blank lines
            lines.add("<void>")
        } else {
            val stand = EntityArmorStand((location.world as CraftWorld).handle, location.x, y, location.z)
            stand.customName = line
            stand.customNameVisible = true
            stand.isInvisible = true
            stand.isSmall = true
            armorStands[lines.size] = stand
            lines.add(line)
            Bukkit.getOnlinePlayers().forEach { player ->
                show(player, mapOf(stand to line))
            }
        }
    }

    open fun removeExtraLines(newSize: Int) {
        for (i in newSize until lines.size) {
            val armorStand = armorStands[i]
            armorStand?.let {
                armorStands.remove(i)
                Bukkit.getOnlinePlayers().forEach { player ->
                    hide(player, mapOf(it to it.customName))
                }
            }
        }
        lines.subList(newSize, lines.size).clear()
    }

    open fun show(player: Player, standsToShow: Map<EntityArmorStand, String>) {
        standsToShow.forEach { (armorStand, line) ->
            armorStand.customName = line
            val connection = (player as CraftPlayer).handle.playerConnection
            connection.sendPacket(PacketPlayOutSpawnEntityLiving(armorStand))
            connection.sendPacket(PacketPlayOutEntityMetadata(armorStand.id, armorStand.dataWatcher, true))
        }
    }

    open fun updateArmorStand(player: Player, armorStand: EntityArmorStand) {
        val connection = (player as CraftPlayer).handle.playerConnection
        connection.sendPacket(PacketPlayOutEntityMetadata(armorStand.id, armorStand.dataWatcher, true))
    }

    open fun show(player: Player) {
        armorStands.values.forEach { armorStand ->
            val connection = (player as CraftPlayer).handle.playerConnection
            connection.sendPacket(PacketPlayOutSpawnEntityLiving(armorStand))
            connection.sendPacket(PacketPlayOutEntityMetadata(armorStand.id, armorStand.dataWatcher, true))
        }
    }

    open fun hide(player: Player) {
        armorStands.values.forEach { armorStand ->
            (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutEntityDestroy(armorStand.id))
        }
    }

    protected open fun hide(player: Player, armorStands: Map<EntityArmorStand, String>) {
        armorStands.keys.forEach { armorStand ->
            (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutEntityDestroy(armorStand.id))
        }
    }

    fun isPlayerNearLocation(player: Player, location: Location): Boolean {
        val playerLocation = player.location
        val distanceSquared = location.distanceSquared(playerLocation)
        val rangeSquared = distance.pow(2)

        return distanceSquared <= rangeSquared
    }
}
package net.lyragames.practice.arena.impl

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.Extent
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.function.mask.ExistingBlockMask
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.registry.WorldData
import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.utils.Cuboid
import net.lyragames.practice.utils.LocationUtil
import org.bukkit.Location
import org.bukkit.Material
import java.util.concurrent.ThreadLocalRandom


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/15/2022
 * Project: lPractice
 */

open class StandaloneArena(name: String) : Arena(name) {

    fun findClosestBlock(referencePoint: Location?, material: Material): Cuboid? {
        val world = referencePoint!!.world ?: return null
        val cuboid = Cuboid(min!!, max!!)
        val blocks = cuboid.blocks.filter { it.type == material }

        val closestBlock = blocks.minByOrNull { it.location.distance(referencePoint) } ?: return null

        val blockLocation = closestBlock.location
        val cuboidSize = 1 // Puedes ajustar el tamaño del Cuboid alrededor del bloque

        return Cuboid(
            Location(world, blockLocation.x - cuboidSize, blockLocation.y - cuboidSize, blockLocation.z - cuboidSize),
            Location(world, blockLocation.x + cuboidSize, blockLocation.y + cuboidSize, blockLocation.z + cuboidSize)
        )
    }

    fun findClosestBlockByLocation(referencePoint: Location?, material: Material): Location? {
        val world = referencePoint!!.world ?: return null
        val cuboid = Cuboid(min!!, max!!)
        val blocks = cuboid.blocks.filter { it.type == material }

        return blocks.minByOrNull { it.location.distance(referencePoint) }?.location
    }

    override fun isFree(): Boolean {
        return super.isFree() || duplicates.any { it.free && it.isSetup }
    }

    fun getFreeDuplicate(): Arena? {
        return duplicates.firstOrNull { it.free && it.isSetup }
    }
}
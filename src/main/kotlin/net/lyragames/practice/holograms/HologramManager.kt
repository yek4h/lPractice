package net.lyragames.practice.holograms

import net.lyragames.practice.utils.CC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player

/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * Author: yek4h © 2024
 * Date: 23/06/2024
 */

class HologramManager {

    private var holograms: MutableSet<Holograms> = mutableSetOf()

    init {
        println("[Katto] Started Holograms System!")
    }

    fun hologramCreation(location: Location, updateDelay: Int, refreshHologram: Int, name: String, list: MutableList<String>, updatable: Boolean, distance: Double, distanceCalcByListener: Boolean, toUpdate: () -> Unit) {
        val existingHologram = holograms.find { it.name == name }
        existingHologram?.stop()
        holograms.remove(existingHologram)

        val hologram = object : Holograms(location, updateDelay, refreshHologram, name, distance, distanceCalcByListener) {
            override fun update() {
                toUpdate()
            }

            override fun updateLines() {
                list.forEachIndexed { index, s ->
                    val line = CC.translate(s.replace("<updating>", "${this.actualTime}"))
                    if (index < this.lines.size) {
                        if (this.lines[index] != line) {
                            this.updateLine(index, line)
                        }
                    } else {
                        this.addLine(line)
                    }
                }
                // Remove any extra lines from previous updates
                if (this.lines.size > list.size) {
                    this.removeExtraLines(list.size)
                }
            }
        }

        hologram.updatable = updatable
        hologram.lines.clear()
        hologram.updateLines()
        holograms.add(hologram)
        hologram.start()
    }

    fun hologramDestroy(name: String) {
        val hologram = holograms.find { it.name == name }
        hologram?.let {
            it.stop()
            holograms.remove(it)
        }
    }

    fun hologramDestroyAll() {
        holograms.forEach {
            it.stop()
            holograms.remove(it)
        }
    }

    fun show(player: Player) {
        holograms.forEach { it.show(player) }
    }

    fun hide(player: Player) {
        holograms.forEach { it.hide(player) }
    }

    fun showHologram(hologramName: String, player: Player) {
        holograms.find { it.name == hologramName }?.show(player)
    }

    fun hideHologram(hologramName: String, player: Player) {
        holograms.find { it.name == hologramName }?.hide(player)
    }
}
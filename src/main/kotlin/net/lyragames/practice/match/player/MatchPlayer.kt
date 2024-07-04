package net.lyragames.practice.match.player

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.utils.countdown.ICountdown
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * This Project is property of Zowpy © 2021
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 12/19/2021
 * Project: Practice
 */

open class MatchPlayer(val uuid: UUID, val name: String, var spawn: Location, var initialElo: Int) {
    var dead = false
    var respawning = false
    var offline = false
    val player: Player
        get() = Bukkit.getPlayer(uuid)

    var lastDamager: UUID? = null

    var coloredName = name

    var selectedKitContent: Array<ItemStack>? = null
    var selectedKitArmor: Array<ItemStack>? = null

    var hits = 0
    var combo = 0
    var longestCombo = 0

    var comboed = 0

    var potionsThrown = 0
    var potionsMissed = 0

    // Agregar variables para W-Tap
    var wtapAttempts = 0
    var effectiveWTaps = 0
    var wtapAccuracy = 100.0 // Inicializar en 100.0

    // bed fights & mlgrush
    var points = 0
    var bedLocations: MutableList<Location> = mutableListOf()
    var respawnCountdown: ICountdown? = null

    val profile: Profile
        get() {
            return PracticePlugin.instance.profileManager.findById(uuid)!!
        }

    val onlineProfile: Profile?
        get() {
            return PracticePlugin.instance.profileManager.findById(uuid)
        }

    fun updateWtapAccuracy() {
        wtapAccuracy = if (wtapAttempts > 0) {
            (effectiveWTaps.toDouble() / wtapAttempts) * 100
        } else {
            100.0 // Si no hay intentos de W-Tap, la precisión es 100%
        }
    }
}
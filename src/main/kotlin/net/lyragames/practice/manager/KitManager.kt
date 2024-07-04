package net.lyragames.practice.manager

import net.lyragames.practice.PracticePlugin
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.utils.CC
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack

/**
 * This Project is property of Zowpy & EliteAres © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy & EliteAres
 * Created: 2/16/2022
 * Project: lPractice
 */

class KitManager {

    val kits = mutableMapOf<String, Kit>()
    var unrankedOrNot = true

    init {
        load()
    }

    fun load() {
        val configFile = PracticePlugin.instance.kitsFile.config
        val sections = configFile.getConfigurationSection("kits") ?: run {
            println("No kits section found in configuration.")
            return
        }

        kits.clear() // Limpiar el mapa antes de cargar
        println("Loading kits...")

        sections.getKeys(false).forEach { key ->
            val section = configFile.getConfigurationSection("kits.$key") ?: run {
                println("No section found for kit: $key")
                return@forEach
            }
            val data = section.getConfigurationSection("data") ?: run {
                println("No data section found for kit: $key")
                return@forEach
            }

            val displayItem = section.get("icon") as? ItemStack ?: run {
                println("No icon found for kit: $key")
                return@forEach
            }
            val displayName = section.getString("displayName", "${CC.SECONDARY}$key")

            val content = section.getList("content") as? List<ItemStack> ?: emptyList()
            val armorContent = section.getList("armorContent") as? List<ItemStack> ?: emptyList()

            val kit = Kit(
                name = key,
                displayItem = displayItem,
                content = content.toTypedArray(),
                armorContent = armorContent.toTypedArray(),
                enabled = data.getBoolean("enabled"),
                unrankedPosition = section.getInt("unrankedPos"),
                rankedPosition = section.getInt("rankedPos"),
                combo = data.getBoolean("combo"),
                sumo = data.getBoolean("sumo"),
                build = data.getBoolean("build"),
                hcf = data.getBoolean("hcf"),
                ffa = data.getBoolean("ffa"),
                ranked = data.getBoolean("ranked"),
                boxing = data.getBoolean("boxing"),
                mlgRush = data.getBoolean("mlgRush"),
                bedFights = data.getBoolean("bedFights"),
                bridge = data.getBoolean("bridge"),
                fireballFight = data.getBoolean("fireballFight"),
                hunger = data.getBoolean("hunger"),
                regeneration = data.getBoolean("regeneration"),
                fallDamage = data.getBoolean("fallDamage"),
                knockbackProfile = data.getString("knockbackProfiler"),
                displayName = displayName,
                damageTicks = data.getInt("damageTicks")
            )
            kits[key] = kit
            println("Loaded kit: $key")
        }
    }

    fun save() {
        val configFile = PracticePlugin.instance.kitsFile
        val fileConfig = configFile.config

        fileConfig.set("kits", null)
        println("Saving kits...")

        kits.values.forEach { kit ->
            fileConfig.set("kits.${kit.name}.icon", kit.displayItem)
            fileConfig.set("kits.${kit.name}.content", if (kit.content.isEmpty()) null else kit.content.toList())
            fileConfig.set("kits.${kit.name}.armorContent", if (kit.armorContent.isEmpty()) null else kit.armorContent.toList())
            fileConfig.set("kits.${kit.name}.displayName", kit.displayName)
            fileConfig.set("kits.${kit.name}.unrankedPos", kit.unrankedPosition)
            fileConfig.set("kits.${kit.name}.rankedPos", kit.rankedPosition)

            val dataPath = "kits.${kit.name}.data"
            setKitData(fileConfig, dataPath, kit)
            println("Saved kit: ${kit.name}")
        }

        configFile.save()
    }

    private fun setKitData(fileConfig: FileConfiguration, path: String, kit: Kit) {
        with(fileConfig) {
            set("$path.build", kit.build)
            set("$path.combo", kit.combo)
            set("$path.hcf", kit.hcf)
            set("$path.ranked", kit.ranked)
            set("$path.sumo", kit.sumo)
            set("$path.boxing", kit.boxing)
            set("$path.ffa", kit.ffa)
            set("$path.enabled", kit.enabled)
            set("$path.mlgRush", kit.mlgRush)
            set("$path.bedFights", kit.bedFights)
            set("$path.bridge", kit.bridge)
            set("$path.fireballFight", kit.fireballFight)
            set("$path.hunger", kit.hunger)
            set("$path.regeneration", kit.regeneration)
            set("$path.fallDamage", kit.fallDamage)
            set("$path.knockbackProfiler", kit.knockbackProfile)
            set("$path.damageTicks", kit.damageTicks)
        }
    }

    fun deleteKit(name: String) {
        kits.remove(name)
        println("Deleted kit: $name")
    }

    fun createKit(name: String) {
        kits[name] = Kit(name)
        println("Created kit: $name")
    }

    fun getKits(): Collection<Kit> = kits.values

    fun getKit(name: String): Kit? = kits[name]
}
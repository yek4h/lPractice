package net.lyragames.practice.utils

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

class ConfigFile(plugin: Plugin, name: String) {

    private val logger: Logger = plugin.logger
    private val file: File = File(plugin.dataFolder, "$name.yml")
    @Volatile var config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdir()
        }

        if (!file.exists()) {
            file.createNewFile()
            plugin.saveResource("$name.yml", true)
        }

        loadConfigAsync()
    }

    private fun loadConfigAsync() {
        CompletableFuture.runAsync {
            config = YamlConfiguration.loadConfiguration(file)
            logger.info("Configuration file ${file.name}.yml loaded asynchronously.")
        }
    }

    fun getConfigurationSection(path: String): ConfigurationSection? {
        return config.getConfigurationSection(path)
    }

    fun createSection(path: String): ConfigurationSection {
        return config.createSection(path)
    }

    fun getString(path: String): String {
        return config.getString(path) ?: ""
    }

    fun getBoolean(path: String): Boolean {
        return config.getBoolean(path, false)
    }

    fun getStringList(path: String): List<String> {
        return config.getStringList(path)
    }

    fun getInt(path: String): Int {
        return config.getInt(path, 0)
    }

    fun save() {
        CompletableFuture.runAsync {
            config.save(file)
            logger.info("Configuration file ${file.name}.yml saved asynchronously.")
        }
    }

    fun reload() {
        CompletableFuture.runAsync {
            config = YamlConfiguration.loadConfiguration(file)
            logger.info("Configuration file ${file.name}.yml reloaded asynchronously.")
        }
    }
}

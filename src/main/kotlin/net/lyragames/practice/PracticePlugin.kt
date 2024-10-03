package net.lyragames.practice

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import com.jonahseguin.drink.CommandService
import com.jonahseguin.drink.Drink
import com.mongodb.client.MongoDatabase
import io.github.thatkawaiisam.assemble.Assemble
import io.github.thatkawaiisam.assemble.AssembleStyle
import net.lyragames.practice.adapter.ScoreboardAdapter
import net.lyragames.practice.api.PracticeAPI
import net.lyragames.practice.arena.Arena
import net.lyragames.practice.arena.type.ArenaType
import net.lyragames.practice.command.*
import net.lyragames.practice.command.admin.*
import net.lyragames.practice.constants.Constants
import net.lyragames.practice.database.Mongo
import net.lyragames.practice.database.MongoManager
import net.lyragames.practice.duel.DuelRequest
import net.lyragames.practice.duel.gson.DuelRequestGsonAdapter
import net.lyragames.practice.event.listener.EventListener
import net.lyragames.practice.event.map.EventMap
import net.lyragames.practice.event.map.type.EventMapType
import net.lyragames.practice.holograms.HologramManager
import net.lyragames.practice.kit.EditedKit
import net.lyragames.practice.kit.Kit
import net.lyragames.practice.kit.editor.listener.KitEditorListener
import net.lyragames.practice.kit.serializer.EditKitSerializer
import net.lyragames.practice.leaderboards.Leaderboards
import net.lyragames.practice.listener.MoveListener
import net.lyragames.practice.listener.PreventionListener
import net.lyragames.practice.listener.WorldListener
import net.lyragames.practice.manager.*
import net.lyragames.practice.match.Match
import net.lyragames.practice.match.ffa.listener.FFAListener
import net.lyragames.practice.match.listener.MatchListener
import net.lyragames.practice.profile.ProfileListener
import net.lyragames.practice.profile.ProfileManager
import net.lyragames.practice.queue.QueueType
import net.lyragames.practice.queue.task.QueueTask
import net.lyragames.practice.task.*
import net.lyragames.practice.utils.AnimatedTextManager
import net.lyragames.practice.utils.ConfigFile
import net.lyragames.practice.utils.InventoryUtil
import net.lyragames.practice.utils.item.ItemListener
import net.lyragames.practice.utils.providers.*
import org.bukkit.Material
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class PracticePlugin : JavaPlugin() {

    lateinit var commandService: CommandService
    lateinit var settingsFile: ConfigFile
    lateinit var kitsFile: ConfigFile
    lateinit var arenasFile: ConfigFile
    lateinit var scoreboardFile: ConfigFile
    lateinit var ffaFile: ConfigFile
    lateinit var eventsFile: ConfigFile
    lateinit var languageFile: ConfigFile

    val mongoManager = MongoManager()

    lateinit var kitManager: KitManager
    lateinit var leaderboards: Leaderboards
    var profileManager: ProfileManager = ProfileManager

    lateinit var API: PracticeAPI
    lateinit var dailyWinstreakManager: DailyWinStreakManager
    lateinit var animatedTextManagerFooter: AnimatedTextManager
    lateinit var animatedTextManagerTitle: AnimatedTextManager
    lateinit var hologramManager: HologramManager

    override fun onEnable() {
        commandService = Drink.get(this)
        instance = this

        // Load configuration files
        loadConfigFiles()
        logger.info("Successfully loaded files!")

        // Load MongoDB
        mongoManager.initialize().thenRun {
            logger.info("MongoDB initialization completed")
            try {
                onMongoInitialized()
            } catch (e: Exception) {
                logger.severe("Failed to run onMongoInitialized: ${e.message}")
                e.printStackTrace()
            }
        }.exceptionally { ex ->
            logger.severe("Failed to initialize MongoDB: ${ex.message}")
            ex.printStackTrace()
            null
        }
    }

    override fun onDisable() {
        profileManager.shutdown()

        for (match in Match.matches.elements()) {
            match!!.reset()
        }

        kitManager.save()

        hologramManager.hologramDestroyAll()
    }

    private fun onMongoInitialized() {
        cleanupWorld()
        logger.info("World cleanup completed")

        // Remove crafting for certain materials
        InventoryUtil.removeCrafting(Material.WORKBENCH)
        logger.info("Removed crafting for certain materials")

        // Load Managers
        loadManagers()
        logger.info("Managers loaded")

        leaderboards = Leaderboards(settingsFile, this)
        // Initialize API and Menu
        API = PracticeAPI()
        logger.info("API initialized")

        // Setup Commands
        setupCommands()
        logger.info("Commands set up")

        // Load Constants
        Constants.load()
        logger.info("Constants loaded")

        // Initialize tasks
        initTasks()
        logger.info("Tasks initialized")

        // Setup Scoreboard
        if (scoreboardFile.getBoolean("scoreboard.enabled")) {
            val assemble = Assemble(this, ScoreboardAdapter(scoreboardFile))
            assemble.ticks = 2
            assemble.assembleStyle = AssembleStyle.MODERN
        }
        logger.info("Scoreboard setup completed")

        // Register Listeners
        registerListeners()
        logger.info("Listeners registered")
    }

    private fun loadConfigFiles() {
        settingsFile = ConfigFile(this, "settings")
        kitsFile = ConfigFile(this, "kits")
        arenasFile = ConfigFile(this, "arenas")
        scoreboardFile = ConfigFile(this, "scoreboard")
        ffaFile = ConfigFile(this, "ffa")
        eventsFile = ConfigFile(this, "events")
        languageFile = ConfigFile(this, "language")
    }

    private fun cleanupWorld() {
        server.worlds.forEach { world ->
            world.time = 4000
            world.entities.forEach { entity ->
                if (entity !is Player && (entity is LivingEntity || entity is Item || entity is ExperienceOrb)) {
                    entity.remove()
                }
            }
        }
        logger.info("Cleaned all worlds")
    }

    private fun loadManagers() {
        try {
            logger.info("Loading ArenaManager...")
            ArenaManager.load()
            logger.info("Successfully loaded ${if (Arena.arenas.size == 1) "1 arena!" else "${Arena.arenas.size} arenas!"}")

            logger.info("Loading KitManager...")
            kitManager = KitManager()
            logger.info("Successfully loaded ${if (kitManager.kits.size == 1) "1 kit!" else "${kitManager.kits.size} kits!"}")

            logger.info("Loading QueueManager...")
            QueueManager.load()
            logger.info("Loading EventMapManager...")
            EventMapManager.load()
            logger.info("Successfully loaded ${if (EventMapManager.maps.size == 1) "1 event map!" else "${EventMapManager.maps.size} event maps!"}")

            logger.info("Loading FFAManager...")
            FFAManager.load()
            logger.info("Loading ArenaRatingManager...")
            ArenaRatingManager.load()

            logger.info("Initializing DailyWinStreakManager...")
            dailyWinstreakManager = DailyWinStreakManager(this)

            logger.info("Initializing ProfileManager...")
            profileManager.initialLoaded()
            loadAnimatedTextManager()

            logger.info("Initializing HologramManager...")
            hologramManager = HologramManager()
            logger.info("Managers loaded successfully")
        } catch (e: Exception) {
            logger.severe("Failed to load managers: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadAnimatedTextManager() {
        val footerFragments = settingsFile.getStringList("ANIMATIONS.FOOTER.TEXT")
        val footerWriteEffect = settingsFile.getBoolean("ANIMATIONS.FOOTER.WRITE-EFFECT")
        val footerColorTrailEffect = settingsFile.getBoolean("ANIMATIONS.FOOTER.COLOR-TRAIL-EFFECT")
        val footerFlashOnFinish = settingsFile.getBoolean("ANIMATIONS.FOOTER.FLASH-COLOURS-ON-FINISH-ANIMATION")
        val footerMainColor = settingsFile.getString("ANIMATIONS.FOOTER.COLOR-TRAIL-MAIN") ?: "&b"
        val footerSecondaryColor = settingsFile.getString("ANIMATIONS.FOOTER.COLOR-TRAIL-SECONDARY") ?: "&f"
        val footerUpdateInterval = (settingsFile.config.getDouble("ANIMATIONS.FOOTER.TIME") * 500).toLong()
        val footerTypeTextBar = settingsFile.getBoolean("ANIMATIONS.FOOTER.TYPE-TEXT-BAR")
        val footerTypeBar = settingsFile.getString("ANIMATIONS.FOOTER.TYPE-BAR") ?: "|"
        val footerBarDuration = (settingsFile.config.getDouble("ANIMATIONS.FOOTER.BAR-DURATION") * 500).toLong()

        animatedTextManagerFooter = AnimatedTextManager(
            mainColor = footerMainColor,
            trailColor = footerSecondaryColor,
            writeEffect = footerWriteEffect,
            colorTrailEffect = footerColorTrailEffect,
            flashOnFinish = footerFlashOnFinish,
            typeTextBar = footerTypeTextBar,
            typeBar = footerTypeBar,
            barDuration = footerBarDuration,
            updateInterval = footerUpdateInterval
        ).apply {
            setTextFragments(footerFragments)
        }

        val titleFragments = settingsFile.getStringList("ANIMATIONS.TITLE.TEXT")
        val titleWriteEffect = settingsFile.getBoolean("ANIMATIONS.TITLE.WRITE-EFFECT")
        val titleColorTrailEffect = settingsFile.getBoolean("ANIMATIONS.TITLE.COLOR-TRAIL-EFFECT")
        val titleFlashOnFinish = settingsFile.getBoolean("ANIMATIONS.TITLE.FLASH-COLOURS-ON-FINISH-ANIMATION")
        val titleMainColor = settingsFile.getString("ANIMATIONS.TITLE.COLOR-TRAIL-MAIN") ?: "&b"
        val titleSecondaryColor = settingsFile.getString("ANIMATIONS.TITLE.COLOR-TRAIL-SECONDARY") ?: "&f"
        val titleUpdateInterval = (settingsFile.config.getDouble("ANIMATIONS.TITLE.TIME") * 500).toLong()
        val titleTypeTextBar = settingsFile.getBoolean("ANIMATIONS.TITLE.TYPE-TEXT-BAR")
        val titleTypeBar = settingsFile.getString("ANIMATIONS.TITLE.TYPE-BAR") ?: "|"
        val titleBarDuration = (settingsFile.config.getDouble("ANIMATIONS.TITLE.FLASH-DURATION") * 500).toLong()

        animatedTextManagerTitle = AnimatedTextManager(
            mainColor = titleMainColor,
            trailColor = titleSecondaryColor,
            writeEffect = titleWriteEffect,
            colorTrailEffect = titleColorTrailEffect,
            flashOnFinish = titleFlashOnFinish,
            typeTextBar = titleTypeTextBar,
            typeBar = titleTypeBar,
            barDuration = titleBarDuration,
            updateInterval = titleUpdateInterval
        ).apply {
            setTextFragments(titleFragments)
        }
    }

    private fun setupCommands() {
        commandService.bind(Arena::class.java).toProvider(ArenaProvider())
        commandService.bind(ArenaType::class.java).toProvider(ArenaTypeProvider())
        commandService.bind(EventMap::class.java).toProvider(EventMapProvider())
        commandService.bind(EventMapType::class.java).toProvider(EventMapTypeProvider())
        commandService.bind(QueueType::class.java).toProvider(QueueTypeProvider())
        commandService.bind(Kit::class.java).toProvider(KitProvider(kitManager))

        val commands = mapOf(
            "leaderboard" to LeaderboardCommand(),
            "winstreak" to WinStreakCommand(dailyWinstreakManager),
            "duel" to DuelCommand(),
            "event" to EventCommand(),
            "leave" to LeaveCommand(),
            "matchsnapshot" to MatchSnapshotCommand(),
            "forcequeue" to ForceQueueCommand(),
            "settings" to SettingsCommand(),
            "spawn" to SpawnCommand(),
            "spec" to SpectateCommand(),
            "arena" to ArenaCommand(),
            "arenaratings" to ArenaRatingCommand(),
            "build" to BuildCommand(),
            "eventmap" to EventMapCommand(),
            "ffa" to FFACommand(),
            "follow" to FollowCommand(),
            "kit" to KitCommand(),
            "setspawn" to SetSpawnCommand(),
            "ratemap" to RateMapCommand(),
            "party" to PartyCommand()
        )

        commands.forEach { (name, command) ->
            commandService.register(command, name)
        }

        commandService.registerCommands()
    }

    private fun initTasks() {
        QueueTask
        EventAnnounceTask
        TNTEventBlockRemovalTask
        TNTTagTask
        MatchSnapshotExpireTask
        EnderPearlCooldownTask
        ArrowCooldownTask
        FFAItemClearTask
    }

    private fun registerListeners() {
        server.pluginManager.apply {
            registerEvents(WorldListener, this@PracticePlugin)
            registerEvents(ProfileListener, this@PracticePlugin)
            registerEvents(MatchListener, this@PracticePlugin)
            registerEvents(FFAListener, this@PracticePlugin)
            registerEvents(EventListener, this@PracticePlugin)
            registerEvents(KitEditorListener, this@PracticePlugin)
            registerEvents(PreventionListener, this@PracticePlugin)
            registerEvents(MoveListener, this@PracticePlugin)
            registerEvents(ItemListener(), this@PracticePlugin)
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: PracticePlugin

        @JvmStatic
        val GSON: Gson = GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeHierarchyAdapter(EditedKit::class.java, EditKitSerializer)
            .registerTypeHierarchyAdapter(DuelRequest::class.java, DuelRequestGsonAdapter)
            .create()
    }


}
package com.excrele.ehml;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

/**
 * Main class for Excrele's Hostile Mob Limiter (EHML) plugin for Spigot 1.21.5.
 * Orchestrates modular components for controlling hostile mob spawns, handling mob cleanup,
 * and logging activities. Features include global and per-mob-type spawn limits, low-health
 * spawn delay, mob cleanup on player death, an in-game GUI for toggling features, and a
 * configuration reload command.
 */
public class EHML extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("EHML");
    private ConfigManager configManager;
    private LoggerModule loggerModule;
    private GlobalLimitModule globalLimitModule;
    private PerMobLimitModule perMobLimitModule;
    private LowHealthDelayModule lowHealthDelayModule;
    private DeathCleanupModule deathCleanupModule;
    private GUIManager guiManager;

    /**
     * Initializes plugin components and registers listeners and commands.
     */
    @Override
    public void onEnable() {
        // Initialize configuration manager
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize logger module
        loggerModule = new LoggerModule(this, configManager);
        loggerModule.log("Server", "EHML plugin enabled.");

        // Initialize modules
        globalLimitModule = new GlobalLimitModule(this, configManager, loggerModule);
        perMobLimitModule = new PerMobLimitModule(this, configManager, loggerModule);
        lowHealthDelayModule = new LowHealthDelayModule(configManager, loggerModule);
        deathCleanupModule = new DeathCleanupModule(configManager, loggerModule);
        guiManager = new GUIManager(this, configManager, loggerModule);

        // Register event listeners
        registerListener(globalLimitModule);
        registerListener(perMobLimitModule);
        registerListener(lowHealthDelayModule);
        registerListener(deathCleanupModule);
        registerListener(new GUIListener(guiManager));

        // Register commands
        getCommand("ehml").setExecutor(new ReloadCommand(this, configManager, loggerModule));
        getCommand("ehmlgui").setExecutor(guiManager);

        LOGGER.info("EHML enabled successfully.");
    }

    /**
     * Cleans up resources when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        loggerModule.log("Server", "EHML plugin disabled.");
        loggerModule.close();
        LOGGER.info("EHML disabled.");
    }

    /**
     * Registers a listener with the server.
     * @param listener The listener to register.
     */
    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    /**
     * Reloads the plugin configuration and updates all modules.
     */
    public void reload() {
        configManager.loadConfig();
        globalLimitModule.reload();
        perMobLimitModule.reload();
        lowHealthDelayModule.reload();
        deathCleanupModule.reload();
        loggerModule.log("Server", "EHML configuration reloaded.");
        LOGGER.info("EHML configuration reloaded.");
    }
}
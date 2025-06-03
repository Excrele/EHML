package com.excrele.ehml;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

/**
 * Main class for Excrele's Hostile Mob Limiter (EHML) plugin for Spigot 1.21.5.
 * Orchestrates modular components for controlling hostile mob spawns and handling mob cleanup.
 * Features include global and per-mob-type spawn limits, low-health spawn delay, mob cleanup on player death,
 * a configuration reload command, and an in-game GUI for toggling features.
 */
public class EHML extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("EHML");
    private ConfigManager configManager;
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

        // Initialize modules
        globalLimitModule = new GlobalLimitModule(this, configManager);
        perMobLimitModule = new PerMobLimitModule(this, configManager);
        lowHealthDelayModule = new LowHealthDelayModule(configManager);
        deathCleanupModule = new DeathCleanupModule(configManager);
        guiManager = new GUIManager(this, configManager);

        // Register event listeners
        registerListener(globalLimitModule);
        registerListener(perMobLimitModule);
        registerListener(lowHealthDelayModule);
        registerListener(deathCleanupModule);
        registerListener(new GUIListener(guiManager));

        // Register commands
        getCommand("ehml").setExecutor(new ReloadCommand(this, configManager));
        getCommand("ehmlgui").setExecutor(guiManager);

        LOGGER.info("EHML enabled successfully.");
    }

    /**
     * Cleans up resources when the plugin is disabled.
     */
    @Override
    public void onDisable() {
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
        LOGGER.info("EHML configuration reloaded.");
    }
}
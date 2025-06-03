package com.excrele.ehml;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import java.util.logging.Logger;

/**
 * Module for enforcing a global limit on hostile mobs in loaded chunks.
 */
public class GlobalLimitModule implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final LoggerModule loggerModule;
    private final Logger logger;

    /**
     * Initializes the global limit module.
     * @param plugin The main plugin instance for server access.
     * @param configManager The configuration manager providing settings.
     * @param loggerModule The logger module for recording activities.
     */
    public GlobalLimitModule(JavaPlugin plugin, ConfigManager configManager, LoggerModule loggerModule) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.loggerModule = loggerModule;
        this.logger = Logger.getLogger("EHML");
    }

    /**
     * Handles mob spawn events to enforce the global hostile mob limit.
     * @param event The creature spawn event.
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!configManager.isGlobalLimitEnabled()) {
            return; // Skip if global limit is disabled
        }

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return; // Only handle natural spawns or spawners
        }

        if (!configManager.isHostileMob(event.getEntityType())) {
            return; // Ignore non-hostile mobs
        }

        // Count current hostile mobs in loaded chunks
        int currentHostileCount = plugin.getServer().getWorlds().stream()
                .flatMap(world -> world.getEntities().stream())
                .filter(entity -> configManager.isHostileMob(entity.getType()))
                .mapToInt(entity -> 1)
                .sum();

        // Check global hostile mob limit
        if (currentHostileCount >= configManager.getGlobalHostileLimit()) {
            event.setCancelled(true);
            logger.fine("Cancelled spawn of " + event.getEntityType() + ": Global limit (" +
                    configManager.getGlobalHostileLimit() + ") reached.");
            loggerModule.log("GlobalLimit", "Cancelled spawn of " + event.getEntityType() +
                    " at " + event.getLocation() + ": Global limit (" +
                    configManager.getGlobalHostileLimit() + ") reached.");
        }
    }

    /**
     * Reloads the module (no-op as settings are managed by ConfigManager).
     */
    public void reload() {
        // No additional reload logic needed; ConfigManager handles settings
    }
}
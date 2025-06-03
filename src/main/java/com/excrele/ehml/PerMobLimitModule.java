package com.excrele.ehml;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Module for enforcing per-mob-type spawn limits.
 */
public class PerMobLimitModule implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Logger logger;
    private final Map<EntityType, Integer> currentMobCounts;

    /**
     * Initializes the per-mob limit module.
     * @param plugin The main plugin instance for server access.
     * @param configManager The configuration manager providing settings.
     */
    public PerMobLimitModule(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.logger = Logger.getLogger("EHML");
        this.currentMobCounts = new HashMap<>();
        updateMobCounts();
    }

    /**
     * Handles mob spawn events to enforce per-mob-type limits.
     * @param event The creature spawn event.
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!configManager.isMobLimitsEnabled()) {
            return; // Skip if per-mob limits are disabled
        }

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return; // Only handle natural spawns or spawners
        }

        EntityType entityType = event.getEntityType();
        if (!configManager.isHostileMob(entityType)) {
            return; // Ignore non-hostile mobs
        }

        // Check per-mob-type limit
        if (configManager.getMobLimits().containsKey(entityType)) {
            int currentCount = currentMobCounts.getOrDefault(entityType, 0);
            int limit = configManager.getMobLimits().get(entityType);
            if (currentCount >= limit) {
                event.setCancelled(true);
                logger.fine("Cancelled spawn of " + entityType + ": Mob-specific limit (" + limit + ") reached.");
                return;
            }
            // Increment count for this mob type
            currentMobCounts.put(entityType, currentCount + 1);
        }
    }

    /**
     * Updates mob counts for all hostile mobs in loaded chunks.
     */
    public void updateMobCounts() {
        currentMobCounts.clear();
        configManager.getMobLimits().keySet().forEach(type -> {
            int count = plugin.getServer().getWorlds().stream()
                    .flatMap(world -> world.getEntities().stream())
                    .filter(entity -> entity.getType() == type)
                    .mapToInt(entity -> 1)
                    .sum();
            currentMobCounts.put(type, count);
        });
    }

    /**
     * Reloads the module by updating mob counts.
     */
    public void reload() {
        updateMobCounts();
    }
}
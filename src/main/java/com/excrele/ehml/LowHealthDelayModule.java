package com.excrele.ehml;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Module for slowing down hostile mob spawns near low-health players.
 */
public class LowHealthDelayModule implements Listener {

    private final ConfigManager configManager;
    private final LoggerModule loggerModule;
    private final Logger logger;
    private final Map<UUID, Long> lastSpawnTimes;

    /**
     * Initializes the low-health delay module.
     * @param configManager The configuration manager providing settings.
     * @param loggerModule The logger module for recording activities.
     */
    public LowHealthDelayModule(ConfigManager configManager, LoggerModule loggerModule) {
        this.configManager = configManager;
        this.loggerModule = loggerModule;
        this.logger = Logger.getLogger("EHML");
        this.lastSpawnTimes = new HashMap<>();
    }

    /**
     * Handles mob spawn events to apply spawn delay near low-health players.
     * @param event The creature spawn event.
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!configManager.isLowHealthDelayEnabled()) {
            return; // Skip if low-health delay is disabled
        }

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return; // Only handle natural spawns or spawners
        }

        if (!configManager.isHostileMob(event.getEntityType())) {
            return; // Ignore non-hostile mobs
        }

        Location spawnLocation = event.getLocation();
        String chunkKey = spawnLocation.getChunk().toString();
        long currentTime = System.currentTimeMillis();

        // Check for low-health players and apply spawn delay
        if (hasLowHealthPlayerNearby(spawnLocation)) {
            Long lastSpawnTime = lastSpawnTimes.getOrDefault(UUID.nameUUIDFromBytes(chunkKey.getBytes()), 0L);
            // Randomly cancel spawn based on configured chance
            if (Math.random() < configManager.getSpawnDelayChance()) {
                event.setCancelled(true);
                logger.fine("Cancelled spawn of " + event.getEntityType() + " near low-health player at " + spawnLocation);
                loggerModule.log("LowHealthDelay", "Cancelled spawn of " + event.getEntityType() +
                        " near low-health player at " + spawnLocation);
                lastSpawnTimes.put(UUID.nameUUIDFromBytes(chunkKey.getBytes()), currentTime);
            }
        }
    }

    /**
     * Checks if any player within the configured radius has low health.
     * @param location The spawn location.
     * @return True if a player with health below threshold is nearby.
     */
    private boolean hasLowHealthPlayerNearby(Location location) {
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getHealth() < configManager.getLowHealthThreshold() &&
                    player.getLocation().distanceSquared(location) <= configManager.getSpawnDelayRadius() * configManager.getSpawnDelayRadius()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reloads the module by clearing spawn time cache.
     */
    public void reload() {
        lastSpawnTimes.clear();
    }
}
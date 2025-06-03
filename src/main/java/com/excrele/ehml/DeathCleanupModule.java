package com.excrele.ehml;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Location;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Module for cleaning up hostile mobs near a player's death location.
 */
public class DeathCleanupModule implements Listener {

    private final ConfigManager configManager;
    private final LoggerModule loggerModule;
    private final Logger logger;

    /**
     * Initializes the death cleanup module.
     * @param configManager The configuration manager providing settings.
     * @param loggerModule The logger module for recording activities.
     */
    public DeathCleanupModule(ConfigManager configManager, LoggerModule loggerModule) {
        this.configManager = configManager;
        this.loggerModule = loggerModule;
        this.logger = Logger.getLogger("EHML");
    }

    /**
     * Handles player death to clean up hostile mobs if more than the threshold are present.
     * @param event The player death event.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configManager.isDeathCleanupEnabled()) {
            return; // Skip if death cleanup is disabled
        }

        Location deathLocation = event.getEntity().getLocation();
        List<Entity> nearbyHostileMobs = deathLocation.getWorld().getNearbyEntities(deathLocation,
                        configManager.getDeathMobCleanupRadius(), configManager.getDeathMobCleanupRadius(),
                        configManager.getDeathMobCleanupRadius()).stream()
                .filter(entity -> configManager.isHostileMob(entity.getType()))
                .collect(Collectors.toList());

        // Check if number of hostile mobs exceeds threshold
        if (nearbyHostileMobs.size() > configManager.getDeathMobThreshold()) {
            int mobsToKill = (int) Math.ceil(nearbyHostileMobs.size() * configManager.getDeathMobKillPercentage());
            // Shuffle list to randomly select mobs to kill
            java.util.Collections.shuffle(nearbyHostileMobs);
            for (int i = 0; i < Math.min(mobsToKill, nearbyHostileMobs.size()); i++) {
                Entity mob = nearbyHostileMobs.get(i);
                EntityType type = mob.getType();
                mob.remove();
                logger.fine("Removed " + type + " at " + mob.getLocation() + " due to player death at " + deathLocation);
                loggerModule.log("DeathCleanup", "Removed " + type + " at " + mob.getLocation() +
                        " due to player death at " + deathLocation);
            }
            logger.info("Removed " + mobsToKill + " hostile mobs near player death at " + deathLocation);
            loggerModule.log("DeathCleanup", "Removed " + mobsToKill + " hostile mobs near player death at " + deathLocation);
        }
    }

    /**
     * Reloads the module (no-op as settings are managed by ConfigManager).
     */
    public void reload() {
        // No additional reload logic needed; ConfigManager handles settings
    }
}
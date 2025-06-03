package com.excrele.ehml;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages configuration loading, validation, and saving for the EHML plugin.
 * Provides access to feature toggles and settings for all modules.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private boolean globalLimitEnabled;
    private int globalHostileLimit;
    private boolean mobLimitsEnabled;
    private Map<EntityType, Integer> mobLimits;
    private boolean lowHealthDelayEnabled;
    private double lowHealthThreshold;
    private double spawnDelayRadius;
    private double spawnDelayChance;
    private boolean deathCleanupEnabled;
    private double deathMobCleanupRadius;
    private int deathMobThreshold;
    private double deathMobKillPercentage;
    private boolean loggingEnabled;

    /**
     * Initializes the configuration manager.
     * @param plugin The main plugin instance.
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.mobLimits = new HashMap<>();
    }

    /**
     * Loads and validates configuration from config.yml.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Load global hostile mob limit settings
        globalLimitEnabled = config.getBoolean("global-limit-enabled", true);
        globalHostileLimit = config.getInt("global-hostile-limit", 70);
        if (globalHostileLimit < 0) {
            globalHostileLimit = 70;
            logger.warning("Invalid global-hostile-limit in config, using default: 70");
        }

        // Load per-mob-type limits settings
        mobLimitsEnabled = config.getBoolean("mob-limits-enabled", true);
        mobLimits.clear();
        if (mobLimitsEnabled && config.contains("mob-limits")) {
            for (String key : config.getConfigurationSection("mob-limits").getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    if (isHostileMob(entityType)) {
                        int limit = config.getInt("mob-limits." + key);
                        if (limit >= 0) {
                            mobLimits.put(entityType, limit);
                        } else {
                            logger.warning("Invalid limit for " + key + ", ignoring.");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid entity type in config: " + key);
                }
            }
        }

        // Load low-health spawn delay settings
        lowHealthDelayEnabled = config.getBoolean("low-health-delay-enabled", true);
        lowHealthThreshold = config.getDouble("low-health-threshold", 5.0);
        if (lowHealthThreshold < 0 || lowHealthThreshold > 20) {
            lowHealthThreshold = 5.0;
            logger.warning("Invalid low-health-threshold in config, using default: 5.0");
        }

        spawnDelayRadius = config.getDouble("spawn-delay-radius", 30.0);
        if (spawnDelayRadius < 0) {
            spawnDelayRadius = 30.0;
            logger.warning("Invalid spawn-delay-radius in config, using default: 30.0");
        }

        spawnDelayChance = config.getDouble("spawn-delay-chance", 0.5);
        if (spawnDelayChance < 0 || spawnDelayChance > 1) {
            spawnDelayChance = 0.5;
            logger.warning("Invalid spawn-delay-chance in config, using default: 0.5");
        }

        // Load player death mob cleanup settings
        deathCleanupEnabled = config.getBoolean("death-cleanup-enabled", true);
        deathMobCleanupRadius = config.getDouble("death-mob-cleanup-radius", 10.0);
        if (deathMobCleanupRadius < 0) {
            deathMobCleanupRadius = 10.0;
            logger.warning("Invalid death-mob-cleanup-radius in config, using default: 10.0");
        }

        deathMobThreshold = config.getInt("death-mob-threshold", 5);
        if (deathMobThreshold < 0) {
            deathMobThreshold = 5;
            logger.warning("Invalid death-mob-threshold in config, using default: 5");
        }

        deathMobKillPercentage = config.getDouble("death-mob-kill-percentage", 0.5);
        if (deathMobKillPercentage < 0 || deathMobKillPercentage > 1) {
            deathMobKillPercentage = 0.5;
            logger.warning("Invalid death-mob-kill-percentage in config, using default: 0.5");
        }

        // Load logging settings
        loggingEnabled = config.getBoolean("logging-enabled", true);

        logger.info("Loaded feature toggles: globalLimit=" + globalLimitEnabled +
                ", mobLimits=" + mobLimitsEnabled +
                ", lowHealthDelay=" + lowHealthDelayEnabled +
                ", deathCleanup=" + deathCleanupEnabled +
                ", logging=" + loggingEnabled);
        logger.info("Loaded global hostile limit: " + globalHostileLimit);
        logger.info("Loaded mob-specific limits: " + mobLimits);
        logger.info("Loaded low-health settings: threshold=" + lowHealthThreshold +
                ", radius=" + spawnDelayRadius + ", chance=" + spawnDelayChance);
        logger.info("Loaded death cleanup settings: radius=" + deathMobCleanupRadius +
                ", threshold=" + deathMobThreshold + ", killPercentage=" + deathMobKillPercentage);
    }

    /**
     * Saves the current configuration to config.yml.
     */
    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("global-limit-enabled", globalLimitEnabled);
        config.set("mob-limits-enabled", mobLimitsEnabled);
        config.set("low-health-delay-enabled", lowHealthDelayEnabled);
        config.set("death-cleanup-enabled", deathCleanupEnabled);
        config.set("logging-enabled", loggingEnabled);
        plugin.saveConfig();
        logger.info("Configuration saved to config.yml");
    }

    /**
     * Toggles a feature's enabled state and saves the change.
     * @param feature The feature to toggle (e.g., "global-limit", "mob-limits").
     * @param enabled The new enabled state.
     */
    public void toggleFeature(String feature, boolean enabled) {
        switch (feature) {
            case "global-limit":
                globalLimitEnabled = enabled;
                break;
            case "mob-limits":
                mobLimitsEnabled = enabled;
                break;
            case "low-health-delay":
                lowHealthDelayEnabled = enabled;
                break;
            case "death-cleanup":
                deathCleanupEnabled = enabled;
                break;
            case "logging":
                loggingEnabled = enabled;
                break;
            default:
                logger.warning("Attempted to toggle invalid feature: " + feature);
        }
        saveConfig();
    }

    /**
     * Checks if an entity is a hostile mob.
     * @param entityType The entity type to check.
     * @return True if the entity is hostile.
     */
    public boolean isHostileMob(EntityType entityType) {
        return switch (entityType) {
            case ZOMBIE, SKELETON, CREEPER, SPIDER, ENDERMAN, WITCH, WITHER_SKELETON,
                 STRAY, HUSK, ZOMBIE_VILLAGER, SKELETON_HORSE, ZOMBIE_HORSE,
                 PHANTOM, SLIME, MAGMA_CUBE, GHAST, BLAZE, DROWNED, PILLAGER,
                 VINDICATOR, EVOKER, VEX, RAVAGER, SHULKER, ENDERMITE, GUARDIAN,
                 ELDER_GUARDIAN, HOGLIN, ZOGLIN, PIGLIN, PIGLIN_BRUTE, BOGGED -> true;
            default -> false;
        };
    }

    // Getters for configuration values
    public boolean isGlobalLimitEnabled() { return globalLimitEnabled; }
    public int getGlobalHostileLimit() { return globalHostileLimit; }
    public boolean isMobLimitsEnabled() { return mobLimitsEnabled; }
    public Map<EntityType, Integer> getMobLimits() { return new HashMap<>(mobLimits); }
    public boolean isLowHealthDelayEnabled() { return lowHealthDelayEnabled; }
    public double getLowHealthThreshold() { return lowHealthThreshold; }
    public double getSpawnDelayRadius() { return spawnDelayRadius; }
    public double getSpawnDelayChance() { return spawnDelayChance; }
    public boolean isDeathCleanupEnabled() { return deathCleanupEnabled; }
    public double getDeathMobCleanupRadius() { return deathMobCleanupRadius; }
    public int getDeathMobThreshold() { return deathMobThreshold; }
    public double getDeathMobKillPercentage() { return deathMobKillPercentage; }
    public boolean isLoggingEnabled() { return loggingEnabled; }
}
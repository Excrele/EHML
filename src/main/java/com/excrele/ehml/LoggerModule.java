package com.excrele.ehml;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Module for logging EHML plugin activities to a YAML file.
 * Creates a new log file on server start with a timestamped name.
 */
public class LoggerModule {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Logger logger;
    private final File logFile;
    private final YamlConfiguration logConfig;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Initializes the logger module and creates a new log file.
     * @param plugin The main plugin instance.
     * @param configManager The configuration manager.
     */
    public LoggerModule(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.logger = plugin.getLogger();

        // Create logs directory
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        // Create new log file with timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        logFile = new File(logsDir, timestamp + "_EHML-Log.yml");
        logConfig = new YamlConfiguration();

        // Initialize log file
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            logConfig.save(logFile);
        } catch (IOException e) {
            logger.severe("Failed to create log file: " + e.getMessage());
        }
    }

    /**
     * Logs an activity to the YAML file if logging is enabled.
     * @param category The category of the activity (e.g., "GlobalLimit", "GUI").
     * @param message The message to log.
     */
    public void log(String category, String message) {
        if (!configManager.isLoggingEnabled()) {
            return;
        }

        String timestamp = dateFormat.format(new Date());
        String logPath = "logs." + category + "." + System.currentTimeMillis();
        logConfig.set(logPath + ".timestamp", timestamp);
        logConfig.set(logPath + ".message", message);

        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            logger.severe("Failed to save log entry: " + e.getMessage());
        }
    }

    /**
     * Closes the logger module (no-op as file is saved after each log).
     */
    public void close() {
        // No additional cleanup needed; logs are saved incrementally
    }
}
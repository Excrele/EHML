package com.excrele.ehml;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Logger;

/**
 * Command executor for the /ehml reload command.
 */
public class ReloadCommand implements CommandExecutor {

    private final EHML plugin;
    private final ConfigManager configManager;
    private final LoggerModule loggerModule;
    private final Logger logger;

    /**
     * Initializes the reload command.
     * @param plugin The main plugin instance.
     * @param configManager The configuration manager.
     * @param loggerModule The logger module for recording activities.
     */
    public ReloadCommand(EHML plugin, ConfigManager configManager, LoggerModule loggerModule) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.loggerModule = loggerModule;
        this.logger = Logger.getLogger("EHML");
    }

    /**
     * Handles the /ehml reload command to reload the plugin configuration.
     * @param sender The command sender.
     * @param command The command.
     * @param label The command label.
     * @param args The command arguments.
     * @return True if the command was handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ehml.reload")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            plugin.reload();
            sender.sendMessage("§aEHML configuration reloaded successfully.");
            logger.info("Configuration reloaded by " + sender.getName());
            loggerModule.log("ReloadCommand", "Configuration reloaded by " + sender.getName());
            return true;
        }
        sender.sendMessage("§cUsage: /ehml reload");
        return false;
    }
}
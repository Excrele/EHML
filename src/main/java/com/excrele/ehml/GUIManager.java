package com.excrele.ehml;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

/**
 * Manages the in-game GUI for toggling EHML features.
 */
public class GUIManager implements CommandExecutor {

    private final EHML plugin;
    private final ConfigManager configManager;
    private final LoggerModule loggerModule;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "EHML Settings";
    private final NamespacedKey featureKey;

    /**
     * Initializes the GUI manager.
     * @param plugin The main plugin instance.
     * @param configManager The configuration manager.
     * @param loggerModule The logger module for recording activities.
     */
    public GUIManager(EHML plugin, ConfigManager configManager, LoggerModule loggerModule) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.loggerModule = loggerModule;
        this.featureKey = new NamespacedKey(plugin, "ehml_feature");
    }

    /**
     * Handles the /ehmlgui command to open the GUI.
     * @param sender The command sender.
     * @param command The command.
     * @param label The command label.
     * @param args The command arguments.
     * @return True if the command was handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("ehml.gui")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        openGUI(player);
        loggerModule.log("GUI", "Player " + player.getName() + " opened EHML GUI.");
        return true;
    }

    /**
     * Opens the EHML settings GUI for a player.
     * @param player The player to open the GUI for.
     */
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);

        // Global Limit Toggle
        gui.setItem(0, createToggleItem(Material.DIAMOND_SWORD, "Global Limit",
                configManager.isGlobalLimitEnabled(), "global-limit",
                "Toggles the global hostile mob limit."));

        // Per-Mob Limits Toggle
        gui.setItem(2, createToggleItem(Material.BOW, "Per-Mob Limits",
                configManager.isMobLimitsEnabled(), "mob-limits",
                "Toggles per-mob-type spawn limits."));

        // Low-Health Delay Toggle
        gui.setItem(4, createToggleItem(Material.POTION, "Low-Health Delay",
                configManager.isLowHealthDelayEnabled(), "low-health-delay",
                "Toggles spawn delay near low-health players."));

        // Death Cleanup Toggle
        gui.setItem(6, createToggleItem(Material.PLAYER_HEAD, "Death Cleanup",
                configManager.isDeathCleanupEnabled(), "death-cleanup",
                "Toggles mob cleanup on player death."));

        // Logging Toggle
        gui.setItem(8, createToggleItem(Material.WRITABLE_BOOK, "Logging",
                configManager.isLoggingEnabled(), "logging",
                "Toggles activity logging to YAML file."));

        player.openInventory(gui);
    }

    /**
     * Creates a toggle item for the GUI.
     * @param material The material of the item.
     * @param name The display name of the item.
     * @param enabled Whether the feature is enabled.
     * @param feature The feature identifier.
     * @param description The lore description of the feature.
     * @return The created ItemStack.
     */
    private ItemStack createToggleItem(Material material, String name, boolean enabled, String feature, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name + ": " + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + description,
                    ChatColor.GRAY + "Feature: " + feature,
                    ChatColor.BLUE + "Click to toggle"
            ));
            meta.getPersistentDataContainer().set(featureKey, PersistentDataType.STRING, feature);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Toggles a feature and updates the GUI.
     * @param player The player toggling the feature.
     * @param feature The feature to toggle.
     */
    public void toggleFeature(Player player, String feature) {
        boolean newState;
        switch (feature) {
            case "global-limit":
                newState = !configManager.isGlobalLimitEnabled();
                configManager.toggleFeature("global-limit", newState);
                break;
            case "mob-limits":
                newState = !configManager.isMobLimitsEnabled();
                configManager.toggleFeature("mob-limits", newState);
                break;
            case "low-health-delay":
                newState = !configManager.isLowHealthDelayEnabled();
                configManager.toggleFeature("low-health-delay", newState);
                break;
            case "death-cleanup":
                newState = !configManager.isDeathCleanupEnabled();
                configManager.toggleFeature("death-cleanup", newState);
                break;
            case "logging":
                newState = !configManager.isLoggingEnabled();
                configManager.toggleFeature("logging", newState);
                break;
            default:
                player.sendMessage("§cInvalid feature: " + feature);
                return;
        }
        plugin.reload();
        player.sendMessage("§a" + feature.replace("-", " ") + " is now " + (newState ? "enabled" : "disabled"));
        loggerModule.log("GUI", "Player " + player.getName() + " toggled " + feature + " to " + (newState ? "enabled" : "disabled"));
        openGUI(player); // Refresh GUI
    }

    /**
     * Gets the GUI title for validation.
     * @return The GUI title.
     */
    public String getGUITitle() {
        return GUI_TITLE;
    }

    /**
     * Gets the feature key for item tagging.
     * @return The NamespacedKey for feature tags.
     */
    public NamespacedKey getFeatureKey() {
        return featureKey;
    }
}
package com.excrele.ehml;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Listens for inventory click events to handle interactions with the EHML GUI.
 */
public class GUIListener implements Listener {

    private final GUIManager guiManager;

    /**
     * Initializes the GUI listener.
     * @param guiManager The GUI manager handling interactions.
     */
    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * Handles clicks in the EHML GUI to toggle features.
     * @param event The inventory click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (!event.getView().getTitle().equals(guiManager.getGUITitle())) {
            return;
        }

        event.setCancelled(true); // Prevent item movement

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(guiManager.getFeatureKey(), PersistentDataType.STRING)) {
            return;
        }

        String feature = container.get(guiManager.getFeatureKey(), PersistentDataType.STRING);
        Player player = (Player) event.getWhoClicked();
        guiManager.toggleFeature(player, feature);
    }
}
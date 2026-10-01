package fr.piricraft.piricraftCore.listeners;

import fr.piricraft.piricraftCore.models.PiricraftMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof PiricraftMenu menu) {
            event.setCancelled(true);
            menu.onClick(event);
        }
    }

}

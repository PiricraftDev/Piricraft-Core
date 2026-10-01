package fr.piricraft.piricraftCore.listeners;

import fr.piricraft.piricraftCore.managers.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final DatabaseManager databaseManager;

    // Constructor
    public PlayerConnectionListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Load profile from DB, or create if no exists
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        databaseManager.loadProfileAsync(event.getUniqueId(), event.getName());
    }

    // Unload profile
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        databaseManager.unloadProfile(event.getPlayer().getUniqueId());
    }

}

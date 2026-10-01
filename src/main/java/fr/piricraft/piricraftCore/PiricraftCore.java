package fr.piricraft.piricraftCore;

import fr.piricraft.piricraftCore.listeners.PlayerConnectionListener;
import fr.piricraft.piricraftCore.managers.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PiricraftCore extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("PiricraftCore has started !");

        // Register listener
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initDatabase();

        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(this.databaseManager),
                this
        );

    }

    @Override
    public void onDisable() {
        getLogger().info("PiricraftCore has stopped !");
    }
}

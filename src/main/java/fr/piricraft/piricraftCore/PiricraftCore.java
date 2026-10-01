package fr.piricraft.piricraftCore;

import fr.piricraft.piricraftCore.commands.MoneyCommand;
import fr.piricraft.piricraftCore.listeners.PlayerConnectionListener;
import fr.piricraft.piricraftCore.managers.DatabaseManager;
import fr.piricraft.piricraftCore.managers.EconomyManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PiricraftCore extends JavaPlugin {

    private DatabaseManager databaseManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        getLogger().info("PiricraftCore has started !");

        // Initialize manager
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initDatabase();

        this.economyManager = new EconomyManager(this.databaseManager);

        // Register listeners
        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(this.databaseManager),
                this
        );

        // Register commands
        if (getCommand("money") != null) {
            getCommand("money").setExecutor(new MoneyCommand(this.economyManager));
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("PiricraftCore has stopped !");
    }

    // Getters
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}

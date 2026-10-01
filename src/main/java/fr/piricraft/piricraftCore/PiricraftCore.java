package fr.piricraft.piricraftCore;

import fr.piricraft.piricraftCore.commands.EcoAdminCommand;
import fr.piricraft.piricraftCore.commands.MoneyCommand;
import fr.piricraft.piricraftCore.commands.PayCommand;
import fr.piricraft.piricraftCore.commands.completers.GlobalTabCompleter;
import fr.piricraft.piricraftCore.listeners.MenuListener;
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

        // Initialize managers
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initDatabase();

        this.economyManager = new EconomyManager(this.databaseManager);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this.databaseManager), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        // Initialize tab completer
        GlobalTabCompleter globalTabCompleter = new GlobalTabCompleter();

        // Register commands & tab completers
        if (getCommand("money") != null) {
            getCommand("money").setExecutor(new MoneyCommand(this.economyManager));
            getCommand("money").setTabCompleter(globalTabCompleter);
        }

        if (getCommand("pay") != null) {
            getCommand("pay").setExecutor(new PayCommand(this.economyManager));
            getCommand("pay").setTabCompleter(globalTabCompleter);
        }

        if (getCommand("eco") != null) {
            getCommand("eco").setExecutor(new EcoAdminCommand(this.economyManager));
            getCommand("eco").setTabCompleter(globalTabCompleter);
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
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

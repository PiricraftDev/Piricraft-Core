package fr.piricraft.piricraftCore.api;

import fr.piricraft.piricraftCore.PiricraftCore;
import fr.piricraft.piricraftCore.managers.DatabaseManager;
import fr.piricraft.piricraftCore.managers.EconomyManager;

public class PiricraftCoreAPI {

    private static PiricraftCore pluginInstance;

    public static void init(PiricraftCore instance) {
        pluginInstance = instance;
    }

    public static EconomyManager getEconomy() {
        if (pluginInstance == null) {
            throw new IllegalStateException("PiricraftCoreAPI n'a pas été initialisée !");
        }
        return pluginInstance.getEconomyManager();
    }

    public static DatabaseManager getDatabase() {
        if (pluginInstance == null) {
            throw new IllegalStateException("PiricraftCoreAPI n'a pas été initialisée !");
        }
        return pluginInstance.getDatabaseManager();
    }

}

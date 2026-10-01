package fr.piricraft.piricraftCore;

import org.bukkit.plugin.java.JavaPlugin;

public final class PiricraftCore extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("PiricraftCore has started !");
    }

    @Override
    public void onDisable() {
        getLogger().info("PiricraftCore has stopped !");
    }
}

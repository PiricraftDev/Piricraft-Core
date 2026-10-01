package fr.piricraft.piricraftCore.models;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

public abstract class PiricraftMenu implements InventoryHolder {

    protected Inventory inventory;
    protected Component title;
    protected int slots;

    public PiricraftMenu(Component title, int slots) {
        this.title = title;
        this.slots = slots;
        inventory = Bukkit.createInventory(this, slots, title);
    }

    public abstract void setMenuItems(Player player);
    public abstract void onClick(InventoryClickEvent event);

    public void open(Player player) {
        setMenuItems(player);
        player.openInventory(this.inventory);
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }
}

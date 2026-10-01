package fr.piricraft.piricraftCore.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class MenuManager {

    public static ItemStack createItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack addAttributes(ItemStack item, Attribute attribute, AttributeModifier modifier) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addAttributeModifier(attribute, modifier);
            item.setItemMeta(meta);
        }

        return  item;
    }

    public static ItemStack hideFlags(ItemStack item, ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addItemFlags(flags);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createPlayerHead(Player player, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.displayName(name);
            skullMeta.lore(lore);
            item.setItemMeta(skullMeta);
        }

        return item;
    }

}

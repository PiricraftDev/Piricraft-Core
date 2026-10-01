package fr.piricraft.piricraftCore.commands;

import fr.piricraft.piricraftCore.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoneyCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MoneyCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            double balance = economyManager.getBalance(player.getUniqueId());
            Component message = miniMessage.deserialize("<gold>Solde : <yellow>" + balance + " €</yellow></gold>");
            player.sendMessage(message);
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(miniMessage.deserialize("<red>Joueur introuvable ou hors-ligne.</red>"));
                return true;
            }

            double targetBalance = economyManager.getBalance(target.getUniqueId());
            Component message = miniMessage.deserialize("<gold>Solde de <yellow>" + target.getName() + "</yellow> : <yellow>" + targetBalance + " €</yellow></gold>");
            player.sendMessage(message);
            return true;
        }

        return true;
    }
}

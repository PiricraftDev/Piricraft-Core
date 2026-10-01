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

public class PayCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PayCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command !", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(miniMessage.deserialize("<red>Utilisation incorrecte ! Utilisez <yellow>/pay [joueur] [montant]<yellow> !<red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(miniMessage.deserialize("<red>Joueur introuvable ou hors-ligne.</red>"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(miniMessage.deserialize("<red>Vous ne pouvez pas vous envoyer de l'argent à vous-même !</red>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(miniMessage.deserialize("<red>Le montant doit être un nombre valide !</red>"));
            return true;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            player.sendMessage(miniMessage.deserialize("<red>Le montant doit être supérieur à 0 !</red>"));
            return true;
        }

        if (!economyManager.transferMoney(player.getUniqueId(), target.getUniqueId(), amount)) {
            player.sendMessage(miniMessage.deserialize("<red>Vous n'avez pas assez d'argent !</red>"));
            return true;
        }

        player.sendMessage(miniMessage.deserialize("<green>Vous avez envoyé <yellow>" + amount + " €</yellow> à <gold>" + target.getName() + "</gold>.</green>"));
        target.sendMessage(miniMessage.deserialize("<green>Vous avez reçu <yellow>" + amount + " €</yellow> de la part de <gold>" + player.getName() + "</gold>.</green>"));

        return true;
    }
}

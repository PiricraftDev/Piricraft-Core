package fr.piricraft.piricraftCore.commands;

import fr.piricraft.piricraftCore.managers.EconomyManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoAdminCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public EcoAdminCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 3) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage incorrect ! Utilise : /eco <give|take|set> <joueur> <montant></red>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Joueur introuvable ou hors-ligne.</red>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Le montant doit être un nombre valide !</red>"));
            return true;
        }

        switch (subCommand) {
            case "give":
                if (amount <= 0) {
                    sender.sendMessage(miniMessage.deserialize("<red>Le montant doit être supérieur à 0.</red>"));
                    return true;
                }
                economyManager.depositMoney(target.getUniqueId(), amount);
                sender.sendMessage(miniMessage.deserialize("<green>Vous avez ajouté <yellow>" + amount + "€</yellow> à <aqua>" + target.getName() + "</aqua>.</green>"));
                target.sendMessage(miniMessage.deserialize("<yellow>" + amount + "€</yellow> <green>vous ont été ajoutés par un administrateur.</green>"));
                return true;

            case "take":
                if (amount <= 0) {
                    sender.sendMessage(miniMessage.deserialize("<red>Le montant doit être supérieur à 0.</red>"));
                    return true;
                }
                double currentBalance = economyManager.getBalance(target.getUniqueId());
                if (currentBalance < amount) {
                    sender.sendMessage(miniMessage.deserialize("<red>Le joueur <yellow>" + target.getName() + "</yellow> ne possède que <yellow>" + currentBalance + "€</yellow>.</red>"));
                    return true;
                }
                economyManager.withdrawMoney(target.getUniqueId(), amount);
                sender.sendMessage(miniMessage.deserialize("<green>Vous avez retiré <yellow>" + amount + "€</yellow> à <aqua>" + target.getName() + "</aqua>.</green>"));
                target.sendMessage(miniMessage.deserialize("<yellow>" + amount + "€</yellow> <red>vous ont été retirés par un administrateur.</red>"));
                return true;

            case "set":
                if (amount < 0) {
                    sender.sendMessage(miniMessage.deserialize("<red>Le montant ne peut pas être négatif.</red>"));
                    return true;
                }
                economyManager.setBalance(target.getUniqueId(), amount);
                sender.sendMessage(miniMessage.deserialize("<green>Le solde de <aqua>" + target.getName() + "</aqua> a été défini à <yellow>" + amount + "€</yellow>.</green>"));
                target.sendMessage(miniMessage.deserialize("<green>Votre solde a été défini à <yellow>" + amount + "€</yellow> par un administrateur.</green>"));
                return true;

            default:
                sender.sendMessage(miniMessage.deserialize("<red>Sous-commande inconnue. Utilise give, take ou set.</red>"));
                return true;
        }
    }
}
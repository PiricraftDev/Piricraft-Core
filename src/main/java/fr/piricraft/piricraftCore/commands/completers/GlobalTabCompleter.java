package fr.piricraft.piricraftCore.commands.completers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        switch (command.getName().toLowerCase()) {
            case "pay":
                if (args.length == 1) {
                    List<String> players = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.getName().equalsIgnoreCase(sender.getName())) {
                            players.add(p.getName());
                        }
                    }
                    StringUtil.copyPartialMatches(args[0], players, completions);
                } else if (args.length == 2) {
                    StringUtil.copyPartialMatches(args[1], Arrays.asList("10", "50", "100", "500"), completions);
                }
                break;

            case "money":
                if (args.length == 1) {
                    List<String> players = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        players.add(p.getName());
                    }
                    StringUtil.copyPartialMatches(args[0], players, completions);
                }
                break;

            case "ecoadmin":
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], Arrays.asList("give", "take", "set", "reset"), completions);
                } else if (args.length == 2) {
                    List<String> players = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        players.add(p.getName());
                    }
                    StringUtil.copyPartialMatches(args[1], players, completions);
                } else if (args.length == 3) {
                    StringUtil.copyPartialMatches(args[2], Arrays.asList("100", "1000", "5000"), completions);
                }
                break;
        }

        return completions;
    }
}
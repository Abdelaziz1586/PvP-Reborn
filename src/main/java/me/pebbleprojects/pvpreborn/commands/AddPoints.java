package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddPoints implements CommandExecutor {

    private final Handler handler;

    public AddPoints(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (player.hasPermission("pvp.addpoints")) {
                    if (args.length > 1) {
                        final Player target = Bukkit.getPlayer(args[0]);

                        if (target == null) {
                            player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cThere is no player online with that name!"));
                            return;
                        }

                        try {
                            handler.playerDataHandler.addPoints(target.getUniqueId(), Integer.parseInt(args[1]));
                            player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aAdded §e" + args[1] + " §aPoints to §e" + player.getDisplayName()));
                        } catch (final NumberFormatException ignored) {
                            player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cPlease enter a valid number!"));
                        }
                    }
                    handler.runTask(() -> player.performCommand("help"));
                    return;
                }
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));
            }
        }).start();
        return false;
    }
}

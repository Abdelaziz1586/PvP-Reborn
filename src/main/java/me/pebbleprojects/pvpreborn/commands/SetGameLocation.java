package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetGameLocation implements CommandExecutor {

    private final Handler handler;

    public SetGameLocation(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {

                final Player player = (Player) sender;

                if (player.hasPermission("pvp.admin")) {
                    handler.writeData("game.location", player.getLocation());
                    player.sendMessage("§aSet game location.");
                    return;
                }
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));

            }
        }).start();

        return false;
    }
}

package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reload implements CommandExecutor {

    private final Handler handler;

    public Reload(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {

                final Player player = (Player) sender;

                if (player.hasPermission("pvp.admin")) {
                    player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §eReloading..."));
                    handler.npcHandler.unloadNPCs();
                    handler.updateConfig();
                    handler.updateData();
                    handler.npcHandler.loadNPCs();
                    handler.shopHandler.updateInventory();
                    player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aReloaded!"));
                    return;
                }
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));
            }
        }).start();

        return false;
    }
}

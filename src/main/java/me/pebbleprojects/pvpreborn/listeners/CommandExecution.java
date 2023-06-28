package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandExecution implements Listener {

    private final Handler handler;

    public CommandExecution(final Handler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onServerCommand(final ServerCommandEvent event) {
        if (event.getCommand().startsWith("reload")) {
            event.setCancelled(true);

            new Thread(() -> reload(event.getSender())).start();
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/reload")) {
            event.setCancelled(true);

            new Thread(() -> reload(event.getPlayer())).start();
        }
    }

    private void reload(final CommandSender sender) {

        if (sender.hasPermission("pvp.admin")) {
            sender.sendMessage(handler.checkForPrefixAndReplace("%prefix% §eReloading..."));
            handler.npcHandler.unloadNPCs();
            handler.updateConfig();
            handler.updateData();
            handler.npcHandler.loadNPCs();
            handler.shopHandler.updateInventory();
            sender.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aReloaded!"));
            return;
        }

        sender.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));
    }
}

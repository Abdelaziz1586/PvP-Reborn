package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Build implements CommandExecutor {

    private final Handler handler;

    public Build(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (player.hasPermission("pvp.build")) {
                    if (handler.playerDataHandler.buildMode.contains(player.getUniqueId())) {
                        handler.playerDataHandler.buildMode.remove(player.getUniqueId());
                        handler.playerDataHandler.getPlayerReady(player, false);
                        player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou're no longer in build mode."));
                        return;
                    }

                    handler.playerDataHandler.buildMode.add(player.getUniqueId());
                    handler.runTask(() -> player.setGameMode(GameMode.CREATIVE));
                    player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aYou're now in build mode."));
                    return;
                }
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));
            }
        }).start();
        return false;
    }
}

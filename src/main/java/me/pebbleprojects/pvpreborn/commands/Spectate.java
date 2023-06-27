package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spectate implements CommandExecutor {

    private final Handler handler;

    public Spectate(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        handler.runTask(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                Object o = handler.getData("game.yHeight");

                if (o instanceof Integer) {
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        if (player.getLocation().getY() >= Integer.parseInt(o.toString())) {
                            player.setGameMode(GameMode.SPECTATOR);

                            if (args.length > 0) {
                                final Player target = Bukkit.getPlayer(args[0]);

                                if (target != null) player.teleport(target);
                            }

                            player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aYou're now in spectator mode!"));
                            return;
                        }

                        player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou must be at spawn to perform such command!"));
                        return;
                    }

                    handler.playerDataHandler.getPlayerReady(player, false);
                    player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou're no longer in spectator mode!"));
                    return;
                }

                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cThis command is currently disabled."));
            }
        });

        return false;
    }
}

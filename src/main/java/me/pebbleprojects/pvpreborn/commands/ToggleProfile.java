package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleProfile implements CommandExecutor {

    private final Handler handler;

    public ToggleProfile(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (handler.playerDataHandler.toggleProfileStatus(player)) {
                    player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aToggled ON profile"));
                    return;
                }

                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cToggled OFF profile"));
            }
        }).start();

        return false;
    }
}

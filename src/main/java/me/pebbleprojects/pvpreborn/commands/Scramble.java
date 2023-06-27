package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Scramble implements CommandExecutor {

    private final Handler handler;

    public Scramble(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final UUID uuid = ((Player) sender).getUniqueId();

                if (handler.getData("players." + uuid + ".scramble") == null) {
                    handler.writeData("players." + uuid + ".scramble", true);
                    sender.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aToggled ON scramble."));
                    return;
                }
                handler.writeData("players." + uuid + ".scramble", null);
                sender.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cToggled OFF scramble."));
            }
        }).start();
        return false;
    }
}

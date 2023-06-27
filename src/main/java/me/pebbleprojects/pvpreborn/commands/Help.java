package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Help implements CommandExecutor {

    private final Handler handler;

    public Help(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            Object o;
            List<?> list;
            if (sender.hasPermission("pvp.admin")) {
                o = handler.getConfig("otherMessages.invalidArguments.admin", false);
                if (o instanceof List) {
                    list = (List<?>) o;
                    for (final Object message : list) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
                    }
                }
                return;
            }

            o = handler.getConfig("otherMessages.invalidArguments.player", false);
            if (o instanceof List) {
                list = (List<?>) o;
                for (final Object message : list) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
                }
            }
        }).start();

        return false;
    }
}

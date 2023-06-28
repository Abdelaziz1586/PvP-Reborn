package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class TopKills implements CommandExecutor {

    private final Handler handler;

    public TopKills(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            final List<?> list = new ArrayList<>(handler.getDataSection("players"));

            list.sort(Comparator.comparingInt(uuid -> handler.playerDataHandler.getKills(UUID.fromString(uuid.toString()))));

            sender.sendMessage("§e§l--------------------------§6§lTop Kills§e§l--------------------------");

            UUID uuid;
            User user;
            String name;

            reverseList(list);

            int i = 1;
            for (final Object o : list) {

                uuid = UUID.fromString(o.toString());

                name = Bukkit.getOfflinePlayer(uuid).getName();

                if (handler.playerDataHandler.api != null) {
                    user = handler.playerDataHandler.api.getUserManager().getUser(uuid);
                    if (user != null) {
                        final Group group = handler.playerDataHandler.api.getGroupManager().getGroup(user.getPrimaryGroup());
                        final String prefix = group != null ? group.getCachedData().getMetaData().getPrefix() : null;

                        name = prefix + " " + name;
                    }
                }
                sender.sendMessage("§7" + i + " §8» §e" + name);
                i++;
                if (i >= 10) break;
            }

            sender.sendMessage("§e§l--------------------------§6§lTop Kills§e§l--------------------------");

        }).start();
        return false;
    }

    private <T> void reverseList(final List<T> list) {
        if (list.size() <= 1) return;

        T value = list.remove(0);

        reverseList(list);

        list.add(value);
    }
}

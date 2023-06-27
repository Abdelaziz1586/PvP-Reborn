package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TopKills implements CommandExecutor {

    private final Handler handler;

    public TopKills(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            final List<Object> list = new java.util.ArrayList<>(Collections.singletonList(handler.getDataSection("players")));

            list.sort(Comparator.comparingInt(uuid -> handler.playerDataHandler.getKills((UUID) uuid)));

            sender.sendMessage("&e&l--------------------------§6§lTop Kills§e§l--------------------------");

            UUID uuid;
            User user;
            String name;
            for (int i = 9; i >= Math.min(list.size(), 9); i--) {
                uuid = (UUID) list.get(i);

                name = Bukkit.getOfflinePlayer((UUID) list.get(i)).getName();

                if (handler.playerDataHandler.api != null) {
                    user = handler.playerDataHandler.api.getUserManager().getUser(uuid);
                    if (user != null) {
                        final Group group = handler.playerDataHandler.api.getGroupManager().getGroup(user.getPrimaryGroup());
                        final String prefix = group != null ? group.getCachedData().getMetaData().getPrefix() : null;

                        name = prefix + " " + name;
                    }
                }
                sender.sendMessage("§7" + (10-i) + " &8» §e" + name);
            }

            sender.sendMessage("&e&l--------------------------§6§lTop Kills§e§l--------------------------");

        }).start();
        return false;
    }
}

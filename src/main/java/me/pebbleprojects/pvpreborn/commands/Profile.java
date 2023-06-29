package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Profile implements CommandExecutor {

    private final Handler handler;

    public Profile(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (args.length > 0) {
                    sendProfileDetails(player, args[0]);
                    return;
                }

                sendProfileDetails(player, player.getName());
            }
        }).start();
        return false;
    }

    private void sendProfileDetails(final Player player, String query) {
        final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + query).getBytes(StandardCharsets.UTF_8));

        Object o = handler.getData("players." + uuid + ".kills");
        if (!(o instanceof Integer)) {
            player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cOur system doesn't contain any data of that player."));
            return;
        }

        query = handler.playerDataHandler.getDisplayName(uuid, query);

        player.sendMessage("\n§e§l---------------§6§lProfile§e§l---------------");
        player.sendMessage("§e" + query + "'s §7stats");
        player.sendMessage("§7Kills §8» §e" + o);
        player.sendMessage("§7Deaths §8» §e" + handler.playerDataHandler.getDeaths(uuid));
        player.sendMessage("§7Rank §8» §e" + query);
        player.sendMessage("§e§l------------------------------------");
    }
}

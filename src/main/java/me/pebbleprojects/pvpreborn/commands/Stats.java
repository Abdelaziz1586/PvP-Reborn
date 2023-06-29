package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Stats implements CommandExecutor {

    private final PlayerDataHandler handler;

    public Stats(final Handler handler) {
        this.handler = handler.playerDataHandler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                player.sendMessage("\n§e§l----------------§6§lStats§e§l----------------");
                player.sendMessage("§7Kills §8» §e" + handler.getKills(player.getUniqueId()));
                player.sendMessage("§7Deaths §8» §e" + handler.getDeaths(player.getUniqueId()));
                player.sendMessage("§7Highest KillStreak §8» §e" + handler.getHighestKillStreak(player.getUniqueId()));
                player.sendMessage("§7Points §8» §e" + handler.getPoints(player.getUniqueId()));
                player.sendMessage("§7Souls §8» §e" + handler.getSouls(player.getUniqueId()));
                player.sendMessage("§7Rank §8» §e" + player.getDisplayName());
                player.sendMessage("§e§l-------------------------------------");
            }
        }).start();
        return false;
    }
}

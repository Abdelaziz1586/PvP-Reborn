package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    private final PlayerDataHandler handler;

    public PlayerQuit(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (handler.players.contains(player)) handler.leave(player);
    }
}

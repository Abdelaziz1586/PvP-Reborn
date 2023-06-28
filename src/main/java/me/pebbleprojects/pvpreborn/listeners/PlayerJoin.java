package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final PlayerDataHandler handler;

    public PlayerJoin(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage("");

        new Thread(() -> {

            final Player player = event.getPlayer();

            handler.updatePlayerDisplayName(player);

            handler.join(event.getPlayer());
        }).start();
    }
}

package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    private final PlayerDataHandler handler;

    public PlayerDeath(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        if (handler.players.contains(e.getEntity()))
            e.setDeathMessage("");
    }

}

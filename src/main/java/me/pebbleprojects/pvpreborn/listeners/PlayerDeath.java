package me.pebbleprojects.pvpreborn.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        e.setDeathMessage("");
    }

}

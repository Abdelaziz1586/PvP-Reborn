package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    private final PlayerDataHandler handler;

    public BlockPlace(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (!handler.buildMode.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

}

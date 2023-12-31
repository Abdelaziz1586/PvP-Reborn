package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    private final PlayerDataHandler handler;

    public BlockBreak(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {

        if (!handler.buildMode.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

}

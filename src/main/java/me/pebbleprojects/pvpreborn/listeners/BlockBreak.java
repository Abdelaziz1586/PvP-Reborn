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
    public void onBlockBreak(final BlockBreakEvent e) {
        if (handler.players.contains(e.getPlayer()))
            e.setCancelled(true);
    }

}

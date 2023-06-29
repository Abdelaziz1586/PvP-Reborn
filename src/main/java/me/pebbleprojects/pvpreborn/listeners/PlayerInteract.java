package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {

    private final Handler handler;

    public PlayerInteract(final Handler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final ItemStack item = event.getItem();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || item == null || item.getType() == Material.AIR || item.getType() != Material.FLINT_AND_STEEL) return;

        if (item.getDurability() >= 63) {
            event.setCancelled(true);

            event.getPlayer().sendMessage(handler.checkForPrefixAndReplace("%prefix% §cKill a player before you use the §7Flint and Steel"));
        }
    }

}

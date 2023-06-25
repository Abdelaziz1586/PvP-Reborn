package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;

public class PlayerDropItem implements Listener {

    private final Handler handler;
    private final HashMap<Player, ItemStack> sureDestroy;

    public PlayerDropItem(final Handler handler) {
        this.handler = handler;
        sureDestroy = new HashMap<>();
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent e) {
        final Player player = e.getPlayer();
        if (handler.playerDataHandler.players.contains(player)) {
            e.setCancelled(true);
            handler.runTask(() -> {
                final ItemStack droppedItem = e.getItemDrop().getItemStack();
                if (sureDestroy.containsKey(player) && sureDestroy.get(player).equals(droppedItem)) {
                    handler.runTaskLater(() -> player.getInventory().remove(droppedItem), 1);
                    sureDestroy.remove(player);
                    player.sendMessage(handler.getConfig("otherMessages.itemDropDestruction.destroyedMessage", true).toString());
                    return;
                }
                sureDestroy.put(player, droppedItem);
                player.sendMessage(Objects.requireNonNull(handler.getConfig("otherMessages.itemDropDestruction.confirmMessage", true).toString()));
                handler.runTaskLater(() -> sureDestroy.remove(player), 5*20);
            });
        }
    }
}

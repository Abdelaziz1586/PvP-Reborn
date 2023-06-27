package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryClick implements Listener {

    private final Handler handler;

    public InventoryClick(final Handler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player p = (Player) event.getWhoClicked();
        if (handler.playerDataHandler.players.contains(p) && event.getInventory().getHolder() == null) {
            event.setCancelled(true);

            final Inventory inventory = event.getClickedInventory();

            if (inventory == null || event.getClickedInventory().getSize() != 9) return;

            handler.runTask(() -> {
                final ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem == null || clickedItem.getType().equals(Material.AIR))
                    return;
                final ItemMeta clickedItemMeta = clickedItem.getItemMeta();

                if (event.getRawSlot() == 8) {
                    final int page = Integer.parseInt(clickedItemMeta.getLore().get(1).split(": ")[1]) + 1;
                    handler.shopHandler.openShopMenu(p, page);
                    return;
                }

                if (event.getRawSlot() == 0) {
                    if (CraftItemStack.asNMSCopy(clickedItem).getTag().hasKey("previousPage")) {
                        final int page = Integer.parseInt(clickedItemMeta.getLore().get(1).split(": ")[1]) - 1;
                        handler.shopHandler.openShopMenu(p, page);
                        return;
                    }
                }

                handler.shopHandler.buy(p, clickedItem.getType().name());
            });
        }
    }
}

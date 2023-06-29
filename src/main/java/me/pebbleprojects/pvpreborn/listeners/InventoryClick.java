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
        if (event.getInventory().getHolder() == null) {
            event.setCancelled(true);

            new Thread(() -> {
                final Player player = (Player) event.getWhoClicked();

                final Inventory inventory = event.getClickedInventory();

                if (inventory == null || event.getClickedInventory().getSize() != 18) return;

                final ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;

                final ItemMeta clickedItemMeta = clickedItem.getItemMeta();

                if (clickedItem.getType().equals(Material.SKULL_ITEM)) return;

                if (event.getRawSlot() == 17) {
                    final int page = Integer.parseInt(clickedItemMeta.getLore().get(1).split(": ")[1]) + 1;
                    handler.shopHandler.openShopMenu(player, page);
                    return;
                }

                if (event.getRawSlot() == 9) {
                    if (CraftItemStack.asNMSCopy(clickedItem).getTag().hasKey("previousPage")) {
                        final int page = Integer.parseInt(clickedItemMeta.getLore().get(1).split(": ")[1]) - 1;
                        handler.shopHandler.openShopMenu(player, page);
                        return;
                    }
                }

                handler.shopHandler.buy(player, clickedItem.getType().name());
            }).start();
        }
    }
}

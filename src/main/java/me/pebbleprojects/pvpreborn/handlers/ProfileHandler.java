package me.pebbleprojects.pvpreborn.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ProfileHandler {

    private final String title;
    private final Handler handler;

    public ProfileHandler(final Handler handler) {
        this.handler = handler;
        title = handler.getConfig("profile.guiName", true).toString();
    }

    public void openProfileMenu(final Player requester, final String player) {
        new Thread(() -> {

            final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(StandardCharsets.UTF_8));

            if (!requester.getName().equalsIgnoreCase(player)) {
                final Object o = handler.getData("players." + uuid + ".profile");

                if (o instanceof Boolean) {
                    if (!Boolean.parseBoolean(o.toString())) {
                        requester.sendMessage("§cSorry but that player has their profile locked");
                        return;
                    }
                }
            }

            final Inventory inventory = Bukkit.createInventory(null, 54, title.replace("%query%", player));

            final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            final SkullMeta headMeta = (SkullMeta) head.getItemMeta();

            headMeta.setOwner(player);
            headMeta.setDisplayName("§6" + player + "'s §bProfile");

            head.setItemMeta(headMeta);

            inventory.setItem(13, head);

            inventory.setItem(29, handler.playerDataHandler.createItemStack(Material.DIAMOND_SWORD, "§aKills: §6" + handler.playerDataHandler.getKills(uuid), null, 1, false));
            inventory.setItem(31, handler.playerDataHandler.createItemStack(Material.REDSTONE_BLOCK, "§cDeaths: §6" + handler.playerDataHandler.getDeaths(uuid), null, 1, false));
            inventory.setItem(33, handler.playerDataHandler.createItemStack(Material.DOUBLE_PLANT, "§eCoins: §6" + handler.playerDataHandler.getCoins(uuid), null, 1, false));

            requester.openInventory(inventory);
        }).start();
    }

}

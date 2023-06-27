package me.pebbleprojects.pvpreborn.handlers;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class ShopHandler {

    private final Handler handler;
    private final Inventory[] inventory;

    public ShopHandler(final Handler handler) {
        this.handler = handler;
        inventory = new Inventory[]{Bukkit.createInventory(null, 9, handler.getConfig("shop.guiName", true).toString())};
        updateInventory();
    }

    public void updateInventory() {
        handler.runTask(() -> {
            inventory[0] = Bukkit.createInventory(null, 9, handler.getConfig("shop.guiName", true).toString());

            final ItemStack nextPage = getHead("MHF_ArrowRight");
            final ItemMeta nextPageItemMeta = nextPage.getItemMeta();
            nextPageItemMeta.setLore(Arrays.asList("§7Left click: go to next page", "§7Current page: 1"));
            nextPageItemMeta.setDisplayName("§7Next Page");
            nextPage.setItemMeta(nextPageItemMeta);


            ItemStack previousPage = getHead("MHF_ArrowLeft");
            final ItemMeta previousPageItemMeta = previousPage.getItemMeta();
            previousPageItemMeta.setLore(Arrays.asList("§7Left click: go back", "§7Current page: 1"));
            previousPageItemMeta.setDisplayName("§7Next Page");
            previousPage.setItemMeta(previousPageItemMeta);

            final net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(previousPage);
            final NBTTagCompound tag = nmsStack.getTag();
            tag.setBoolean("previousPage", true);
            nmsStack.setTag(tag);

            previousPage = CraftItemStack.asBukkitCopy(nmsStack);

            int i = 0;
            Material material;
            int currentInventory = 0;
            for (final String item : handler.getConfigSection("shop.items")) {
                if (i >= 7) {
                    i = 1;
                    currentInventory++;
                    nextPageItemMeta.getLore().set(1, "§7Current page: " + currentInventory+1);
                    previousPageItemMeta.getLore().set(1, "§7Current page: " + currentInventory+1);
                    nextPage.setItemMeta(nextPageItemMeta);
                    previousPage.setItemMeta(previousPageItemMeta);
                    inventory[currentInventory-1].setItem(8, nextPage);
                    inventory[currentInventory].setItem(0, previousPage);
                }
                material = Material.getMaterial(item.toUpperCase());
                if (material != null) {
                    inventory[currentInventory].setItem(i, handler.playerDataHandler.createItemStack(material, handler.getConfig("shop.items." + item + ".gui.name", true).toString(), handler.getConfigList("shop.items." + item + ".gui.lore", true, null), 1, false));
                    i++;
                }
            }
        });
    }

    public void openShopMenu(final Player player, final int menu) {
        final Object exception = handler.getData("locations.game");

        if (exception instanceof Location) {
            if (((Location) exception).getY() - player.getLocation().getY() > 1) {
                player.sendMessage("§cYou can't open the shop at such location!");
                return;
            }
        }

        if (inventory[menu] != null) player.openInventory(inventory[menu]);
    }

    public void buy(final Player player, final String shopItem) {
        final Material material = Material.getMaterial(shopItem);
        if (material != null) {
            Object o = handler.getConfig("shop.items." + shopItem.toLowerCase() + ".item.name", true);
            if (o == null) o = "";
            ItemStack itemStack;
            if (Boolean.parseBoolean(handler.getConfig("shop.items." + shopItem.toLowerCase() + ".item.lore.enabled", false).toString())) {
                itemStack = handler.playerDataHandler.createItemStack(material, o.toString(), handler.getConfigList("shop.items." + shopItem.toLowerCase() + ".item.lore.lore", true, null), 1, true);
            } else {
                itemStack = handler.playerDataHandler.createItemStack(material, o.toString(), null, 1, true);
            }

            o = handler.getConfig("shop.items." + shopItem.toLowerCase() + ".price", false);
            final String typeNameString = itemStack.getType().name();
            if (o instanceof Integer) {
                final int price = (Integer) o;
                if (handler.playerDataHandler.getSouls(player.getUniqueId()) >= price) {
                    if (!hasFreeSpace(player)) {
                        player.sendMessage(handler.getConfig("shop.fullInventory", true).toString().replace("%query%", handler.getConfig("shop.items." + shopItem.toLowerCase() + ".queryName", true).toString()));
                        return;
                    }
                    final PlayerInventory inventory = player.getInventory();
                    if (!Boolean.parseBoolean(handler.getConfig("shop.items." + shopItem.toLowerCase() + ".canBuyMultipleOf", false).toString())  && (inventory.contains(itemStack) || Arrays.asList(inventory.getArmorContents()).contains(itemStack))) {
                        player.sendMessage(handler.getConfig("shop.alreadyHave", true).toString().replace("%query%", handler.getConfig("shop.items." + shopItem.toLowerCase() + ".queryName", true).toString()));
                        return;
                    }
                    try {
                        itemStack.setAmount(Integer.parseInt(handler.getConfig("shop.items." + shopItem.toLowerCase() + ".amount", false).toString()));
                    } catch (final NumberFormatException ignored) {}
                    handler.playerDataHandler.removeSouls(player.getUniqueId(), price);
                    player.sendMessage(handler.getConfig("shop.items." + shopItem.toLowerCase() + ".purchaseMessage", true).toString().replace("%query%", handler.getConfig("shop.items." + shopItem.toLowerCase() + ".queryName", true).toString()));
                    handler.playerDataHandler.updateScoreboard(player);
                    if (typeNameString.endsWith("_HELMET")) {
                        inventory.setHelmet(itemStack);
                        return;
                    }

                    if (typeNameString.endsWith("_CHESTPLATE")) {
                        inventory.setChestplate(itemStack);
                        return;
                    }

                    if (typeNameString.endsWith("_LEGGINGS")) {
                        inventory.setLeggings(itemStack);
                        return;
                    }

                    if (typeNameString.endsWith("_BOOTS")) {
                        inventory.setBoots(itemStack);
                        return;
                    }

                    inventory.addItem(itemStack);
                    return;
                }
                player.sendMessage(handler.getConfig("shop.noEnoughSouls", true).toString().replace("%query%", handler.getConfig("shop.items." + shopItem.toLowerCase() + ".queryName", true).toString()));
            }
        }
    }


    // Internal Functions

    private ItemStack getHead(final String player) {
        final ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta skull = (SkullMeta) itemStack.getItemMeta();
        skull.setOwner(player);
        itemStack.setItemMeta(skull);
        return itemStack;
    }

    private boolean hasFreeSpace(final Player player) {
        for (final ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null)
                return true;
        }
        return false;
    }

}

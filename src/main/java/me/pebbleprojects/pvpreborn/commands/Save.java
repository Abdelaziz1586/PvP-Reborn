package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Save implements CommandExecutor {

    private final Handler handler;

    public Save(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                final PlayerInventory inventory = player.getInventory();
                final ItemStack[] items = inventory.getContents();

                Material material;
                ItemStack itemStack;
                for (int i = 0; i < items.length; i++) {
                    itemStack = items[i];

                    if (itemStack == null) continue;

                    material = itemStack.getType();

                    if (material.name().endsWith("_SWORD")) {
                        handler.writeData("players." + player.getUniqueId() + ".savedInventory.sword", i);
                        continue;
                    }
                    if (material.equals(Material.FISHING_ROD)) {
                        handler.writeData("players." + player.getUniqueId() + ".savedInventory.rod", i);
                        continue;
                    }
                    if (material.equals(Material.BOW)) {
                        handler.writeData("players." + player.getUniqueId() + ".savedInventory.bow", i);
                        continue;
                    }
                    if (material.equals(Material.FLINT_AND_STEEL)) {
                        handler.writeData("players." + player.getUniqueId() + ".savedInventory.flint", i);
                    }
                }

                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §aSaved your inventory slots"));
            }
        }).start();

        return false;
    }
}

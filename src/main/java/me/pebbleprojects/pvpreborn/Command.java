package me.pebbleprojects.pvpreborn;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import me.pebbleprojects.pvpreborn.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Command implements CommandExecutor {

    private final Handler handler;

    public Command(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (args.length > 0) {

                    if (args[0].equalsIgnoreCase("reload") && player.hasPermission("pvp.reload")) {
                        player.sendMessage("§eReloading...");
                        handler.npcHandler.unloadNPCs();
                        handler.updateConfig();
                        handler.updateData();
                        handler.npcHandler.loadNPCs();
                        handler.shopHandler.updateInventory();
                        player.sendMessage("§aReloaded!");
                        return;
                    }

                    if (args[0].equalsIgnoreCase("setGameLocation") && player.hasPermission("pvp.admin")) {
                        handler.writeData("locations.game", player.getLocation());
                        player.sendMessage("§aSet game location.");
                        return;
                    }

                    if (args[0].equalsIgnoreCase("shop")) {
                        handler.shopHandler.openShopMenu(player, 0);
                        return;
                    }

                    if (args[0].equalsIgnoreCase("profile")) {
                        if (handler.playerDataHandler.players.contains(player)) {
                            if (args.length > 1) {
                                handler.runTask(() -> handler.profileHandler.openProfileMenu(player, args[1]));
                                return;
                            }

                            handler.profileHandler.openProfileMenu(player, player.getName());
                        }
                        return;
                    }

                    if (args[0].equalsIgnoreCase("toggleProfile")) {
                        if (handler.playerDataHandler.players.contains(player)) {
                            if (handler.playerDataHandler.toggleProfileStatus(player)) {
                                player.sendMessage("§aToggled ON profile");
                                return;
                            }

                            player.sendMessage("§cToggled OFF profile");
                        }
                        return;
                    }

                    if (args[0].equalsIgnoreCase("setShopNPC") && player.hasPermission("pvp.admin")) {
                        player.sendMessage("§eCreating Shop NPC...");
                        final NPC npc = handler.npcHandler.createNPC(handler.getConfig("shop.npc.name", false).toString(), player.getLocation(), 0);
                        npc.setSkin(handler.getConfig("shop.npc.skinName", false).toString());
                        npc.save();
                        handler.playerDataHandler.players.forEach(npc::sendShowPacket);
                        player.sendMessage("§aCreated Shop NPC at your location.");
                        if (!handler.playerDataHandler.players.contains(player)) {
                            player.sendMessage("§eNOTE: as you're not in the game, you won't be able to see the NPC. Join to see the NPC you have created!");
                        }
                        return;
                    }

                    if (args[0].equalsIgnoreCase("setProfileNPC") && player.hasPermission("pvp.admin")) {
                        player.sendMessage("§eCreating Profile NPC...");
                        final NPC npc = handler.npcHandler.createNPC(handler.getConfig("profile.npc.name", false).toString(), player.getLocation(), 1);
                        npc.setSkin(handler.getConfig("profile.npc.skinName", false).toString());
                        npc.save();
                        handler.playerDataHandler.players.forEach(npc::sendShowPacket);
                        player.sendMessage("§aCreated Profile NPC at your location.");
                        if (!handler.playerDataHandler.players.contains(player)) {
                            player.sendMessage("§eNOTE: as you're not in the game, you won't be able to see the NPC. Join to see the NPC you have created!");
                        }
                        return;
                    }

                    if (args[0].equalsIgnoreCase("removeNPC") && player.hasPermission("pvp.admin")) {
                        if (args.length >= 2) {
                            try {
                                final int id = Integer.parseInt(args[1]);
                                if (id >= 1) {
                                    if (handler.getData("NPCs." + id + ".name") != null) {
                                        handler.writeData("NPCs." + id, null);
                                        player.sendMessage("§aSuccessfully removed NPC with ID of §e" + id);
                                        handler.npcHandler.deleteNPC(id);
                                        return;
                                    }
                                }
                                player.sendMessage("§cThere is no NPC with such an ID!");
                                return;
                            } catch (final NumberFormatException ignored) {
                                player.sendMessage("§cID of NPCs start from 1 to above.");
                            }
                            return;
                        }
                    }
                }
                sendHelpList(sender);
            }
        }).start();
        return false;
    }

    
    // Internal Functions

    private void sendHelpList(final CommandSender sender) {
        Object o;
        List<?> list;
        if (sender.hasPermission("pvp.admin")) {
            o = handler.getConfig("otherMessages.invalidArguments.admin", false);
            if (o instanceof List) {
                list = (List<?>) o;
                for (final Object message : list) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
                }
            }
            return;
        }

        o = handler.getConfig("otherMessages.invalidArguments.player", false);
        if (o instanceof List) {
            list = (List<?>) o;
            for (final Object message : list) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
            }
        }
    }
}

package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import me.pebbleprojects.pvpreborn.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetShopNPC implements CommandExecutor {

    private final Handler handler;

    public SetShopNPC(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {

            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (player.hasPermission("pvp.admin")) {
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
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));
            }

        }).start();
        return false;
    }
}

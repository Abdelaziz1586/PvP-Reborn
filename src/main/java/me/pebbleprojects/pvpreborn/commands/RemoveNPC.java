package me.pebbleprojects.pvpreborn.commands;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveNPC implements CommandExecutor {

    private final Handler handler;

    public RemoveNPC(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {

                final Player player = (Player) sender;

                if (player.hasPermission("pvp.admin")) {
                    if (args.length > 0) {
                        try {
                            final int id = Integer.parseInt(args[0]);
                            if (id >= 1) {
                                if (handler.getData("NPCs." + id + ".name") != null) {
                                    handler.writeData("NPCs." + id, null);
                                    player.sendMessage("§aSuccessfully removed NPC with ID of §e" + id);
                                    handler.npcHandler.deleteNPC(id);
                                    return;
                                }
                            }
                            player.sendMessage("§cThere is no NPC with such an ID!");
                        } catch (final NumberFormatException ignored) {
                            player.sendMessage("§cID of NPCs start from 1 to above.");
                        }
                        return;
                    }
                    player.performCommand("help");
                    return;
                }
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou don't have permission to use this command"));

            }
        }).start();
        return false;
    }
}

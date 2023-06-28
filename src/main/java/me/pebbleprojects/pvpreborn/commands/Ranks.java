package me.pebbleprojects.pvpreborn.commands;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Ranks implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                player.sendMessage("\n§eAvailable Ranks");
                player.sendMessage("§5LEGENDARY §8» §e17,500 §bPoints");

                TextComponent component = new net.md_5.bungee.api.chat.TextComponent("§5Amethyst §8» §e15,000 -> 17,000 §bPoints");

                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5Amethyst I §8» §e15,000\n§5Amethyst II §8» §e15,500\n§5Amethyst III §8» §e16,000\n§5Amethyst IV §8» §e16,500\n§5Amethyst V §8» §e17,000").create()));

                player.spigot().sendMessage(component);

                component = new TextComponent("§4Ultimate §8» §e12,500 -> 14,500 §bPoints");
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§4Ultimate I §8» §e12,500\n§4Ultimate II §8» §e13,000\n§4Ultimate III §8» §e13,500\n§4Ultimate IV §8» §e14,000\n§4Ultimate V §8» §e14,500").create()));

                player.spigot().sendMessage(component);

                component = new TextComponent("§3Special §8» §e10,000 -> 12,000 §bPoints");
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§3Special I §8» §e10,000\n§3Special II §8» §e10,500\n§3Special III §8» §e11,000\n§3Special IV §8» §e11,500\n§3Special V §8» §e12,000").create()));

                player.spigot().sendMessage(component);

                component = new TextComponent("§8Iron §8» §e7,500 -> 9,500 §bPoints");
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8Iron I §8» §e7,500\n§8Iron II §8» §e8,000\n§8Iron III §8» §e8,500\n§8Iron IV §8» §e9,000\n§8Iron V §8» §e9,500").create()));

                player.spigot().sendMessage(component);

                component = new TextComponent("§8Silver §8» §e5,000 -> 7,000 §bPoints");
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8Silver I §8» §e5,000\n§8Silver II §8» §e5,500\n§8Silver III §8» §e6,000\n§8Silver IV §8» §e6,500\n§8Silver V §8» §e7,000").create()));

                player.spigot().sendMessage(component);

                component = new TextComponent("§6Bronze §8» §e1,500 -> 4,500 §bPoints");
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§6Bronze I §8» §e1,500\n§6Bronze II §8» §e2,000\n§6Bronze III §8» §e2,500\n§6Bronze IV §8» §e3,000\n§6Bronze V §8» §e4,500").create()));

                player.spigot().sendMessage(component);

                player.sendMessage("§eUNRANKED §8» §e<1,500 §bPoints");

            }

        }).start();
        return false;
    }
}

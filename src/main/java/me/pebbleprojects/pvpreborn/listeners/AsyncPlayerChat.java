package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChat implements Listener {

    private final Handler handler;

    public AsyncPlayerChat(final Handler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        handler.runTask(() -> {
            if (Boolean.parseBoolean(handler.getConfig("chat-format.enabled", false).toString())) {
                handler.playerDataHandler.broadcast(handler.getConfig("chat-format.format", true).toString().replace("%player%", e.getPlayer().getDisplayName()).replace("%message%", e.getMessage()));
            }
        });
    }

}

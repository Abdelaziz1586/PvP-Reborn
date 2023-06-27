package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final PlayerDataHandler handler;

    public PlayerJoin(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage("");

        new Thread(() -> {
            handler.join(event.getPlayer());

            final Player player = event.getPlayer();

            if (handler.api == null) return;

            final User user = handler.api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {

                final Group group = handler.api.getGroupManager().getGroup(user.getPrimaryGroup());
                final String prefix = group != null ? group.getCachedData().getMetaData().getPrefix() : null;

                player.setDisplayName(prefix + " " + player.getName());
            }
        }).start();
    }
}

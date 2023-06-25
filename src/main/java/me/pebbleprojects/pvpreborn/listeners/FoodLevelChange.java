package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChange implements Listener {

    private final PlayerDataHandler handler;

    public FoodLevelChange(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent e) {
        if (handler.players.contains((Player) e.getEntity()))
            e.setCancelled(true);
    }

}

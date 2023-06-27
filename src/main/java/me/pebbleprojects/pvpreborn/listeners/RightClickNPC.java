package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import me.pebbleprojects.pvpreborn.npc.customEvent.RightClickNPCEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RightClickNPC implements Listener {

    private final Handler handler;

    public RightClickNPC(final Handler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onRightClickNPC(final RightClickNPCEvent event) {
        handler.runTask(() -> {
            final Player player = event.getPlayer();
            if (handler.playerDataHandler.players.contains(player)) {
                if (handler.npcHandler.getCustomData(event.getNPC().getNpc()) == 0) {
                    handler.shopHandler.openShopMenu(player, 0);
                }
            }
        });
    }

}

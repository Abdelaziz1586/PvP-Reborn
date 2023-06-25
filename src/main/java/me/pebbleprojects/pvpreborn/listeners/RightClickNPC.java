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
    public void onRightClickNPC(final RightClickNPCEvent e) {
        handler.runTask(() -> {
            final Player player = e.getPlayer();
            if (handler.playerDataHandler.players.contains(player)) {
                if (handler.npcHandler.getCustomData(e.getNPC().getNpc()) == 0) {
                    player.performCommand("pvp shop");
                    return;
                }

                if (handler.npcHandler.getCustomData(e.getNPC().getNpc()) == 1)
                    player.performCommand("pvp profile");
            }
        });
    }

}

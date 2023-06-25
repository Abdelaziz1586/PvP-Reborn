package me.pebbleprojects.pvpreborn.listeners;

import me.pebbleprojects.pvpreborn.handlers.PlayerDataHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    private final PlayerDataHandler handler;

    public EntityDamage(final PlayerDataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof Player) {
            final Player player = (Player) entity;

            player.setMaximumNoDamageTicks(16);

            if (handler.players.contains(player)) {
                final EntityDamageEvent.DamageCause cause = e.getCause();
                if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
                    e.setCancelled(true);
                    return;
                }

                if (cause.equals(EntityDamageEvent.DamageCause.VOID)) {
                    e.setCancelled(true);
                    handler.death(player, null, cause);
                    return;
                }

                Player attacker = null;

                if (e instanceof EntityDamageByEntityEvent) {
                    entity = ((EntityDamageByEntityEvent) e).getDamager();
                    if (entity instanceof Player) {
                        attacker = (Player) entity;
                        if (!handler.players.contains(attacker)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }

                if (player.getHealth() <= e.getDamage())
                    handler.death(player, attacker, cause);
            }
        }
    }


    // Internal Functionsx
}

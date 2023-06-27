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
    public void onEntityDamage(final EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            final Player player = (Player) entity;

            player.setMaximumNoDamageTicks(17);

            if (handler.players.contains(player)) {
                final EntityDamageEvent.DamageCause cause = event.getCause();
                if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
                    event.setCancelled(true);
                    return;
                }

                if (cause.equals(EntityDamageEvent.DamageCause.VOID)) {
                    event.setCancelled(true);
                    handler.death(player, null, cause);
                    return;
                }

                Player attacker = null;

                if (event instanceof EntityDamageByEntityEvent) {
                    entity = ((EntityDamageByEntityEvent) event).getDamager();
                    if (entity instanceof Player) {
                        attacker = (Player) entity;
                        if (!handler.players.contains(attacker) && handler.buildMode.contains(attacker.getUniqueId())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                if (player.getHealth() <= event.getDamage())
                    handler.death(player, attacker, cause);
            }
        }
    }


    // Internal Functionsx
}

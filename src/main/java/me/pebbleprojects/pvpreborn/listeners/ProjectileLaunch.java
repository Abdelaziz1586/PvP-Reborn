package me.pebbleprojects.pvpreborn.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunch implements Listener {

    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        final Projectile hook = event.getEntity();

        if (event.getEntityType().equals(EntityType.FISHING_HOOK)) {
            hook.setVelocity(hook.getVelocity().multiply(1.6));
        }
    }

}

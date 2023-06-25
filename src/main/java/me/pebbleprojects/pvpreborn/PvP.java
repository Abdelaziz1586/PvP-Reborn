package me.pebbleprojects.pvpreborn;

import me.pebbleprojects.pvpreborn.handlers.Handler;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;
import java.util.TimerTask;

public final class PvP extends JavaPlugin {

    private Handler handler;
    public static PvP INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        new Thread(() -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                final ConsoleCommandSender console = getServer().getConsoleSender();
                console.sendMessage("§eLoading §6PvP§e...");
                handler = new Handler();
                console.sendMessage("§aLoaded §6PvP");
            }
        }, 1000)).start();
    }

    @Override
    public void onDisable() {
        handler.shutdown();
        handler = null;
        System.gc();
        getServer().getConsoleSender().sendMessage("§cUnloaded §6PvP");
    }
}

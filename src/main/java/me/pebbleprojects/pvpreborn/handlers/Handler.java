package me.pebbleprojects.pvpreborn.handlers;

import me.pebbleprojects.pvpreborn.Command;
import me.pebbleprojects.pvpreborn.PvP;
import me.pebbleprojects.pvpreborn.listeners.*;
import me.pebbleprojects.pvpreborn.npc.NPCHandler;
import me.pebbleprojects.pvpreborn.npc.customEvent.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Handler {

    public final PvP main;
    private final File dataFile;
    public final NPCHandler npcHandler;
    public final ShopHandler shopHandler;
    private FileConfiguration config, data;
    public final PacketReader packetReader;
    public final ProfileHandler profileHandler;
    public final PlayerDataHandler playerDataHandler;

    public Handler() {
        main = PvP.INSTANCE;

        main.getConfig().options().copyDefaults(true);
        main.saveDefaultConfig();
        updateConfig();

        dataFile = new File(main.getDataFolder().getPath(), "data.yml");

        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile())
                    main.getServer().getConsoleSender().sendMessage("§aCreated data.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateData();

        playerDataHandler = new PlayerDataHandler(this);

        npcHandler = new NPCHandler(this);

        shopHandler = new ShopHandler(this);

        profileHandler = new ProfileHandler(this);

        packetReader = new PacketReader(this);

        main.getCommand("PvP").setExecutor(new Command(this));

        final PluginManager pm = main.getServer().getPluginManager();

        pm.registerEvents(new AsyncPlayerChat(this), main);
        pm.registerEvents(new BlockBreak(playerDataHandler), main);
        pm.registerEvents(new EntityDamage(playerDataHandler), main);
        pm.registerEvents(new FoodLevelChange(playerDataHandler), main);
        pm.registerEvents(new InventoryClick(this), main);
        pm.registerEvents(new PlayerDeath(playerDataHandler), main);
        pm.registerEvents(new PlayerDropItem(this), main);
        pm.registerEvents(new PlayerJoin(playerDataHandler), main);
        pm.registerEvents(new PlayerQuit(playerDataHandler), main);
        pm.registerEvents(new RightClickNPC(this), main);
    }

    public void shutdown() {
        playerDataHandler.shutdown();
        npcHandler.unloadNPCs();
    }

    public void updateConfig() {
        main.reloadConfig();
        config = main.getConfig();
    }

    public void updateData() {
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void writeData(final String key, final Object value) {
        data.set(key, value);
        try {
            data.save(dataFile);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    public void runTask(final Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTask(main);
            return;
        }
        runnable.run();
    }


    public void runTaskLater(final Runnable runnable, final int delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, runnable, delay);
    }
    public Object getConfig(final String key, final boolean translate) {
        return config.isSet(key) ? (translate ? ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(key))).replace("%nl%", "\n") : config.get(key)) : null;
    }

    public Set<String> getConfigSection(final String key) {
        return config.isSet(key) ? config.getConfigurationSection(key).getKeys(false) : null;
    }

    public Set<String> getDataSection(final String key) {
        return data.isSet(key) ? data.getConfigurationSection(key).getKeys(false) : null;
    }

    public List<String> getConfigList(final String key, final boolean translate, final Player player) {
        if (!config.isSet(key)) return null;

        final List<String> list = config.getStringList(key);

        if (translate) {
            String s;
            for (int i = 0; i < list.size(); i++) {
                s = player != null ? playerDataHandler.replaceStringWithData(player.getUniqueId(), list.get(i)).replace("%player%", player.getName()) : list.get(i);
                list.set(i, ChatColor.translateAlternateColorCodes('&', s));
            }
        }
        return list;
    }

    public Object getData(final String key) {
        return data.isSet(key) ? data.get(key) : null;
    }
}

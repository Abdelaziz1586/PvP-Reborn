package me.pebbleprojects.pvpreborn.handlers;

import fr.mrmicky.fastboard.FastBoard;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerDataHandler {

    private final Handler handler;
    public final ArrayList<Player> players;
    public final HashMap<UUID, Player> lastDamage;
    private final HashMap<UUID, Integer> killStreaks;
    private final HashMap<UUID, FastBoard> scoreboards;

    public PlayerDataHandler(final Handler handler) {
        this.handler = handler;

        players = new ArrayList<>();
        lastDamage = new HashMap<>();
        scoreboards = new HashMap<>();
        killStreaks = new HashMap<>();
    }

    public void shutdown() {
        final ArrayList<Player> playersCache = new ArrayList<>(players);
        for (final Player player : playersCache) {
            quickLeave(player);
        }
    }

    public void broadcast(final String message) {
        for (final Player player : players) player.sendMessage(message);
    }

    public void sendTitle(final Player player, final String title, final String subTitle) {
        PacketPlayOutTitle packet;
        final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        if (title != null) {
            packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title), 1, 5, 1);
            playerConnection.sendPacket(packet);
        }
        if (subTitle != null) {
            packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subTitle), 1, 5, 1);
            playerConnection.sendPacket(packet);
        }
    }

    public void sendActionbar(final Player player, final String message) {
        if (player == null || message == null) return;

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
    }

    public void join(final Player player) {
        final Object o = handler.getData("locations.game");
        if (!(o instanceof Location)) {
            if (player.isOp()) {
                player.sendMessage("§cYou haven't set the game location yet.");
                return;
            }
            player.kickPlayer("§cGame is under setup, come back later!");
            return;
        }

        players.add(player);
        getPlayerReady(player);
        player.setFoodLevel(20);
        handler.npcHandler.loadNPCs(player);
        handler.packetReader.inject(player);
    }

    public void leave(final Player player) {
        if (lastDamage.containsKey(player.getUniqueId())) {
            death(player, lastDamage.get(player.getUniqueId()), null);
        }

        lastDamage.remove(player.getUniqueId());
        players.remove(player);
        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
            return;
        }

        handler.npcHandler.unloadNPCs(player);
        handler.packetReader.uninject(player);
    }

    public void getPlayerReady(final Player player) {
        final PlayerInventory playerInventory = player.getInventory();

        playerInventory.setHelmet(createItemStack(Material.IRON_HELMET, "§7", null, 1, true));
        playerInventory.setChestplate(createItemStack(Material.IRON_CHESTPLATE, "§7", null, 1, true));
        playerInventory.setLeggings(createItemStack(Material.IRON_LEGGINGS, "§7", null, 1, true));
        playerInventory.setBoots(createItemStack(Material.IRON_BOOTS, "§7", null, 1, true));

        playerInventory.setItem(0, createItemStack(Material.IRON_SWORD, "§7Iron Sword", null, 1, true));

        final Object o = handler.getData("locations.game");
        if (o instanceof Location) handler.runTask(() -> player.teleport((Location) o));

        player.setFireTicks(0);
        updateScoreboard(player);
    }

    public final boolean toggleProfileStatus(final Player player) {
        final Object o = handler.getData("players." + player.getUniqueId() + ".profile");

        final boolean status = !(o != null && Boolean.parseBoolean(o.toString()));

        handler.writeData("players." + player.getUniqueId() + ".profile", status);

        return status;
    }

    public void death(final Player victim, final Player attacker, final EntityDamageEvent.DamageCause cause) {
        victim.getInventory().clear();
        addDeath(victim.getUniqueId());
        victim.setHealth(victim.getMaxHealth());

        final Object o = handler.getConfig("kill.coins", false);
        int coins = 20;
        if (o instanceof Integer)
            coins = (Integer) o;

        if (attacker != null) {
            addKill(attacker.getUniqueId());
            addKillStreak(attacker.getUniqueId());
            addCoins(attacker.getUniqueId(), coins);
            attacker.setHealth(attacker.getMaxHealth());
            getPlayerReady(attacker);
        }
        sendSuitableDeathMessage(victim, attacker, coins, cause);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (victim.isOnline()) getPlayerReady(victim);
            }
        }, 50);
    }

    private void addKill(final UUID uuid) {
        handler.writeData("players." + uuid + ".kills", getKills(uuid)+1);
    }

    public void addKillStreak(final UUID uuid) {
        killStreaks.put(uuid, getKillStreaks(uuid)+1);
    }

    public void addDeath(final UUID uuid) {
        handler.writeData("players." + uuid + ".deaths", getDeaths(uuid)+1);
    }

    public void addCoins(final UUID uuid, final int coins) {
        handler.writeData("players." + uuid + ".coins", getCoins(uuid)+coins);
    }

    public void removeCoins(final UUID uuid, final int coins) {
        handler.writeData("players." + uuid + ".coins", getCoins(uuid)-coins);
    }

    public final int getKills(final UUID uuid) {
        int i = 0;
        final Object o = handler.getData("players." + uuid + ".kills");
        if (o instanceof Integer)
            i = (Integer) o;
        return i;
    }

    public final int getKillStreaks(final UUID uuid) {
        return killStreaks.getOrDefault(uuid, 0);
    }

    public final int getDeaths(final UUID uuid) {
        int i = 0;
        final Object o = handler.getData("players." + uuid + ".deaths");
        if (o instanceof Integer)
            i = (Integer) o;
        return i;
    }

    public final int getCoins(final UUID uuid) {
        int i = 0;
        final Object o = handler.getData("players." + uuid + ".coins");
        if (o instanceof Integer)
            i = (Integer) o;
        return i;
    }

    public void updateScoreboard(final Player player) {
        new Thread(() -> {
            FastBoard scoreboard = scoreboards.getOrDefault(player.getUniqueId(), null);

            if (scoreboard == null) {
                scoreboard = new FastBoard(player);
                scoreboards.put(player.getUniqueId(), scoreboard);
            }

            scoreboard.updateTitle(handler.getConfig("scoreboard.title", true).toString());
            scoreboard.updateLines(handler.getConfigList("scoreboard.lines", true, player));
        }).start();
    }

    public String replaceStringWithData(final UUID uuid, final String input) {
        return input.replace("%kills%", String.valueOf(getKills(uuid)))
                .replace("%coins%", String.valueOf(getCoins(uuid)))
                .replace("%deaths%", String.valueOf(getDeaths(uuid)))
                .replace("%killStreaks%", String.valueOf(getKillStreaks(uuid)));
    }

    public ItemStack createItemStack(final Material material, final String name, final List<String> lore, int amount, boolean unbreakable) {
        ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            if (name != null)
                meta.setDisplayName(name);

            if (lore != null)
                meta.setLore(lore);

            item.setItemMeta(meta);
        }

        if (unbreakable) {
            final net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            final NBTTagCompound tag = nmsStack.getTag();
            tag.setBoolean("Unbreakable", true);
            nmsStack.setTag(tag);
            item = CraftItemStack.asBukkitCopy(nmsStack);
        }
        return item;
    }


    // Internal Functions

    private void quickLeave(final Player player) {
        handler.packetReader.uninject(player);

        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
        }

        final Object o = handler.getData("locations.lobby");
        if (o instanceof Location) player.teleport((Location) o);
    }

    private void sendSuitableDeathMessage(final Player victim, final Player attacker, final int coins, final EntityDamageEvent.DamageCause damageCause) {
        Object o;
        List<?> list;
        final Random random = new Random();
        if (attacker == null) {
            if (damageCause.equals(EntityDamageEvent.DamageCause.LAVA) || damageCause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || damageCause.equals(EntityDamageEvent.DamageCause.FIRE)) {
                if (Boolean.parseBoolean(handler.getConfig("deathMessages.fireDeath.self.chat.enabled", false).toString())) {
                    o = handler.getConfig("deathMessages.fireDeath.self.chat.messages", false);
                    if (o instanceof List) {
                        list = (List<?>) o;
                        victim.sendMessage(ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName())));
                    }
                }

                if (Boolean.parseBoolean(handler.getConfig("deathMessages.fireDeath.self.action-bar.enabled", false).toString())) {
                    o = handler.getConfig("deathMessages.fireDeath.self.action-bar.messages", false);
                    if (o instanceof List) {
                        list = (List<?>) o;
                        sendActionbar(victim, ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName())));
                    }
                }

                if (Boolean.parseBoolean(handler.getConfig("deathMessages.fireDeath.broadcast.enabled", false).toString())) {
                    o = handler.getConfig("deathMessages.fireDeath.broadcast.messages", false);
                    if (o instanceof List) {
                        list = (List<?>) o;
                        broadcast(ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName())));
                    }
                }

                if (Boolean.parseBoolean(handler.getConfig("deathMessages.fireDeath.self.title-screen.enabled", false).toString())) {
                    sendTitle(victim, handler.getConfig("deathMessages.fireDeath.self.title-screen.title", true).toString(), handler.getConfig("deathMessages.fireDeath.self.title-screen.subtitle", true).toString());
                }
                return;
            }

            if (Boolean.parseBoolean(handler.getConfig("deathMessages.randomDeath.self.chat.enabled", false).toString())) {
                o = handler.getConfig("deathMessages.randomDeath.self.chat.messages", false);
                if (o instanceof List) {
                    list = (List<?>) o;
                    victim.sendMessage(ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName())));
                }
            }

            if (Boolean.parseBoolean(handler.getConfig("deathMessages.randomDeath.self.action-bar.enabled", false).toString())) {
                o = handler.getConfig("deathMessages.randomDeath.self.action-bar.messages", false);
                if (o instanceof List) {
                    list = (List<?>) o;
                    sendActionbar(victim, ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName())));
                }
            }

            if (Boolean.parseBoolean(handler.getConfig("deathMessages.randomDeath.broadcast.enabled", false).toString())) {
                o = handler.getConfig("deathMessages.randomDeath.broadcast.messages", false);
                if (o instanceof List) {
                    list = (List<?>) o;
                    broadcast(ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName())));
                }
            }

            if (Boolean.parseBoolean(handler.getConfig("deathMessages.randomDeath.self.title-screen.enabled", false).toString())) {
                sendTitle(victim, handler.getConfig("deathMessages.randomDeath.self.title-screen.title", true).toString(), handler.getConfig("deathMessages.fireDeath.self.title-screen.subtitle", true).toString());
            }
            return;
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.chat.victim.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.chat.victim.message", true);
            if (o != null)
                victim.sendMessage(o.toString()
                        .replace("%coins%", String.valueOf(coins))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName()));
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.chat.attacker.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.chat.attacker.message", true);
            if (o != null)
                attacker.sendMessage(o.toString()
                        .replace("%coins%", String.valueOf(coins))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName()));
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.action-bar.victim.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.action-bar.victim.message", true);
            if (o != null)
                sendActionbar(victim, o.toString()
                        .replace("%coins%", String.valueOf(coins))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName()));
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.action-bar.attacker.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.action-bar.attacker.message", true);
            if (o != null)
                sendActionbar(attacker, o.toString()
                        .replace("%coins%", String.valueOf(coins))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName()));
        }
    }

}

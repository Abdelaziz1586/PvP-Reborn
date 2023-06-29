package me.pebbleprojects.pvpreborn.handlers;

import fr.mrmicky.fastboard.FastBoard;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public class PlayerDataHandler {

    public final LuckPerms api;
    private final Handler handler;
    public final ArrayList<Player> players;
    public final ArrayList<UUID> buildMode;
    public final HashMap<UUID, Player> lastDamage;
    private final HashMap<UUID, Integer> killStreaks;
    private final HashMap<UUID, FastBoard> scoreboards;

    public PlayerDataHandler(final Handler handler) {
        this.handler = handler;

        players = new ArrayList<>();
        lastDamage = new HashMap<>();
        scoreboards = new HashMap<>();
        killStreaks = new HashMap<>();
        buildMode = new ArrayList<>();

        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        if (provider != null) {
            api = provider.getProvider();
            return;
        }

        api = null;
        handler.main.getServer().getPluginManager().disablePlugin(handler.main);
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
        final Object o = handler.getData("game.location");
        if (!(o instanceof Location)) {
            if (player.isOp()) {
                player.sendMessage(handler.checkForPrefixAndReplace("%prefix% §cYou haven't set the game location yet."));
                return;
            }
            player.kickPlayer("§cGame is under setup, come back later!");
            return;
        }

        players.add(player);
        getPlayerReady(player, true);
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

    public void getPlayerReady(final Player player, final boolean firstJoin) {
        handler.runTask(() -> player.setGameMode(GameMode.SURVIVAL));

        final PlayerInventory playerInventory = player.getInventory();

        playerInventory.clear();
        playerInventory.setHelmet(null);
        playerInventory.setChestplate(null);
        playerInventory.setLeggings(null);
        playerInventory.setBoots(null);

        setKit(player);

        final Object o = handler.getData("game.location");

        if (o instanceof Location) {
            if (firstJoin) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.runTask(() -> player.teleport((Location) o));
                    }
                }, 50);
            } else {
                handler.runTask(() -> player.teleport((Location) o));
            }
        }

        player.setFireTicks(0);
        updateScoreboard(player);
        player.setDisplayName(handler.playerDataHandler.getDisplayName(player.getUniqueId(), player.getName()));
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

        final Object o = handler.getConfig("kill.points", false);
        int points = 20;
        if (o instanceof Integer)
            points = (Integer) o;

        if (attacker != null) {
            addKill(attacker.getUniqueId());
            addSouls(attacker.getUniqueId(), 1);
            addKillStreak(attacker.getUniqueId());
            addPoints(attacker.getUniqueId(), points);

            attacker.setHealth(attacker.getMaxHealth());

            attacker.setDisplayName(handler.playerDataHandler.getDisplayName(attacker.getUniqueId(), attacker.getName()));

            final List<ItemStack> contents = Arrays.asList(attacker.getInventory().getContents());

            for (int i = 0; i < contents.size(); i++) {
                if (contents.get(i).getType() == Material.FLINT_AND_STEEL) {
                    attacker.getInventory().setItem(i, createItemStack(Material.FLINT_AND_STEEL, "§7Flint and Steel", null, 1, false));
                    break;
                }
            }


            if (getKillStreak(attacker.getUniqueId()) % 5 == 0) {
                broadcast(handler.checkForPrefixAndReplace("%prefix% §e" + attacker.getDisplayName() + " §ahas a kill streak of §6" + getKillStreak(attacker.getUniqueId()) + "§a!"));
            }
        }
        sendSuitableDeathMessage(victim, attacker, points, cause);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (victim.isOnline()) getPlayerReady(victim, false);
            }
        }, 50);
    }

    private void addKill(final UUID uuid) {
        handler.writeData("players." + uuid + ".kills", getKills(uuid)+1);
    }

    public void addSouls(final UUID uuid, final int souls) {
        handler.writeData("players." + uuid + ".souls", getSouls(uuid)+souls);
    }

    public void addKillStreak(final UUID uuid) {
        int i = getKillStreak(uuid)+1;

        killStreaks.put(uuid, i);

        final Object o = handler.getData("players." + uuid + ".highestKillStreak");
        if (o instanceof Integer) {
            if (i > (Integer) o) handler.writeData("players." + uuid + ".highestKillStreak", i);
            return;
        }
        handler.writeData("players." + uuid + ".highestKillStreak", i);
    }

    public void addDeath(final UUID uuid) {
        handler.writeData("players." + uuid + ".deaths", getDeaths(uuid)+1);
    }

    public void addPoints(final UUID uuid, final int points) {
        handler.writeData("players." + uuid + ".points", getPoints(uuid)+points);
    }

    public void removeSouls(final UUID uuid, final int souls) {
        handler.writeData("players." + uuid + ".souls", getSouls(uuid)-souls);
    }

    public final int getKills(final UUID uuid) {
        final Object o = handler.getData("players." + uuid + ".kills");
        return o instanceof Integer ? (Integer) o : 0;
    }

    public final int getSouls(final UUID uuid) {
        final Object o = handler.getData("players." + uuid + ".souls");
        return o instanceof Integer ? (Integer) o : 0;
    }

    public final int getHighestKillStreak(final UUID uuid) {
        final Object o = handler.getData("players." + uuid + ".highestKillStreak");
        return o instanceof Integer ? (Integer) o : 0;
    }

    public final int getKillStreak(final UUID uuid) {
        return killStreaks.getOrDefault(uuid, 0);
    }

    public final int getDeaths(final UUID uuid) {
        final Object o = handler.getData("players." + uuid + ".deaths");
        return o instanceof Integer ? (Integer) o : 0;
    }

    public final int getPoints(final UUID uuid) {
        final Object o = handler.getData("players." + uuid + ".points");
        return o instanceof Integer ? (Integer) o : 500;
    }

    public final String getRank(final UUID uuid) {

        Object o = handler.getData("players." + uuid + ".scramble");

        if (o != null) return "§e#";

        o = handler.getData("players." + uuid + ".points");

        if (o instanceof Integer) {
            final int i = (Integer) o;

            if (i >= 17500) return "§5§lLEGENDARY";

            if (i >= 17000) return "§5Amethyst V";
            if (i >= 16500) return "§5Amethyst IV";
            if (i >= 16000) return "§5Amethyst III";
            if (i >= 15500) return "§5Amethyst II";
            if (i >= 15000) return "§5Amethyst I";

            if (i >= 14500) return "§4Ultimate V";
            if (i >= 14000) return "§4Ultimate IV";
            if (i >= 13500) return "§4Ultimate III";
            if (i >= 13000) return "§4Ultimate II";
            if (i >= 12500) return "§4Ultimate I";

            if (i >= 12000) return "§3Special V";
            if (i >= 11500) return "§3Special IV";
            if (i >= 11000) return "§3Special III";
            if (i >= 10500) return "§3Special II";
            if (i >= 10000) return "§3Special I";

            if (i >= 9500) return "§8Iron V";
            if (i >= 9000) return "§8Iron IV";
            if (i >= 8500) return "§8Iron III";
            if (i >= 8000) return "§8Iron II";
            if (i >= 7500) return "§8Iron I";

            if (i >= 7000) return "§8Silver V";
            if (i >= 6500) return "§8Silver IV";
            if (i >= 6000) return "§8Silver III";
            if (i >= 5500) return "§8Silver II";
            if (i >= 5000) return "§8Silver I";

            if (i >= 4500) return "§6Bronze V";
            if (i >= 3500) return "§6Bronze IV";
            if (i >= 2500) return "§6Bronze III";
            if (i >= 2000) return "§6Bronze II";
            if (i >= 1500) return "§6Bronze I";
        }
        return "§eUNRANKED";
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
                .replace("%points%", String.valueOf(getPoints(uuid)))
                .replace("%souls%", String.valueOf(getSouls(uuid)))
                .replace("%deaths%", String.valueOf(getDeaths(uuid)))
                .replace("%killStreak%", String.valueOf(getKillStreak(uuid)));
    }

    public ItemStack createItemStack(final Material material, final String name, final List<String> lore, final int amount, final boolean unbreakable) {
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

    public final String getDisplayName(final UUID uuid, final String name) {
        if (api != null) {

            final User user = api.getUserManager().getUser(uuid);
            if (user != null) {
                final Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
                final String prefix = group != null ? group.getCachedData().getMetaData().getPrefix() : null;

                return getRank(uuid) + " " + (prefix != null ? prefix + " " + name : name);
            }
        }
        return getRank(uuid) + " " + name;
    }


    // Internal Functions

    private void setKit(final Player player) {
        final PlayerInventory inventory = player.getInventory();

        int sword, rod, bow, flint;

        if (player.hasPermission("pvp.emerald")) {
            inventory.setHelmet(enchant(createItemStack(Material.DIAMOND_HELMET, "§7Helmet", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setChestplate(enchant(createItemStack(Material.IRON_CHESTPLATE, "§7ChestPlate", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setLeggings(enchant(createItemStack(Material.DIAMOND_LEGGINGS, "§7Leggings", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setBoots(enchant(createItemStack(Material.IRON_BOOTS, "§7Boots", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));

            Object o = handler.getData("players." + player.getUniqueId() + ".savedInventory.sword");
            sword = o instanceof Integer ? (Integer) o : 0;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.rod");
            rod = o instanceof Integer ? (Integer) o : 1;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.bow");
            bow = o instanceof Integer ? (Integer) o : 2;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.flint");
            flint = o instanceof Integer ? (Integer) o : 3;

            inventory.setItem(sword, enchant(createItemStack(Material.IRON_SWORD, "§7Iron Sword", null, 1, true), Enchantment.DAMAGE_ALL, 1));
            inventory.setItem(rod, enchant(createItemStack(Material.FISHING_ROD, "§7Fishing Rod", null, 1, true), Enchantment.DURABILITY, 3));
            inventory.setItem(bow, enchant(createItemStack(Material.BOW, "§7Bow", null, 1, true), Enchantment.ARROW_DAMAGE, 2));
            inventory.setItem(flint, createItemStack(Material.FLINT_AND_STEEL, "§7Flint and Steel", null, 1, false));
            return;
        }

        if (player.hasPermission("pvp.diamond")) {
            inventory.setHelmet(enchant(createItemStack(Material.IRON_HELMET, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setChestplate(enchant(createItemStack(Material.IRON_CHESTPLATE, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setLeggings(enchant(createItemStack(Material.DIAMOND_LEGGINGS, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setBoots(enchant(createItemStack(Material.IRON_BOOTS, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));

            Object o = handler.getData("players." + player.getUniqueId() + ".savedInventory.sword");
            sword = o instanceof Integer ? (Integer) o : 0;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.rod");
            rod = o instanceof Integer ? (Integer) o : 1;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.bow");
            bow = o instanceof Integer ? (Integer) o : 2;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.flint");
            flint = o instanceof Integer ? (Integer) o : 3;

            inventory.setItem(sword, enchant(createItemStack(Material.IRON_SWORD, "§7Iron Sword", null, 1, true), Enchantment.DAMAGE_ALL, 1));
            inventory.setItem(rod, enchant(createItemStack(Material.FISHING_ROD, "§7Fishing Rod", null, 1, true), Enchantment.DURABILITY, 3));
            inventory.setItem(bow, enchant(createItemStack(Material.BOW, "§7Bow", null, 1, true), Enchantment.ARROW_DAMAGE, 2));
            inventory.setItem(flint, createItemStack(Material.FLINT_AND_STEEL, "§7Flint and Steel", null, 1, false));
            return;
        }

        if (player.hasPermission("pvp.gold")) {
            inventory.setHelmet(enchant(createItemStack(Material.DIAMOND_HELMET, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 2));
            inventory.setChestplate(enchant(createItemStack(Material.IRON_CHESTPLATE, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 2));
            inventory.setLeggings(enchant(createItemStack(Material.IRON_LEGGINGS, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
            inventory.setBoots(enchant(createItemStack(Material.IRON_BOOTS, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));

            Object o = handler.getData("players." + player.getUniqueId() + ".savedInventory.sword");
            sword = o instanceof Integer ? (Integer) o : 0;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.rod");
            rod = o instanceof Integer ? (Integer) o : 1;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.bow");
            bow = o instanceof Integer ? (Integer) o : 2;

            o = handler.getData("players." + player.getUniqueId() + ".savedInventory.flint");
            flint = o instanceof Integer ? (Integer) o : 3;

            inventory.setItem(sword, enchant(createItemStack(Material.IRON_SWORD, "§7Iron Sword", null, 1, true), Enchantment.DAMAGE_ALL, 1));
            inventory.setItem(rod, enchant(createItemStack(Material.FISHING_ROD, "§7Fishing Rod", null, 1, true), Enchantment.DURABILITY, 3));
            inventory.setItem(bow, enchant(createItemStack(Material.BOW, "§7Bow", null, 1, true), Enchantment.ARROW_DAMAGE, 2));
            inventory.setItem(flint, createItemStack(Material.FLINT_AND_STEEL, "§7Flint and Steel", null, 1, false));
            return;
        }

        inventory.setHelmet(enchant(createItemStack(Material.IRON_HELMET, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
        inventory.setChestplate(enchant(createItemStack(Material.IRON_CHESTPLATE, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 2));
        inventory.setLeggings(enchant(createItemStack(Material.IRON_LEGGINGS, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));
        inventory.setBoots(enchant(createItemStack(Material.IRON_BOOTS, "§7", null, 1, true), Enchantment.PROTECTION_ENVIRONMENTAL, 1));

        Object o = handler.getData("players." + player.getUniqueId() + ".savedInventory.sword");
        sword = o instanceof Integer ? (Integer) o : 0;

        o = handler.getData("players." + player.getUniqueId() + ".savedInventory.rod");
        rod = o instanceof Integer ? (Integer) o : 1;

        o = handler.getData("players." + player.getUniqueId() + ".savedInventory.bow");
        bow = o instanceof Integer ? (Integer) o : 2;

        o = handler.getData("players." + player.getUniqueId() + ".savedInventory.flint");
        flint = o instanceof Integer ? (Integer) o : 3;

        inventory.setItem(sword, enchant(createItemStack(Material.STONE_SWORD, "§7Stone Sword", null, 1, true), Enchantment.DAMAGE_ALL, 1));
        inventory.setItem(rod, enchant(createItemStack(Material.FISHING_ROD, "§7Fishing Rod", null, 1, true), Enchantment.DURABILITY, 3));
        inventory.setItem(bow, enchant(createItemStack(Material.BOW, "§7Bow", null, 1, true), Enchantment.ARROW_DAMAGE, 1));
        inventory.setItem(flint, createItemStack(Material.FLINT_AND_STEEL, "§7Flint and Steel", null, 1, false));
    }

    private ItemStack enchant(final ItemStack item, final Enchantment enchantment, final int level) {
        item.addEnchantment(enchantment, level);
        return item;
    }

    private void quickLeave(final Player player) {
        handler.packetReader.uninject(player);

        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
        }

        player.kickPlayer("Restarting...");
    }

    private void sendSuitableDeathMessage(final Player victim, final Player attacker, final int points, final EntityDamageEvent.DamageCause damageCause) {
        Object o;
        List<?> list;
        final Random random = new Random();
        if (attacker == null) {
            if (damageCause.equals(EntityDamageEvent.DamageCause.LAVA) || damageCause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || damageCause.equals(EntityDamageEvent.DamageCause.FIRE)) {
                if (Boolean.parseBoolean(handler.getConfig("deathMessages.fireDeath.self.chat.enabled", false).toString())) {
                    o = handler.getConfig("deathMessages.fireDeath.self.chat.messages", false);
                    if (o instanceof List) {
                        list = (List<?>) o;
                        victim.sendMessage(handler.checkForPrefixAndReplace(ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName()))));
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
                    victim.sendMessage(handler.checkForPrefixAndReplace(ChatColor.translateAlternateColorCodes('&', list.get(random.nextInt(list.size() - 1)).toString().replace("%victim%", victim.getDisplayName()))));
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
                victim.sendMessage(handler.checkForPrefixAndReplace(o.toString()
                        .replace("%points%", String.valueOf(points))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName())));
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.chat.attacker.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.chat.attacker.message", true);
            if (o != null)
                attacker.sendMessage(handler.checkForPrefixAndReplace(o.toString()
                        .replace("%points%", String.valueOf(points))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName())));
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.action-bar.victim.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.action-bar.victim.message", true);
            if (o != null)
                sendActionbar(victim, o.toString()
                        .replace("%points%", String.valueOf(points))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName()));
        }

        if (Boolean.parseBoolean(handler.getConfig("kill.messages.action-bar.attacker.enabled", false).toString())) {
            o = handler.getConfig("kill.messages.action-bar.attacker.message", true);
            if (o != null)
                sendActionbar(attacker, o.toString()
                        .replace("%points%", String.valueOf(points))
                        .replace("%attacker%", attacker.getDisplayName())
                        .replace("%victim%", victim.getDisplayName()));
        }
    }

}

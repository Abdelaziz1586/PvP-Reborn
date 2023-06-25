package me.pebbleprojects.pvpreborn.npc.customEvent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.pebbleprojects.pvpreborn.handlers.Handler;
import me.pebbleprojects.pvpreborn.npc.NPC;
import me.pebbleprojects.pvpreborn.npc.NPCHandler;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PacketReader {

    private Channel channel;
    private final Handler handler;
    private final NPCHandler npcHandler;
    private final HashMap<UUID, Channel> channels;

    public PacketReader(final Handler handler) {
        this.handler = handler;
        channels = new HashMap<>();
        npcHandler = handler.npcHandler;
    }

    public void inject(final Player player) {
        final CraftPlayer craftPlayer = (CraftPlayer) player;
        channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        channels.put(player.getUniqueId(), channel);

        if (channel.pipeline().get("PacketInjector") != null) {
            return;
        }

        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<PacketPlayInUseEntity>() {
            @Override
            protected void decode(final ChannelHandlerContext channel, final PacketPlayInUseEntity packet, final List<Object> arg) {
                arg.add(packet);
                readPacket(player, packet);
            }
        });
    }

    public void uninject(final Player player) {
        channel = channels.get(player.getUniqueId());
        if (channel.pipeline().get("PacketInjector") != null) {
            channel.pipeline().remove("PacketInjector");
            channels.remove(player.getUniqueId());
        }
    }

    private void readPacket(final Player player, final PacketPlayInUseEntity packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {

            final String s = packet.a().name();
            if (s.equals("ATTACK"))
                return;
            if (s.equals("INTERACT_AT"))
                return;

            int id = (int) getEntityID(packet);
            if (s.equalsIgnoreCase("INTERACT")) {
                for (final NPC npc : npcHandler.getNPCs()) {
                    if (npc.getNpc().getId() == id) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(handler.main, () -> Bukkit.getPluginManager().callEvent(new RightClickNPCEvent(player, npc)));
                        return;
                    }
                }
            }
        }
    }

    private Object getEntityID(final Object instance) {
        Object o = null;
        try {
            final Field field = instance.getClass().getDeclaredField("a");
            field.setAccessible(true);
            o = field.get(instance);
            field.setAccessible(false);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return o;
    }

}


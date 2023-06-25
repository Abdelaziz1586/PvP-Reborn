package me.pebbleprojects.pvpreborn.npc;

import com.mojang.authlib.properties.Property;
import me.pebbleprojects.pvpreborn.handlers.Handler;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class NPCHandler {

    private boolean isReloading;
    private final Handler handler;
    private final HashMap<Integer, NPC> NPCs;

    public NPCHandler(final Handler handler) {
        this.handler = handler;
        NPCs = new HashMap<>();
        isReloading = false;
        loadNPCs();
    }

    public void loadNPCs(final Player player) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (final NPC npc : NPCs.values()) {
                    npc.sendRemovePacket(player);
                    npc.sendShowPacket(player);
                }
            }
        }, 500);
    }

    public void loadNPCs() {
        new Thread(() -> {
            if (isReloading)
                return;
            isReloading = true;
            NPC npc;
            NPCs.clear();
            final Set<String> NPCs = handler.getDataSection("NPCs");
            if (NPCs == null) return;
            for (final String i : NPCs) {
                try {
                    npc = new NPC(handler.getData("NPCs." + i + ".name").toString(), (Location) handler.getData("NPCs." + i + ".location"), Integer.parseInt(handler.getData("NPCs." + i + ".customData").toString()), this);
                    final Object o = handler.getData("NPCs." + i + ".text");
                    if (o != null)
                        npc.getNpc().getProfile().getProperties().put("textures", new Property("textures", o.toString(), handler.getData("NPCs." + i + ".signature").toString()));
                    this.NPCs.put(Integer.parseInt(i), npc);
                    handler.playerDataHandler.players.forEach(npc::sendShowPacket);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            isReloading = false;
        }).start();
    }

    public void unloadNPCs() {
        NPCs.values().forEach(npc -> handler.playerDataHandler.players.forEach(npc::sendRemovePacket));
        NPCs.clear();
    }

    public void unloadNPCs(final Player player) {
        handler.runTask(() -> NPCs.values().forEach(npc -> npc.sendRemovePacket(player)));
    }

    public void deleteNPC(final int id) {
        final NPC npc = getNPC(id);
        if (npc != null) {
            handler.playerDataHandler.players.forEach(npc::sendRemovePacket);
            NPCs.remove(id);
        }
    }

    public void save(final NPC npc) {
        int i = 1;
        final Set<String> NPCs = handler.getDataSection("NPCs");
        if (NPCs != null)
            i = NPCs.size() + 1;
        handler.writeData("NPCs." + i + ".customData", npc.getCustomData());
        handler.writeData("NPCs." + i + ".location", npc.getLocation());
        handler.writeData("NPCs." + i + ".name", npc.getName());

        String s = npc.getTexture();
        if (s != null)
            handler.writeData("NPCs." + i + ".text", s);
        s = npc.getSignature();
        if (s != null)
            handler.writeData("NPCs." + i + ".signature", s);
        this.NPCs.put(i, npc);
    }

    public final NPC createNPC(final String name, final Location location, final int customData) {
        return new NPC(name, location, customData, this);
    }

    public final int getCustomData(final EntityPlayer entityPlayer) {
        for (final NPC npc : NPCs.values()) {
            if (entityPlayer.equals(npc.getNpc())) return npc.getCustomData();
        }
        return -1;
    }

    public final NPC getNPC(final int id) {
        if (NPCs.containsKey(id))
            return NPCs.get(id);
        return null;
    }

    public final Collection<NPC> getNPCs() {
        return NPCs.values();
    }
}

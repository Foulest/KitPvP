package net.foulest.kitpvp.utils;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project Togepi
 */
public final class NMSUtil {

    private NMSUtil() {
    }

    public static EntityPlayer getNmsPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static PlayerConnection getConnection(Player player) {
        return NMSUtil.getNmsPlayer(player).playerConnection;
    }
}

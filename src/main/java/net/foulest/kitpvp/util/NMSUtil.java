package net.foulest.kitpvp.util;

import lombok.NonNull;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 */
public final class NMSUtil {

    public static EntityPlayer getNmsPlayer(@NonNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static PlayerConnection getConnection(@NonNull Player player) {
        return getNmsPlayer(player).playerConnection;
    }
}

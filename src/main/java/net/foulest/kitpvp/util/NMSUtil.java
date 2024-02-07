package net.foulest.kitpvp.util;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Utility class for accessing NMS ({@link net.minecraft.server}) components.
 *
 * @author Foulest
 * @project KitPvP
 */
public final class NMSUtil {

    /**
     * Gets the NMS ({@link net.minecraft.server}) EntityPlayer for a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The NMS EntityPlayer.
     */
    public static EntityPlayer getNmsPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Gets the PlayerConnection for a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The PlayerConnection.
     */
    public static PlayerConnection getConnection(Player player) {
        return getNmsPlayer(player).playerConnection;
    }
}

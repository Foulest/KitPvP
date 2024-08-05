/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NMSUtil {

    /**
     * Gets the NMS ({@link net.minecraft.server}) EntityPlayer for a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The NMS EntityPlayer.
     */
    private static EntityPlayer getNmsPlayer(Player player) {
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

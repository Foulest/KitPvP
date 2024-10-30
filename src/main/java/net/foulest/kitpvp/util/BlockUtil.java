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

import lombok.Data;
import net.foulest.kitpvp.util.data.ConcurrentStream;
import net.foulest.kitpvp.util.raytrace.BoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility class for block-related methods.
 *
 * @author Foulest
 */
@Data
public class BlockUtil {

    /**
     * Checks if a player is in an unloaded chunk.
     *
     * @param player The player to check.
     * @return Whether the player is in an unloaded chunk.
     */
    private static boolean isPlayerInUnloadedChunk(@NotNull Player player) {
        Location location = player.getLocation();
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();
        return !location.getWorld().isChunkLoaded(blockX >> 4, blockZ >> 4);
    }

    /**
     * Gets the blocks that a player is colliding with.
     *
     * @param player The player to check.
     * @param boundingBox The bounding box to check.
     * @return The blocks that the player is colliding with.
     */
    @Contract("_, _ -> new")
    private static @NotNull ConcurrentStream<Block> getCollidingBlocks(Player player, @NotNull BoundingBox boundingBox) {
        List<Block> collidingBlocks = boundingBox.getCollidingBlocks(player);
        return new ConcurrentStream<>(collidingBlocks, false);
    }

    /**
     * Checks if a player is colliding with a solid block.
     *
     * @param player The player to check.
     * @return Whether the player is colliding with a solid block.
     */
    private static boolean collidesWithSolid(Player player, BoundingBox boundingBox) {
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> {
            Material type = block.getType();

            return type.isSolid()
                    || type == Material.WATER_LILY
                    || type == Material.FLOWER_POT
                    || type == Material.CARPET
                    || type == Material.SNOW
                    || type == Material.SKULL;
        });
    }

    /**
     * Checks if a player is on the ground.
     *
     * @param player The player to check.
     * @param offset The offset to check.
     * @return Whether the player is on the ground.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isOnGroundOffset(Player player, double offset) {
        if (isPlayerInUnloadedChunk(player)) {
            return true;
        }

        BoundingBox boundingBox = new BoundingBox(player).expand(0.0, 0.0, 0.0)
                .expandMin(0.0, offset, 0.0)
                .expandMax(0.0, -1.0, 0.0);
        return collidesWithSolid(player, boundingBox);
    }
}

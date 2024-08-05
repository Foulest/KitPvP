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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.foulest.kitpvp.util.data.ConcurrentStream;
import net.foulest.kitpvp.util.raytrace.BoundingBox;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockUtil {

    private static boolean isPlayerInUnloadedChunk(@NotNull Player player) {
        return !player.getLocation().getWorld().isChunkLoaded(player.getLocation().getBlockX() >> 4,
                player.getLocation().getBlockZ() >> 4);
    }

    @Contract("_, _ -> new")
    private static @NotNull ConcurrentStream<Block> getCollidingBlocks(Player player, @NotNull BoundingBox boundingBox) {
        return new ConcurrentStream<>(boundingBox.getCollidingBlocks(player), false);
    }

    private static boolean collidesWithSolid(Player player, BoundingBox boundingBox) {
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType().isSolid()
                || block.getType() == Material.WATER_LILY
                || block.getType() == Material.FLOWER_POT
                || block.getType() == Material.CARPET
                || block.getType() == Material.SNOW
                || block.getType() == Material.SKULL);
    }

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

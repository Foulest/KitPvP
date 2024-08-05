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
package net.foulest.kitpvp.util.raytrace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * BoundingBox class, taken from SpigotMC.
 * This class is impossible to use without NMS.
 * <p>
 * <a href="https://www.spigotmc.org/threads/hitboxes-and-ray-tracing.174358/">...</a>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@SuppressWarnings("unused")
public class BoundingBox {

    private Vector min;
    private Vector max;

    // Gets the min and max point of a block.
    private BoundingBox(@NotNull Block block) {
        IBlockData blockData = ((CraftWorld) block.getWorld()).getHandle().getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        net.minecraft.server.v1_8_R3.Block blockNative = blockData.getBlock();

        blockNative.updateShape(((CraftWorld) block.getWorld()).getHandle(), new BlockPosition(block.getX(), block.getY(), block.getZ()));

        min = new Vector(block.getX() + blockNative.B(), block.getY() + blockNative.D(), block.getZ() + blockNative.F());
        max = new Vector(block.getX() + blockNative.C(), block.getY() + blockNative.E(), block.getZ() + blockNative.G());
    }

    // Gets the min and max point of an entity.
    public BoundingBox(Entity entity) {
        AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox();
        min = new Vector(box.a, box.b, box.c);
        max = new Vector(box.d, box.e, box.f);
    }

    // Gets the min and max point of an AxisAlignedBB.
    public BoundingBox(@NotNull AxisAlignedBB box) {
        min = new Vector(box.a, box.b, box.c);
        max = new Vector(box.d, box.e, box.f);
    }

    // Gets the min and max point of a custom BoundingBox.
    private BoundingBox(double minX, double minY, double minZ,
                        double maxX, double maxY, double maxZ) {
        min = new Vector(minX, minY, minZ);
        max = new Vector(maxX, maxY, maxZ);
    }

    // Gets the mid-point of the bounding box.
    public Vector midPoint() {
        return max.clone().add(min).multiply(0.5);
    }

    private boolean collidesWith(@NotNull BoundingBox other) {
        return (min.getX() <= other.max.getX() && max.getX() >= other.min.getX())
                && (min.getY() <= other.max.getY() && max.getY() >= other.min.getY())
                && (min.getZ() <= other.max.getZ() && max.getZ() >= other.min.getZ());
    }

    public BoundingBox expand(double value) {
        double minX = min.getX() - value;
        double minY = min.getY() - value;
        double minZ = min.getZ() - value;
        double maxX = max.getX() + value;
        double maxY = max.getY() + value;
        double maxZ = max.getZ() + value;
        return new BoundingBox(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }

    public BoundingBox expand(double x, double y, double z) {
        double minX = min.getX() - x;
        double minY = min.getY() - y;
        double minZ = min.getZ() - z;
        double maxX = max.getX() + x;
        double maxY = max.getY() + y;
        double maxZ = max.getZ() + z;
        return new BoundingBox(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }

    public BoundingBox expandMin(double x, double y, double z) {
        double minX = min.getX() - x;
        double minY = min.getY() - y;
        double minZ = min.getZ() - z;
        return new BoundingBox(new Vector(minX, minY, minZ), max);
    }

    public BoundingBox expandMax(double x, double y, double z) {
        double maxX = max.getX() + x;
        double maxY = max.getY() + y;
        double maxZ = max.getZ() + z;
        return new BoundingBox(min, new Vector(maxX, maxY, maxZ));
    }

    public List<Block> getCollidingBlocks(Player player) {
        List<Block> blocks = new ArrayList<>();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Location loc = new Location(player.getWorld(), x, y, z);

                    if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        BoundingBox boundingBox = new BoundingBox(loc.getBlock());

                        if (boundingBox.collidesWith(this)) {
                            blocks.add(loc.getBlock());
                        }
                    }
                }
            }
        }
        return blocks;
    }

    @Contract("_ -> new")
    public static @NotNull BoundingBox getEntityBoundingBox(@NotNull Location location) {
        double f = 0.6 / 2.0;
        double f1 = 1.8;
        return (new BoundingBox(location.getX() - f, location.getY(), location.getZ() - f,
                location.getX() + f, location.getY() + f1, location.getZ() + f));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull BoundingBox getEntityBoundingBox(double x, double y, double z) {
        double f = 0.6 / 2.0;
        double f1 = 1.8;
        return (new BoundingBox(x - f, y, z - f, x + f, y + f1, z + f));
    }
}

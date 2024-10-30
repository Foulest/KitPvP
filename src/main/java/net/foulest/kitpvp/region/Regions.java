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
package net.foulest.kitpvp.region;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Data;
import net.foulest.kitpvp.util.MessageUtil;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Utility class for handling WorldGuard regions.
 *
 * @author Foulest
 */
@Data
public class Regions {

    private static final WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
    private static Map<String, ProtectedRegion> regionMap = new HashMap<>();

    /**
     * Caches WorldGuard regions for later use.
     */
    static void cacheRegions() {
        World world = Spawn.getLocation().getWorld();
        RegionContainer container = worldGuard.getRegionContainer();
        RegionManager regionManager = container.get(world);

        if (regionManager == null) {
            MessageUtil.log(Level.WARNING, "ERROR: No regions found.");
            return;
        }

        regionMap = regionManager.getRegions();
    }

    /**
     * Checks if a location is inside a safe zone (as defined by WorldGuard regions).
     *
     * @param loc The location to check.
     * @return True if the location is in a safe zone, false otherwise.
     */
    public static boolean isInSafezone(Location loc) {
        for (Map.Entry<String, ProtectedRegion> regions : regionMap.entrySet()) {
            ProtectedRegion region = regions.getValue();
            BlockVector regionMin = region.getMinimumPoint();
            BlockVector regionMax = region.getMaximumPoint();

            double minX = regionMin.getX();
            double minY = regionMin.getY();
            double minZ = regionMin.getZ();

            double maxX = regionMax.getX();
            double maxY = regionMax.getY();
            double maxZ = regionMax.getZ();

            AxisAlignedBB regionZone = new AxisAlignedBB(
                    new BlockPosition(minX, minY, minZ),
                    new BlockPosition((maxX + 1), maxY, (maxZ + 1))
            );

            double locX = loc.getX();
            double locY = loc.getY();
            double locZ = loc.getZ();

            Vec3D vec3D = new Vec3D(locX, locY, locZ);

            if (regionZone.a(vec3D) && region.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY) {
                return true;
            }
        }
        return false;
    }
}

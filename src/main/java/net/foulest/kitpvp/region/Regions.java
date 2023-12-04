package net.foulest.kitpvp.region;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.NonNull;
import net.foulest.kitpvp.util.MessageUtil;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Handles regions
 */
public class Regions {

    private static final WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
    private static Map<String, ProtectedRegion> regionMap = new HashMap<>();

    public static void cacheRegions() {
        RegionContainer container = worldGuard.getRegionContainer();
        RegionManager regionManager = container.get(Spawn.getLocation().getWorld());

        if (regionManager == null) {
            MessageUtil.log(Level.WARNING, "ERROR: No regions found.");
            return;
        }

        regionMap = regionManager.getRegions();
    }

    public static boolean isInSafezone(@NonNull Location loc) {
        for (Map.Entry<String, ProtectedRegion> regions : regionMap.entrySet()) {
            ProtectedRegion region = regions.getValue();
            BlockVector regionMin = region.getMinimumPoint();
            BlockVector regionMax = region.getMaximumPoint();

            AxisAlignedBB regionZone = new AxisAlignedBB(new BlockPosition(region.getMinimumPoint().getX(), regionMin.getY(), regionMin.getZ()),
                    new BlockPosition((regionMax.getX() + 1), regionMax.getY(), (regionMax.getZ() + 1)));
            Vec3D vec3D = new Vec3D(loc.getX(), loc.getY(), loc.getZ());

            if (regionZone.a(vec3D) && region.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY) {
                return true;
            }
        }
        return false;
    }
}

package net.foulest.kitpvp.utils;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Regions {

    private static final Regions INSTANCE = new Regions();
    private final WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
    private final Spawn spawn = Spawn.getInstance();
    private Map<String, ProtectedRegion> regionMap = new HashMap<>();

    public static Regions getInstance() {
        return INSTANCE;
    }

    public void cacheRegions() {
        RegionContainer container = worldGuard.getRegionContainer();
        RegionManager regionManager = container.get(spawn.getLocation().getWorld());

        if (regionManager == null) {
            MessageUtil.log("&c[KitPvP] ERROR: No regions found.");
            return;
        }

        regionMap = regionManager.getRegions();
    }

    public Map<String, ProtectedRegion> getRegions() {
        return regionMap;
    }

    public boolean isInSafezone(Player player) {
        for (Map.Entry<String, ProtectedRegion> regions : Regions.getInstance().getRegions().entrySet()) {
            ProtectedRegion region = regions.getValue();
            BlockVector regionMin = region.getMinimumPoint();
            BlockVector regionMax = region.getMaximumPoint();
            AxisAlignedBB regionZone = new AxisAlignedBB(new BlockPosition(regionMin.getX(), regionMin.getY(), regionMin.getZ()),
                    new BlockPosition(regionMax.getX(), regionMax.getY(), regionMax.getZ()));
            Vec3D vec3D = new Vec3D(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

            if (regionZone.a(vec3D) && region.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY) {
                return true;
            }
        }

        return false;
    }
}

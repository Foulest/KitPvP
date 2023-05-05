package net.foulest.kitpvp.region;

import lombok.Getter;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.logging.Level;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Handles the spawn point
 */
public class Spawn {

    @Getter
    private static Location location;

    public static void setLocation(Location loc) {
        Settings.spawnX = loc.getX();
        Settings.spawnY = loc.getY();
        Settings.spawnZ = loc.getZ();
        Settings.spawnYaw = loc.getYaw();
        Settings.spawnPitch = loc.getPitch();
        Settings.spawnWorld = loc.getWorld().getName();

        location = loc;
        loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Teleports a player to spawn.
     *
     * @param player The player to teleport.
     */
    public static void teleport(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);

        if (location == null) {
            MessageUtil.messagePlayer(player, "&cThe spawn point is not set. Please contact an administrator.");
            return;
        }

        playerData.clearCooldowns();
        CombatLog.remove(player);
        playerData.setKit(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        playerData.giveDefaultItems();

        player.setHealth(20);
        player.teleport(location);
    }

    /**
     * Loads the spawn point data from config files.
     */
    public static void load() {
        if (Settings.config.get("spawn") == null) {
            MessageUtil.log(Level.WARNING, "Spawn is not defined. Define it using /setspawn.");
            return;
        }

        location = new Location(Bukkit.getWorld(Settings.spawnWorld), Settings.spawnX,
                Settings.spawnY, Settings.spawnZ, Settings.spawnYaw, Settings.spawnPitch);

        Regions.cacheRegions();
    }
}

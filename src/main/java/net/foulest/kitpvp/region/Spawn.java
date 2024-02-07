package net.foulest.kitpvp.region;

import lombok.Getter;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Handles the spawn point.
 *
 * @author Foulest
 * @project KitPvP
 */
public class Spawn {

    @Getter
    public static Location location;

    /**
     * Sets the spawn point.
     *
     * @param loc The location to set the spawn point to.
     */
    public static void setLocation(@NotNull Location loc) {
        Settings.spawnX = loc.getX();
        Settings.spawnY = loc.getY();
        Settings.spawnZ = loc.getZ();
        Settings.spawnYaw = loc.getYaw();
        Settings.spawnPitch = loc.getPitch();
        Settings.spawnWorld = loc.getWorld().getName();
        Settings.saveConfig();

        location = loc;
        loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Teleports a player to spawn.
     *
     * @param player The player to teleport.
     */
    public static void teleport(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (location == null) {
            MessageUtil.messagePlayer(player, "&cThe spawn point is not set. Please contact an administrator.");
            return;
        }

        playerData.clearCooldowns();
        CombatTag.remove(player);
        playerData.setActiveKit(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        playerData.giveDefaultItems();

        player.setHealth(20);
        player.teleport(location);
    }

    /**
     * Loads the spawn point data.
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

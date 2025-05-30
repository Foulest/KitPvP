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

import lombok.Data;
import lombok.Getter;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the spawn point.
 *
 * @author Foulest
 */
@Data
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

        int blockX = loc.getBlockX();
        int blockY = loc.getBlockY();
        int blockZ = loc.getBlockZ();

        loc.getWorld().setSpawnLocation(blockX, blockY, blockZ);
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
            PotionEffectType effectType = effect.getType();
            player.removePotionEffect(effectType);
        }

        playerData.giveDefaultItems();

        player.setMaxHealth(20);
        player.setHealth(20);
        player.teleport(location);

        // Update the player's kit change count.
        int changeCount = playerData.getChangeCount();
        playerData.setChangeCount(changeCount + 1);
    }

    /**
     * Loads the spawn point data.
     */
    public static void load() {
        location = new Location(Bukkit.getWorld(Settings.spawnWorld), Settings.spawnX,
                Settings.spawnY, Settings.spawnZ, Settings.spawnYaw, Settings.spawnPitch);

        Regions.cacheRegions();
    }
}

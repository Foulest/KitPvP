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
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.Ninja;
import net.foulest.kitpvp.region.Regions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class AbilityUtil {

    /**
     * Gets the nearby players within a certain range.
     *
     * @param player The player.
     * @param x The x range.
     * @param y The y range.
     * @param z The z range.
     * @return The nearby players.
     */
    public static @NotNull Collection<Player> getNearbyPlayers(@NotNull Player player, double x, double y, double z) {
        Collection<Player> nearbyPlayers = new ArrayList<>();

        for (Entity entity : player.getNearbyEntities(x, y, z)) {
            // Ignores the entity if it's not a player.
            if (!(entity instanceof Player)) {
                continue;
            }

            Player target = (Player) entity;
            PlayerData targetData = PlayerDataManager.getPlayerData(target);
            Location targetLoc = target.getLocation();

            // Ignores ineligible players.
            if (targetData.getActiveKit() == null
                    || Regions.isInSafezone(targetLoc)
                    || (target.hasPotionEffect(PotionEffectType.INVISIBILITY)
                    && targetData.getActiveKit() instanceof Ninja)) {
                continue;
            }

            nearbyPlayers.add(target);
        }
        return nearbyPlayers;
    }

    /**
     * Checks for exclusions before executing an ability.
     *
     * @param playerLoc The player's location.
     * @param player The player.
     * @param playerData The player's data.
     * @return Whether the ability should be ignored.
     */
    public static boolean shouldBeExcluded(Location playerLoc, Player player,
                                           @NotNull PlayerData playerData, @NotNull Kit desiredKit) {
        // Ignores the event if the player is not using the desired kit.
        if (playerData.getActiveKit().getClass() != desiredKit.getClass()) {
            return true;
        }

        // Ignores the event if the player is in a safe zone.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return true;
        }

        // Ignores the event if the player's ability is on cooldown.
        return playerData.hasCooldown(true);
    }

    /**
     * Checks for exclusions before executing an ability.
     *
     * @param playerLoc The player's location.
     * @param player The player.
     * @param playerData The player's data.
     * @return Whether the ability should be ignored.
     */
    public static boolean shouldBeExcluded(Location playerLoc, Player player,
                                           @NotNull PlayerData playerData, @NotNull Kit desiredKit,
                                           ItemStack itemStack, @NotNull Material itemType) {
        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != itemType) {
            return true;
        }

        // Ignores the event if the player is not using the desired kit.
        if (playerData.getActiveKit().getClass() != desiredKit.getClass()) {
            return true;
        }

        // Ignores the event if the player is in a safe zone.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return true;
        }

        // Ignores the event if the player's ability is on cooldown.
        return playerData.hasCooldown(true);
    }
}

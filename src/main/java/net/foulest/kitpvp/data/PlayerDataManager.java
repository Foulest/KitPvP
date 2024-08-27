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
package net.foulest.kitpvp.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for managing player data.
 *
 * @author Foulest
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerDataManager {

    /**
     * Map of player UUIDs to their stored data.
     */
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * Gets a player's data from the map.
     *
     * @param player The player to get.
     * @return The player's data.
     */
    public static PlayerData getPlayerData(@NotNull Player player) {
        if (playerDataMap.containsKey(player.getUniqueId())) {
            return playerDataMap.get(player.getUniqueId());
        } else {
            addPlayerData(player);
        }
        return playerDataMap.get(player.getUniqueId());
    }

    /**
     * Adds a player's data to the map.
     *
     * @param player The player to add.
     */
    private static void addPlayerData(@NotNull Player player) {
        if (!playerDataMap.containsKey(player.getUniqueId())) {
            PlayerData data = new PlayerData(player.getUniqueId(), player);
            playerDataMap.put(player.getUniqueId(), data);
        }
    }

    /**
     * Removes a player's data from the map.
     *
     * @param player The player to remove.
     */
    public static void removePlayerData(@NotNull Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    /**
     * Checks if a player has data stored.
     *
     * @param player The player to check.
     * @return True if the player has data stored, otherwise false.
     */
    public static boolean hasPlayerData(@NotNull Player player) {
        return playerDataMap.containsKey(player.getUniqueId());
    }
}

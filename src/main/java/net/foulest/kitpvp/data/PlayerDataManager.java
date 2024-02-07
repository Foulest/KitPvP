package net.foulest.kitpvp.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    // Map of player UUIDs to their stored data.
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
    public static void addPlayerData(@NotNull Player player) {
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
}

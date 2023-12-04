package net.foulest.kitpvp.data;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static PlayerData getPlayerData(Player player) {
        if (playerDataMap.containsKey(player.getUniqueId())) {
            return playerDataMap.get(player.getUniqueId());
        } else {
            addPlayerData(player);
        }
        return playerDataMap.get(player.getUniqueId());
    }

    public static void addPlayerData(Player player) {
        if (!playerDataMap.containsKey(player.getUniqueId())) {
            PlayerData data = new PlayerData(player.getUniqueId(), player);
            playerDataMap.put(player.getUniqueId(), data);
        }
    }

    public static void removePlayerData(Player player) {
        // TODO: Is this necessary?
        PlayerData playerData = getPlayerData(player);
        playerData.getOwnedKits().clear();
        playerData.setKit(null);
        playerData.setBenefactor(null);

        playerDataMap.remove(player.getUniqueId());
    }
}

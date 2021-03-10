package net.foulest.kitpvp.utils.kits;

import lombok.Getter;
import net.foulest.kitpvp.utils.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
@Getter
public class KitManager {

    private static final KitManager INSTANCE = new KitManager();
    private final List<Kit> kits = new ArrayList<>();

    public static KitManager getInstance() {
        return INSTANCE;
    }

    public void registerKit(Kit kit) {
        kits.add(kit);
    }

    public void unloadKits() {
        kits.clear();
    }

    public Kit valueOf(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }

        return null;
    }

    public boolean hasRequiredKit(Player player, String required) {
        PlayerData playerData = PlayerData.getInstance(player);
        return playerData.getKit() != null && valueOf(required) != null
                && playerData.getKit().getName().equals(valueOf(required).getName());
    }
}

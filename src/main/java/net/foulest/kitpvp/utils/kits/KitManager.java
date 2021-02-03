package net.foulest.kitpvp.utils.kits;

import net.foulest.kitpvp.utils.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KitManager {

    private static final KitManager instance = new KitManager();
    private final List<Kit> kits = new ArrayList<>();

    public static KitManager getInstance() {
        return instance;
    }

    public void registerKit(Kit kit) {
        kits.add(kit);
    }

    public void unloadKits() {
        kits.clear();
    }

    public List<Kit> getKits() {
        return kits;
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
        return playerData.hasKit() && valueOf(required) != null && playerData.getKit().getName().equals(valueOf(required).getName());
    }
}

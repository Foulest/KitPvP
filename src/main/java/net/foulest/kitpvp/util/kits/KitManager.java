package net.foulest.kitpvp.util.kits;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Foulest
 * @project KitPvP
 */
public class KitManager {

    public static final List<Kit> kits = new ArrayList<>();

    public static Kit getKit(@NonNull String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }
}

package net.foulest.kitpvp.util.kits;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Foulest
 * @project KitPvP
 */
public class KitManager {

    public static final List<Kit> kits = new ArrayList<>();

    public static Kit getKit(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }

        return null;
    }
}

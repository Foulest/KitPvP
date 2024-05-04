package net.foulest.kitpvp.kits;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KitManager {

    public static final List<Kit> kits = new ArrayList<>();

    public static @Nullable Kit getKit(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }
}

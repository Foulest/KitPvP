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
package net.foulest.kitpvp.kits;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all the loaded kits.
 *
 * @author Foulest
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KitManager {

    /**
     * The list of loaded kits.
     */
    @Getter
    private static final List<Kit> kits = new ArrayList<>();

    /**
     * Gets a kit by its name.
     *
     * @param name The name of the kit.
     * @return The kit with the given name, or null if no kit with that name exists.
     */
    public static @Nullable Kit getKit(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }
}

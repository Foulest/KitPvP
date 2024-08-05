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
package net.foulest.kitpvp.enchants;

import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Enchants {
    FEATHER_FALLING,
    THORNS,
    PROTECTION,
    KNOCKBACK,
    SHARPNESS,
    PUNCH,
    POWER;

    /**
     * Gets the name of the enchantment in a MySQL-friendly format.
     * Example: "FEATHER_FALLING" -> "featherFalling"
     *
     * @return The formatted name.
     */
    public @NotNull String getDatabaseName() {
        String processedName = MessageUtil.capitalize(name().toLowerCase(Locale.ROOT)
                        .replace("_", " "))
                .replace(" ", "");

        // De-capitalizes the first letter.
        if (!processedName.isEmpty()) {
            processedName = processedName.substring(0, 1).toLowerCase(Locale.ROOT) + processedName.substring(1);
        }
        return processedName;
    }

    /**
     * Gets the name of the enchantment in a human-friendly format.
     * Example: "FEATHER_FALLING" -> "Feather Falling"
     *
     * @return The formatted name.
     */
    public @NotNull String getFormattedName() {
        return MessageUtil.capitalize(name().toLowerCase(Locale.ROOT)
                .replace("_", " "));
    }

    /**
     * Gets the corresponding enchantment cost from Settings.
     * Example: "FEATHER_FALLING" -> Settings.featherFallingCost
     *
     * @return The enchant cost.
     */
    public int getCost() {
        switch (this) {
            case FEATHER_FALLING:
                return Settings.featherFallingCost;
            case THORNS:
                return Settings.thornsCost;
            case PROTECTION:
                return Settings.protectionCost;
            case KNOCKBACK:
                return Settings.knockbackCost;
            case SHARPNESS:
                return Settings.sharpnessCost;
            case PUNCH:
                return Settings.punchCost;
            case POWER:
                return Settings.powerCost;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}

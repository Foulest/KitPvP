package net.foulest.kitpvp.enchants;

import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.jetbrains.annotations.NotNull;

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
        String processedName = MessageUtil.capitalize(this.name().toLowerCase()
                        .replace("_", " "))
                .replace(" ", "");

        // De-capitalizes the first letter.
        if (!processedName.isEmpty()) {
            processedName = processedName.substring(0, 1).toLowerCase() + processedName.substring(1);
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
        return MessageUtil.capitalize(this.name().toLowerCase()
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

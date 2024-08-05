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
package net.foulest.kitpvp.menus;

import lombok.ToString;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Kit Enchanter GUI.
 *
 * @author Foulest
 * @project KitPvP
 */
@ToString
public class KitEnchanter {

    private final Inventory inventory;

    /**
     * Creates a new instance of the Kit Enchanter GUI.
     *
     * @param player The player to open the GUI for.
     */
    public KitEnchanter(Player player) {
        String inventoryName = MessageUtil.colorize("Kit Enchanter");
        inventory = Bukkit.createInventory(player, 27, inventoryName);

        populateInventory(player);
        player.closeInventory();
        player.openInventory(inventory);
    }

    /**
     * Populates the GUI's inventory.
     */
    private void populateInventory(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int maxSlots = 27;

        for (int i = 0; i < maxSlots; i++) {
            inventory.setItem(i, glass);
        }

        ItemStack featherFalling = createEnchantedItem(
                Material.DIAMOND_BOOTS,
                "&aFeather Falling",
                Arrays.asList(
                        "&7Adds the &fFeather Falling IV &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.FEATHER_FALLING,
                                Settings.featherFallingEnabled, Settings.featherFallingCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        ItemStack thorns = createEnchantedItem(
                Material.DIAMOND_CHESTPLATE,
                "&aThorns",
                Arrays.asList(
                        "&7Adds the &fThorns II &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.THORNS,
                                Settings.thornsEnabled, Settings.thornsCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        ItemStack protection = createEnchantedItem(
                Material.DIAMOND_CHESTPLATE,
                "&aProtection",
                Arrays.asList(
                        "&7Adds the &fProtection II &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.PROTECTION,
                                Settings.protectionEnabled, Settings.protectionCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        ItemStack knockback = createEnchantedItem(
                Material.DIAMOND_SWORD,
                "&aKnockback",
                Arrays.asList(
                        "&7Adds the &fKnockback II &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.KNOCKBACK,
                                Settings.knockbackEnabled, Settings.knockbackCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        ItemStack sharpness = createEnchantedItem(
                Material.DIAMOND_SWORD,
                "&aSharpness",
                Arrays.asList(
                        "&7Adds the &fSharpness II &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.SHARPNESS,
                                Settings.sharpnessEnabled, Settings.sharpnessCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        ItemStack punch = createEnchantedItem(
                Material.BOW,
                "&aPunch",
                Arrays.asList(
                        "&7Adds the &fPunch II &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.PUNCH,
                                Settings.punchEnabled, Settings.punchCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        ItemStack power = createEnchantedItem(
                Material.BOW,
                "&aPower",
                Arrays.asList(
                        "&7Adds the &fPower II &7enchantment.",
                        "",
                        getEnchantmentStatus(playerData, Enchants.POWER,
                                Settings.powerEnabled, Settings.powerCost),
                        "",
                        "&cNote: &7This enchantment is temporary.",
                        "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level."
                )
        );

        inventory.setItem(10, featherFalling);
        inventory.setItem(11, thorns);
        inventory.setItem(12, protection);
        inventory.setItem(13, knockback);
        inventory.setItem(14, sharpness);
        inventory.setItem(15, punch);
        inventory.setItem(16, power);
    }

    private static ItemStack createEnchantedItem(Material material, String name, List<String> lore) {
        return new ItemBuilder(material).addGlow().name(name).lore(lore).getItem();
    }

    private static @NotNull String getEnchantmentStatus(@NotNull PlayerData playerData, Enchants enchant, boolean isEnabled, int cost) {
        if (playerData.getEnchants().contains(enchant)) {
            return "&aYou have this equipped.";
        } else if (isEnabled) {
            return "&7Cost: &6" + cost + " coins";
        } else {
            return "&cThis enchantment is disabled.";
        }
    }
}

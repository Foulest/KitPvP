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
package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the Mage kit.
 *
 * @author Foulest
 */
public class Mage implements Kit {

    @Override
    public String getName() {
        return "Mage";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Material.GLOWSTONE_DUST);
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        // Damage value: 5.0
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).name("&aMage's Sword")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &7No notable changes."
                )).unbreakable(true).hideInfo();

        // Damage value: 3.0
        ItemBuilder sunStaff = new ItemBuilder(Material.BLAZE_ROD).name("&aSun Staff")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &b+100% damage against burning players",
                        "&8\u2503 &b+25% fire damage resistance while active",
                        "&8\u2503 &c-40% damage penalty"
                )).unbreakable(true).hideInfo();

        ItemBuilder special = new ItemBuilder(Material.GLOWSTONE_DUST).name("&aStasis &7(Right Click)")
                .lore("&7Debuff nearby players.");

        return Arrays.asList(sword, sunStaff, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVl"
                + "MmIxNTQ4NTQ1ZTJhMjQ5N2JkMjRhYWM3OTE3OTI2NTRlZjU4N2E1YWI3M2QzNmFiN2Y1ZDliZjcyYTU0NyJ9fX0=";

        return new ItemBuilder[]{
                // Armor value: 5.0
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fMage's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Debuff nearby players."));
    }

    @Override
    public boolean enabled() {
        return Settings.mageKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.mageKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.mage", PermissionDefault.TRUE);
    }
}

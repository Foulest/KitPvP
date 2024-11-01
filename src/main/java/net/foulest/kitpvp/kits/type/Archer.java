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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the Archer kit.
 *
 * @author Foulest
 */
public class Archer implements Kit {

    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemBuilder(Material.BOW).hideInfo().getItem();
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        // Damage value: 4.0
        ItemBuilder sword = new ItemBuilder(Material.WOOD_SWORD).name("&aArcher's Sword")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &c-25% damage penalty"
                )).unbreakable(true).hideInfo();

        // Damage value: 2.0
        ItemBuilder bow = new ItemBuilder(Material.BOW).name("&aArcher's Bow")
                .lore(Arrays.asList(
                        "&7Compared to Bow:",
                        "&8\u2503 &7No notable changes."
                )).unbreakable(true).hideInfo();

        ItemBuilder special = new ItemBuilder(Material.FEATHER).unbreakable(true).hideInfo().name("&aSpeed Boost &7(Right Click)")
                .lore("&7Gain a temporary speed boost.");

        ItemBuilder arrow = new ItemBuilder(Material.ARROW).unbreakable(true).hideInfo().amount(32).slot(8);
        return Arrays.asList(sword, bow, special, arrow);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYT"
                + "MwMzIyZDM1NjgzMjI4ZjMwZmJjYThjZDFjMmE2MDIwODczMDE1MTZmNmI0MzhiNDhkNjc2ZWU1NTIwNzU3MCJ9fX0=";

        return new ItemBuilder[]{
                // Armor value: 3.0
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fArcher's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.LEATHER_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Master of long-ranged combat."));
    }

    @Override
    public boolean enabled() {
        return Settings.archerKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.archerKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.archer", PermissionDefault.TRUE);
    }
}

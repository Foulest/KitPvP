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
 * Represents the Pyro kit.
 *
 * @author Foulest
 */
public class Pyro implements Kit {

    @Override
    public String getName() {
        return "Pyro";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemBuilder(Material.FLINT_AND_STEEL).hideInfo().getItem();
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false)
        };
    }

    @Override
    public List<ItemBuilder> getItems() {
        // Damage value: 4.0
        ItemBuilder axtinguisher = new ItemBuilder(Material.STONE_AXE).name("&aAxtinguisher")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &bMini-crits burning players and extinguishes them",
                        "&8\u2503 &bKilling blows on burning players grant a speed boost",
                        "&8\u2503 &c-25% damage penalty"))
                .unbreakable(true).hideInfo();

        // Damage value: 3.0
        ItemBuilder powerjack = new ItemBuilder(Material.GOLD_AXE).name("&aPowerjack")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &bRestores 3 hearts on kill",
                        "&8\u2503 &b+20% movement speed when active",
                        "&8\u2503 &c+20% damage vulnerability when active",
                        "&8\u2503 &c-40% damage penalty"))
                .unbreakable(true).hideInfo();

        ItemBuilder special = new ItemBuilder(Material.FIREBALL).name("&aIgnite &7(Right Click)")
                .lore("&7Ignites players on fire.");

        return Arrays.asList(axtinguisher, powerjack, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWF"
                + "mNTc2NDU0Y2I2NDFhNmU1OTVlZGY0ZTc3YTcwYzIwM2U4OGVjYWIwZjIyMGQzZmUzMGZiM2NjYzhjOGJhOCJ9fX0=";

        return new ItemBuilder[]{
                // Armor value: 4.5
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fPyro's Head"),
                new ItemBuilder(Material.GOLD_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Set other players ablaze."));
    }

    @Override
    public boolean enabled() {
        return Settings.pyroKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.pyroKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.pyro", PermissionDefault.TRUE);
    }
}

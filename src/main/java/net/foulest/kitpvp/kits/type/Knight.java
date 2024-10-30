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
import java.util.Collections;
import java.util.List;

/**
 * Represents the Knight kit.
 *
 * @author Foulest
 */
public class Knight implements Kit {

    @Override
    public String getName() {
        return "Knight";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemBuilder(Material.IRON_CHESTPLATE).hideInfo().getItem();
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        // Attack value: 5.0
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        return Collections.singletonList(sword);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOD"
                + "A1Mzk3M2U3YzUyMzcyYzNiMTExMzk0ZGZmOTUxOWNiYWMxZmJhM2Y2NTliMjE4NmJlZjhlZWY5ZTEwZmEyIn19fQ==";

        return new ItemBuilder[]{
                // Armor value: 6.5
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fKnight's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7No perks or abilities."));
    }

    @Override
    public boolean enabled() {
        return Settings.knightKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.knightKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.knight", PermissionDefault.TRUE);
    }
}

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
import net.foulest.kitpvp.util.item.ItemBuilder;
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
import java.util.Objects;

import static net.foulest.kitpvp.util.Settings.ghostKitCost;
import static net.foulest.kitpvp.util.Settings.ghostKitEnabled;

public class Ghost implements Kit {

    @Override
    public String getName() {
        return "Ghost";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.GHAST_TEAR));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true)
        };
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.DIAMOND_SWORD).unbreakable(true).hideInfo().enchant(Enchantment.DAMAGE_ALL, 1);
        ItemBuilder air = new ItemBuilder(Material.AIR).slot(1);
        return Arrays.asList(sword, air);
    }

    @Override
    public ItemBuilder[] getArmor() {
        return new ItemBuilder[]{
                new ItemBuilder(Material.AIR),
                new ItemBuilder(Material.AIR),
                new ItemBuilder(Material.AIR),
                new ItemBuilder(Material.AIR)
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Permanent invisibility."));
    }

    @Override
    public boolean enabled() {
        return ghostKitEnabled;
    }

    @Override
    public int getCost() {
        return ghostKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.ghost", PermissionDefault.TRUE);
    }
}

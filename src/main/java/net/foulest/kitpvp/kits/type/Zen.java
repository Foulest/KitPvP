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
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents the Zen kit.
 *
 * @author Foulest
 */
public class Zen implements Kit {

    @Override
    public String getName() {
        return "Zen";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.SLIME_BALL));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.SLIME_BALL).name("&aTeleporter &7(Right Click)")
                .lore("&7Teleport to the nearest player.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMT"
                + "g2ZDMxZTRkZDE2OWI0YzFjNjRkNDg1YjhlODRjN2IxY2NlYTdmNmZhYzg4ZTI1YTA4ZDJiN2ZmYmI4NDBhOCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fZen's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0x3E7F2B)),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Teleports to the nearest player."));
    }

    @Override
    public boolean enabled() {
        return Settings.zenKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.zenKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.zen", PermissionDefault.TRUE);
    }
}

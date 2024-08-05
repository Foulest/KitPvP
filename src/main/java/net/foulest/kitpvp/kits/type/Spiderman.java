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

import lombok.NoArgsConstructor;
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

@NoArgsConstructor
public class Spiderman implements Kit {

    @Override
    public String getName() {
        return "Spiderman";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.WEB));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.WOOD_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.WEB).name("&aWeb Slinger &7(Right Click)")
                .lore("&7Traps players in cobwebs.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYW"
                + "M0MGQ5ODBjNjEyNzI0M2NkM2JiZGMwN2FhOTY1NDgzZWI2YTdlZWFiOWFhY2U5NzAzZGY5ZGYyOGQ3ZjU1MiJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fSpiderman's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0xCC2E33)),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.DIAMOND_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aDefensive", "", "&7Traps players in cobwebs."));
    }

    @Override
    public boolean enabled() {
        return Settings.spidermanKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.spidermanKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.spiderman", PermissionDefault.TRUE);
    }
}

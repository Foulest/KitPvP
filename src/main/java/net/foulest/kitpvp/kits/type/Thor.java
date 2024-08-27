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

import java.util.*;

/**
 * Represents the Thor kit.
 *
 * @author Foulest
 */
public class Thor implements Kit {

    @Override
    public String getName() {
        return "Thor";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.IRON_AXE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.IRON_AXE).name("&aMjolnir &7(Right Click)")
                .lore("&7Strike your opponents with lightning.").unbreakable(true).hideInfo();
        return Collections.singletonList(sword);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                + "Zjg2NDRkZmQyNGM4MjRmNTU1NTY5ZWMwNjVjMDcwYTk3ZWQ5M2U1ZTY0M2E3MmQ1MzA0OGUyMDMyMWUwYjI3MCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fThor's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.GOLD_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.GOLD_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Strike your opponents with lightning."));
    }

    @Override
    public boolean enabled() {
        return Settings.thorKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.thorKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.thor", PermissionDefault.TRUE);
    }
}

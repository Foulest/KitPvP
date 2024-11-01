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
 * Represents the Kangaroo kit.
 *
 * @author Foulest
 */
public class Kangaroo implements Kit {

    @Override
    public String getName() {
        return "Kangaroo";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Material.FIREWORK);
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false)
        };
    }

    @Override
    public List<ItemBuilder> getItems() {
        // Damage value: 4.0
        ItemBuilder sword = new ItemBuilder(Material.WOOD_SWORD).name("&aKangaroo's Sword")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &c-25% damage penalty"
                )).unbreakable(true).hideInfo();

        // Damage value: 3.0
        // Mid-air damage value: 7.5
        ItemBuilder shovel = new ItemBuilder(Material.STONE_SPADE).name("&aMarket Gardener")
                .lore(Arrays.asList(
                        "&7Compared to Stone Sword:",
                        "&8\u2503 &b+150% damage bonus while hopping",
                        "&8\u2503 &c-40% damage penalty"
                )).unbreakable(true).hideInfo();

        ItemBuilder special = new ItemBuilder(Material.FIREWORK).name("&aHop &7(Right Click)")
                .lore("&7Hop around like a Kangaroo.");

        return Arrays.asList(sword, shovel, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "ewogICJ0aW1lc3RhbXAiIDogMTYyMjI4MzIxMTIwOSwKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2"
                + "ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNeVV1aWRJcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCi"
                + "AgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZX"
                + "QvdGV4dHVyZS9kM2I2MWVjNGI1MjU2NDUzZWRjOTU0MTZhODJiNTRkMjQyMzdhZTgxNGQzNjYzMjQ1MzZhZTkxYzgxYzM5NWVlIg"
                + "ogICAgfQogIH0KfQ==";

        return new ItemBuilder[]{
                // Armor value: 5.0
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fKangaroo's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0x7A6738)),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Hop around like a Kangaroo."));
    }

    @Override
    public boolean enabled() {
        return Settings.kangarooKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.kangarooKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.kangaroo", PermissionDefault.TRUE);
    }
}

package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Hulk implements Kit {

    @Override
    public String getName() {
        return "Hulk";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.PISTON_STICKY_BASE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false, false)
        };
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).getItem();
        ItemStack special = new ItemBuilder(Material.PISTON_STICKY_BASE).name("&aHulk Smash &7(Right Click)")
                .lore("&7Deals players immense knockback.").getItem();
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZ"
                + "GJhMTIwZTM3MjdkZDc3MDY2Mjk3MThhNTE3MTI1YjFkNTgwNWZmYTUxM2E3ZDcxZmYyMmRiYTg4NjRmZWMzMSJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fHulk's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.fromRGB(0x3E7F2B)).unbreakable(true).getItem(),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.fromRGB(0x3E7F2B)).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Deals players immense knockback."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.SkullUtil;
import net.foulest.kitpvp.util.kits.Kit;
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
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.PISTON_STICKY_BASE).name("&aHulk Smash &7(Right Click)")
                .lore("&7Deals players immense knockback.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZ"
                + "GJhMTIwZTM3MjdkZDc3MDY2Mjk3MThhNTE3MTI1YjFkNTgwNWZmYTUxM2E3ZDcxZmYyMmRiYTg4NjRmZWMzMSJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullUtil.itemFromBase64(base64)).name("&fHulk's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0x3E7F2B)),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.LEATHER_BOOTS).unbreakable(true).hideInfo().color(Color.fromRGB(0x3E7F2B))
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Deals players immense knockback."));
    }

    @Override
    public boolean enabled() {
        return Settings.hulkKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.hulkKitCost;
    }

    @Override
    public boolean premiumOnly() {
        return Settings.hulkKitPremiumOnly;
    }
}

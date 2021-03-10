package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
public class Kangaroo implements Kit {

    @Override
    public String getName() {
        return "Kangaroo";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.FIREWORK));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false)
        };
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).getItem();
        ItemStack special = new ItemBuilder(Material.FIREWORK).name("&aHop &7(Right Click)")
                .lore("&7Hop around like a Kangaroo.").getItem();
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTR"
                + "hYjQyYWZhOTJhZGYxNWFiZmJmNDljZDA5NjY0NDA5NjQ5Mzk3N2YzNjYyMDNmNTIzMTFlYzk3ODJiY2YyNCJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fKangaroo's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.fromRGB(0x7F4A19)).unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_BOOTS).enchant(Enchantment.PROTECTION_FALL, 4).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Hop around like a Kangaroo."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

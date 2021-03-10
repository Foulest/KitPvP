package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.kits.Kit;
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
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 1).unbreakable(true).getItem();
        ItemStack air = new ItemBuilder(Material.AIR).getItem();
        return Arrays.asList(sword, air);
    }

    @Override
    public ItemStack[] getArmor() {
        return new ItemStack[]{
                new ItemBuilder(Material.AIR).getItem(),
                new ItemBuilder(Material.AIR).getItem(),
                new ItemBuilder(Material.AIR).getItem(),
                new ItemBuilder(Material.AIR).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Permanent invisibility."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

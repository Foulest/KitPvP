package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.kits.Kit;
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
    public int getCost() {
        return 250;
    }
}

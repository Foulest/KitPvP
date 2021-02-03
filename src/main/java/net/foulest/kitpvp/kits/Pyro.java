package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
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

public class Pyro implements Kit {

    @Override
    public String getName() {
        return "Pyro";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.FLINT_AND_STEEL));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false)
        };
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack weapon = new ItemBuilder(Material.STONE_SWORD).enchant(Enchantment.FIRE_ASPECT, 1).unbreakable(true).build();
        ItemStack bow = new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE, 1).enchant(Enchantment.ARROW_FIRE, 1).unbreakable(true).build();
        ItemStack arrow = new ItemBuilder(Material.ARROW).amount(16).build();
        return Arrays.asList(weapon, bow, arrow);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWF"
                + "mNTc2NDU0Y2I2NDFhNmU1OTVlZGY0ZTc3YTcwYzIwM2U4OGVjYWIwZjIyMGQzZmUzMGZiM2NjYzhjOGJhOCJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fPyro's Head").unbreakable(true).build(),
                new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).unbreakable(true).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).unbreakable(true).build(),
                new ItemBuilder(Material.GOLD_BOOTS).enchant(Enchantment.PROTECTION_FIRE, 4).unbreakable(true).build()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Set other players ablaze."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

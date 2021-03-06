package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Mage implements Kit {

    @Override
    public String getName() {
        return "Mage";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.GLOWSTONE_DUST));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).getItem();
        ItemStack special = new ItemBuilder(Material.GLOWSTONE_DUST).name("&aRandom Effect &7(Right Click)")
                .lore("&7Gives you random potion effects.").getItem();
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVl"
                + "MmIxNTQ4NTQ1ZTJhMjQ5N2JkMjRhYWM3OTE3OTI2NTRlZjU4N2E1YWI3M2QzNmFiN2Y1ZDliZjcyYTU0NyJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fMage's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Gives you random potion effects."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

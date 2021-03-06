package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Cactus implements Kit {

    @Override
    public String getName() {
        return "Cactus";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.CACTUS));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.CACTUS).enchant(Enchantment.KNOCKBACK, 2)
                .enchant(Enchantment.DAMAGE_ALL, 3).name("&aPrick").lore("&7Inflict knockback and poison.").getItem();
        return Collections.singletonList(sword);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNm"
                + "MwNzRlNjA2ZDIwNzg0YTc3OTZmYWIyYzBkMDM1NjRmNjVhODI2YzQwYTA1ZWU3NjkxYjYxODZjMjExYzlmMiJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fCactus's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.fromRGB(0x197F22)).unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.fromRGB(0x197F22)).unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.fromRGB(0x197F22)).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Inflict knockback and poison."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

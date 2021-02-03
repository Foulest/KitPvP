package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Vampire implements Kit {

    @Override
    public String getName() {
        return "Vampire";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.REDSTONE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).build();
        ItemStack special = new ItemBuilder(Material.REDSTONE).name("&aDrain Effects &7(Right Click)")
                .lore("&7Drains players potion effects.").build();
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ"
                + "0NDc1NmUwYjRlY2U4ZDc0NjI5NmEzZDVlMjk3ZTE0MTVmNGJhMTc2NDdmZmUyMjgzODUzODNkMTYxYTkifX19";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fVampire's Head").unbreakable(true).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.fromRGB(0x191919)).unbreakable(true).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).build(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).build()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Drains players potion effects."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

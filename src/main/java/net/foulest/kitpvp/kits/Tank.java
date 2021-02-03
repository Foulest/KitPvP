package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Tank implements Kit {

    @Override
    public String getName() {
        return "Tank";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.DIAMOND_CHESTPLATE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false)
        };
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack weapon = new ItemBuilder(Material.STONE_AXE).unbreakable(true).build();
        return Collections.singletonList(weapon);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYT"
                + "Y1OWIyYmIwNzBjMTIwOGJhNTE0NTIzNjFmZDMwYTY2NzIxMzI5NWYyMWRiNDM3ZGY1NzI4MWQ1ODJjODlhZCJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fTank's Head").unbreakable(true).build(),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE).unbreakable(true).build(),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).build(),
                new ItemBuilder(Material.DIAMOND_BOOTS).unbreakable(true).build()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aDefensive", "", "&7Slow but very resistant."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

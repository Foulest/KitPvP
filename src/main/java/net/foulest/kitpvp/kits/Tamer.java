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

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Tamer implements Kit {

    @Override
    public String getName() {
        return "Tamer";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.BONE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).getItem();
        ItemStack special = new ItemBuilder(Material.BONE).name("&aSummon Dogs &7(Right Click)")
                .lore("&7Summon the hounds.").getItem();
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMW"
                + "U2ZjU4NmZiZjViMTMxNmVlODI4Mjk0NmM4NTA4NzUxYzk3MTk0ZGFjZWVjNTk5ZDIxNDg4ZjNhYTU0NTAyNSJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fTamer's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.fromRGB(0xCCAA7A)).unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Summon the hounds."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

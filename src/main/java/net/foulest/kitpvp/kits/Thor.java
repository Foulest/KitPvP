package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Thor implements Kit {

    @Override
    public String getName() {
        return "Thor";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.IRON_AXE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.IRON_AXE).name("&aMjolnir &7(Right Click)")
                .lore("&7Strike your opponents with lightning.").unbreakable(true).getItem();
        return Collections.singletonList(sword);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                + "Zjg2NDRkZmQyNGM4MjRmNTU1NTY5ZWMwNjVjMDcwYTk3ZWQ5M2U1ZTY0M2E3MmQ1MzA0OGUyMDMyMWUwYjI3MCJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fThor's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).getItem(),
                new ItemBuilder(Material.GOLD_LEGGINGS).unbreakable(true).getItem(),
                new ItemBuilder(Material.GOLD_BOOTS).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Strike your opponents with lightning."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

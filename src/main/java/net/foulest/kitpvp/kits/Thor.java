package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.SkullUtil;
import net.foulest.kitpvp.util.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * @author Foulest
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
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.IRON_AXE).name("&aMjolnir &7(Right Click)")
                .lore("&7Strike your opponents with lightning.").unbreakable(true).hideInfo();
        return Collections.singletonList(sword);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                + "Zjg2NDRkZmQyNGM4MjRmNTU1NTY5ZWMwNjVjMDcwYTk3ZWQ5M2U1ZTY0M2E3MmQ1MzA0OGUyMDMyMWUwYjI3MCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullUtil.itemFromBase64(base64)).name("&fThor's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.GOLD_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.GOLD_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Strike your opponents with lightning."));
    }

    @Override
    public boolean enabled() {
        return Settings.thorKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.thorKitCost;
    }

    @Override
    public boolean premiumOnly() {
        return Settings.thorKitPremiumOnly;
    }
}

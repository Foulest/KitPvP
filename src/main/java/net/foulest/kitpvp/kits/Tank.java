package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.SkullUtil;
import net.foulest.kitpvp.util.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * @author Foulest
 * @project KitPvP
 */
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
    public List<ItemBuilder> getItems() {
        ItemBuilder weapon = new ItemBuilder(Material.STONE_AXE).unbreakable(true).hideInfo();
        return Collections.singletonList(weapon);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYT"
                + "Y1OWIyYmIwNzBjMTIwOGJhNTE0NTIzNjFmZDMwYTY2NzIxMzI5NWYyMWRiNDM3ZGY1NzI4MWQ1ODJjODlhZCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullUtil.itemFromBase64(base64)).name("&fTank's Head"),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.DIAMOND_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aDefensive", "", "&7Slow but very resistant."));
    }

    @Override
    public boolean enabled() {
        return Settings.tankKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.tankKitCost;
    }

    @Override
    public boolean premiumOnly() {
        return Settings.tamerKitPremiumOnly;
    }
}

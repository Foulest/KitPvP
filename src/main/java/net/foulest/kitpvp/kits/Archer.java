package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.SkullUtil;
import net.foulest.kitpvp.util.kits.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Foulest
 * @project KitPvP
 */
public class Archer implements Kit {

    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Material.BOW);
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false),
                new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0, false, false)
        };
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.WOOD_SWORD).unbreakable(true).hideInfo().enchant(Enchantment.KNOCKBACK, 1);
        ItemBuilder bow = new ItemBuilder(Material.BOW).unbreakable(true).hideInfo().enchant(Enchantment.ARROW_DAMAGE, 1);
        ItemBuilder arrow = new ItemBuilder(Material.ARROW).unbreakable(true).hideInfo().amount(32).slot(9);
        return Arrays.asList(sword, bow, arrow);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYT"
                + "MwMzIyZDM1NjgzMjI4ZjMwZmJjYThjZDFjMmE2MDIwODczMDE1MTZmNmI0MzhiNDhkNjc2ZWU1NTIwNzU3MCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullUtil.itemFromBase64(base64)).name("&fArcher's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.LEATHER_BOOTS).unbreakable(true).hideInfo().enchant(Enchantment.PROTECTION_FALL, 4)
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Master of long-ranged combat."));
    }

    @Override
    public boolean enabled() {
        return Settings.archerKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.archerKitCost;
    }

    @Override
    public boolean premiumOnly() {
        return Settings.archerKitPremiumOnly;
    }
}

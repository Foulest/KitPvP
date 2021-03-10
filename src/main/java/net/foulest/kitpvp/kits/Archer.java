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

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Archer implements Kit {

    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.BOW));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false),
                new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0, false, false)
        };
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).hideInfo().enchant(Enchantment.KNOCKBACK, 1).unbreakable(true).getItem();
        ItemStack bow = new ItemBuilder(Material.BOW).hideInfo().enchant(Enchantment.ARROW_DAMAGE, 1).unbreakable(true).getItem();
        ItemStack arrow = new ItemBuilder(Material.ARROW).hideInfo().amount(32).getItem();
        return Arrays.asList(sword, bow, arrow);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYT"
                + "MwMzIyZDM1NjgzMjI4ZjMwZmJjYThjZDFjMmE2MDIwODczMDE1MTZmNmI0MzhiNDhkNjc2ZWU1NTIwNzU3MCJ9fX0=";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).hideInfo().name("&fArcher's Head").unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).hideInfo().unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).hideInfo().unbreakable(true).getItem(),
                new ItemBuilder(Material.LEATHER_BOOTS).hideInfo().enchant(Enchantment.PROTECTION_FALL, 4).unbreakable(true).getItem()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Master of long-ranged combat."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

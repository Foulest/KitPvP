package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.foulest.kitpvp.util.Settings.pyroKitCost;
import static net.foulest.kitpvp.util.Settings.pyroKitEnabled;

public class Pyro implements Kit {

    @Override
    public String getName() {
        return "Pyro";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.FLINT_AND_STEEL));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder weapon = new ItemBuilder(Material.WOOD_SWORD).unbreakable(true).hideInfo().enchant(Enchantment.FIRE_ASPECT, 1);
        ItemBuilder bow = new ItemBuilder(Material.BOW).unbreakable(true).hideInfo().enchant(Enchantment.ARROW_FIRE, 1);
        ItemBuilder arrow = new ItemBuilder(Material.ARROW).amount(16).slot(9);
        return Arrays.asList(weapon, bow, arrow);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWF"
                + "mNTc2NDU0Y2I2NDFhNmU1OTVlZGY0ZTc3YTcwYzIwM2U4OGVjYWIwZjIyMGQzZmUzMGZiM2NjYzhjOGJhOCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fPyro's Head"),
                new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.GOLD_BOOTS).unbreakable(true).hideInfo().enchant(Enchantment.PROTECTION_FIRE, 4)
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Set other players ablaze."));
    }

    @Override
    public boolean enabled() {
        return pyroKitEnabled;
    }

    @Override
    public int getCost() {
        return pyroKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.pyro", PermissionDefault.TRUE);
    }
}

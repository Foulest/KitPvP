package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.foulest.kitpvp.util.Settings.cactusKitCost;
import static net.foulest.kitpvp.util.Settings.cactusKitEnabled;

public class Cactus implements Kit {

    @Override
    public String getName() {
        return "Cactus";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Material.CACTUS);
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.CACTUS).enchant(Enchantment.KNOCKBACK, 1)
                .enchant(Enchantment.DAMAGE_ALL, 4).name("&aPrick").lore("&7Inflict knockback and poison.");
        return Collections.singletonList(sword);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNm"
                + "MwNzRlNjA2ZDIwNzg0YTc3OTZmYWIyYzBkMDM1NjRmNjVhODI2YzQwYTA1ZWU3NjkxYjYxODZjMjExYzlmMiJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fCactus's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0x197F22)),
                new ItemBuilder(Material.LEATHER_LEGGINGS).unbreakable(true).hideInfo().color(Color.fromRGB(0x197F22)),
                new ItemBuilder(Material.LEATHER_BOOTS).unbreakable(true).hideInfo().color(Color.fromRGB(0x197F22))
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Inflict knockback and poison."));
    }

    @Override
    public boolean enabled() {
        return cactusKitEnabled;
    }

    @Override
    public int getCost() {
        return cactusKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.cactus", PermissionDefault.TRUE);
    }
}

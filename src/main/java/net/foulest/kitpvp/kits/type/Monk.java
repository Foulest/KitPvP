package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.foulest.kitpvp.util.Settings.monkKitCost;
import static net.foulest.kitpvp.util.Settings.monkKitEnabled;

public class Monk implements Kit {

    @Override
    public String getName() {
        return "Monk";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.BLAZE_ROD));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.BLAZE_ROD).name("&aItem Scrambler &7(Right Click)")
                .lore("&7Scrambles a player's hotbar items.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY"
                + "WM5NmY3NWY2YWQ2ZTZhNjNhNWY3ZmI3ZTVkNWE5MmI4NmI4MzI2MmQyNzgzZThlMjBiMWZkZDA2NDlmNjllIn19fQ==";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fMonk's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.ORANGE),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Scramble a player's hotbar items."));
    }

    @Override
    public boolean enabled() {
        return monkKitEnabled;
    }

    @Override
    public int getCost() {
        return monkKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.monk", PermissionDefault.TRUE);
    }
}

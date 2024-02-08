package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.foulest.kitpvp.util.Settings.burrowerKitCost;
import static net.foulest.kitpvp.util.Settings.burrowerKitEnabled;

public class Burrower implements Kit {

    @Override
    public String getName() {
        return "Burrower";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Material.BRICK);
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.BRICK).name("&aPanic Room &7(Right Click)")
                .lore("&7Create a panic room for protection.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMW"
                + "M0NDBlYWM4YmQ1MzA4YzMyY2Y5ODJjM2I5YzNjOWI0OWQzNDVkYjY0ODNlZDQ0Nzg0ZmQyZDk0ZmNhMzIyZSJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fBurrower's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aDefensive", "", "&7Create a panic room for protection."));
    }

    @Override
    public boolean enabled() {
        return burrowerKitEnabled;
    }

    @Override
    public int getCost() {
        return burrowerKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.burrower", PermissionDefault.TRUE);
    }
}
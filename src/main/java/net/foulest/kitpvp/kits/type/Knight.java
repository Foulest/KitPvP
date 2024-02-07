package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static net.foulest.kitpvp.util.Settings.knightKitCost;
import static net.foulest.kitpvp.util.Settings.knightKitEnabled;

public class Knight implements Kit {

    @Override
    public String getName() {
        return "Knight";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.IRON_CHESTPLATE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        return Collections.singletonList(sword);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOD"
                + "A1Mzk3M2U3YzUyMzcyYzNiMTExMzk0ZGZmOTUxOWNiYWMxZmJhM2Y2NTliMjE4NmJlZjhlZWY5ZTEwZmEyIn19fQ==";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fKnight's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7No perks or abilities."));
    }

    @Override
    public boolean enabled() {
        return knightKitEnabled;
    }

    @Override
    public int getCost() {
        return knightKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.knight", PermissionDefault.TRUE);
    }
}

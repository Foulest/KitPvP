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
import java.util.Objects;

import static net.foulest.kitpvp.util.Settings.timelordKitCost;
import static net.foulest.kitpvp.util.Settings.timelordKitEnabled;

public class Timelord implements Kit {

    @Override
    public String getName() {
        return "Timelord";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.WATCH));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.WATCH).name("&aFreeze Time &7(Right Click)")
                .lore("&7Freezes players in time.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYz"
                + "M3Mjc0ZWMyODg5YjdjYWZhYjc2OGFkMjE2YzNlM2FlNjZmODAwNTQ3MDljNDcwNTI3NGVhNDAyMDA1Yzk2YiJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fTimelord's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Freezes players in time."));
    }

    @Override
    public boolean enabled() {
        return timelordKitEnabled;
    }

    @Override
    public int getCost() {
        return timelordKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.timelord", PermissionDefault.TRUE);
    }
}

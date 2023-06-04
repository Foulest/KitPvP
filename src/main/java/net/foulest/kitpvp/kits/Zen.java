package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.SkullUtil;
import net.foulest.kitpvp.util.kits.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Foulest
 * @project KitPvP
 */
public class Zen implements Kit {

    @Override
    public String getName() {
        return "Zen";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.SLIME_BALL));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.SLIME_BALL).name("&aTeleporter &7(Right Click)")
                .lore("&7Teleport to the nearest player.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMT"
                + "g2ZDMxZTRkZDE2OWI0YzFjNjRkNDg1YjhlODRjN2IxY2NlYTdmNmZhYzg4ZTI1YTA4ZDJiN2ZmYmI4NDBhOCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullUtil.itemFromBase64(base64)).name("&fZen's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0x3E7F2B)),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Teleports to the nearest player."));
    }

    @Override
    public boolean enabled() {
        return Settings.zenKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.zenKitCost;
    }

    @Override
    public boolean premiumOnly() {
        return Settings.zenKitEnabled;
    }
}

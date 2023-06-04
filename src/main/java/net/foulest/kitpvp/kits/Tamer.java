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
public class Tamer implements Kit {

    @Override
    public String getName() {
        return "Tamer";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.BONE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.BONE).name("&aSummon Dogs &7(Right Click)")
                .lore("&7Summon the hounds.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMW"
                + "U2ZjU4NmZiZjViMTMxNmVlODI4Mjk0NmM4NTA4NzUxYzk3MTk0ZGFjZWVjNTk5ZDIxNDg4ZjNhYTU0NTAyNSJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullUtil.itemFromBase64(base64)).name("&fTamer's Head"),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).unbreakable(true).hideInfo().color(Color.fromRGB(0xCCAA7A)),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aMixed", "", "&7Summon the hounds."));
    }

    @Override
    public boolean enabled() {
        return Settings.tamerKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.tamerKitCost;
    }

    @Override
    public boolean premiumOnly() {
        return Settings.tamerKitPremiumOnly;
    }
}

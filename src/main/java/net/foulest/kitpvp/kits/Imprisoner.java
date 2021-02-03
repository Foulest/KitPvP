package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.SkullCreator;
import net.foulest.kitpvp.utils.kits.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Imprisoner implements Kit {

    @Override
    public String getName() {
        return "Imprisoner";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.LAVA_BUCKET));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemStack> getItems() {
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD).unbreakable(true).build();
        ItemStack special = new ItemBuilder(Material.DISPENSER).name("&aLava Prison &7(Right Click)")
                .lore("&7Sends players to jail.").build();
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemStack[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjc"
                + "xNTdjZmZiMDYwNjNiMzUyZGM2ODQ3OGY0NzZlN2QyMDJjM2JhNmU3Y2JmMjk3MjQxYmU4MTY4MTA3NGJmIn19fQ==";

        return new ItemStack[]{
                new ItemBuilder(SkullCreator.itemFromBase64(base64)).name("&fImprisoner's Head").unbreakable(true).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.fromRGB(0x1E4866)).unbreakable(true).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).unbreakable(true).build(),
                new ItemBuilder(Material.IRON_BOOTS).unbreakable(true).build()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aOffensive", "", "&7Sends players to lava jail."));
    }

    @Override
    public int getCost() {
        return 250;
    }
}

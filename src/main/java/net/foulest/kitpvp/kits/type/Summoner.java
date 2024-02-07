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

import static net.foulest.kitpvp.util.Settings.summonerKitCost;
import static net.foulest.kitpvp.util.Settings.summonerKitEnabled;

public class Summoner implements Kit {

    @Override
    public String getName() {
        return "Summoner";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.IRON_BLOCK));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[0];
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder sword = new ItemBuilder(Material.WOOD_SWORD).unbreakable(true).hideInfo();
        ItemBuilder special = new ItemBuilder(Material.IRON_BLOCK).name("&aSummon Golem &7(Right Click)")
                .lore("&7Summon your very own Iron Golem.");
        return Arrays.asList(sword, special);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZT"
                + "RiNTcxYWY0M2JhZjBkYmI2ODI0OTNiZGUxY2U0ZTg0N2RiNzU4ZGQ5Njg1ZTliZWMyYjdhYmJjYzcyNzcyNiJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fSummoner's Head"),
                new ItemBuilder(Material.IRON_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.CHAINMAIL_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aDefensive", "", "&7Summon your very own Iron Golem."));
    }

    @Override
    public boolean enabled() {
        return summonerKitEnabled;
    }

    @Override
    public int getCost() {
        return summonerKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.summoner", PermissionDefault.TRUE);
    }
}

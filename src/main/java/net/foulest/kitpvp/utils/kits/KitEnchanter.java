package net.foulest.kitpvp.utils.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class KitEnchanter {

    public static final String INVENTORY_NAME = MiscUtils.colorize("Kit Enchanter");
    private final Inventory inv;

    public KitEnchanter(Player player) {
        inv = Bukkit.createInventory(player, 36, INVENTORY_NAME);

        populateInventory(player);
        player.closeInventory();
        player.openInventory(inv);
    }

    // Populates the GUI's inventory.
    private void populateInventory(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build();

        // Sets non-present items to glass.
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, glass);
        }

        ItemStack featherFalling = new ItemBuilder(Material.ENCHANTED_BOOK).name("&aFeather Falling")
                .lore(Arrays.asList("&7Adds the &fFeather Falling IV &7enchantment.", "",
                        (player.hasMetadata("featherFalling") ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).build();

        ItemStack protection = new ItemBuilder(Material.ENCHANTED_BOOK).name("&aProtection")
                .lore(Arrays.asList("&7Adds the &fProtection II &7enchantment.", "",
                        (player.hasMetadata("protection") ? "&cYou have this equipped." : "&7Cost: &6150 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).build();

        ItemStack power = new ItemBuilder(Material.ENCHANTED_BOOK).name("&aPower")
                .lore(Arrays.asList("&7Adds the &fPower II &7enchantment.", "",
                        (player.hasMetadata("power") ? "&cYou have this equipped." : "&7Cost: &6200 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).build();

        ItemStack sharpness = new ItemBuilder(Material.ENCHANTED_BOOK).name("&aSharpness")
                .lore(Arrays.asList("&7Adds the &fSharpness II &7enchantment.", "",
                        (player.hasMetadata("sharpness") ? "&cYou have this equipped." : "&7Cost: &6250 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).build();

        inv.setItem(10, featherFalling);
        inv.setItem(19, new ItemBuilder(Material.FEATHER).name("Feather Falling").build());
        inv.setItem(11, protection);
        inv.setItem(20, new ItemBuilder(Material.DIAMOND_CHESTPLATE).name("Protection").build());
        inv.setItem(12, power);
        inv.setItem(21, new ItemBuilder(Material.BOW).name("Power").build());
        inv.setItem(13, sharpness);
        inv.setItem(22, new ItemBuilder(Material.DIAMOND_SWORD).name("Sharpness").build());
    }

    public Inventory getInventory() {
        return inv;
    }
}

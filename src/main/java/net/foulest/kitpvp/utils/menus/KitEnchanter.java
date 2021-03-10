package net.foulest.kitpvp.utils.menus;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class KitEnchanter {

    public static final String INVENTORY_NAME = MessageUtil.colorize("Kit Enchanter");
    private final Inventory inv;

    public KitEnchanter(Player player) {
        inv = Bukkit.createInventory(player, 27, INVENTORY_NAME);

        populateInventory(player);
        player.closeInventory();
        player.openInventory(inv);
    }

    /**
     * Populates the GUI's inventory.
     */
    private void populateInventory(Player player) {
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int maxSlots = 27;

        for (int i = 0; i < maxSlots; i++) {
            inv.setItem(i, glass);
        }

        ItemStack featherFalling = new ItemBuilder(Material.DIAMOND_BOOTS).addGlow().name("&aFeather Falling")
                .lore(Arrays.asList("&7Adds the &fFeather Falling IV &7enchantment.", "",
                        (player.hasMetadata("featherFalling") ? "&cYou have this equipped." : "&7Cost: &650 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack thorns = new ItemBuilder(Material.DIAMOND_CHESTPLATE).addGlow().name("&aThorns")
                .lore(Arrays.asList("&7Adds the &fThorns II &7enchantment.", "",
                        (player.hasMetadata("thorns") ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack protection = new ItemBuilder(Material.DIAMOND_CHESTPLATE).addGlow().name("&aProtection")
                .lore(Arrays.asList("&7Adds the &fProtection II &7enchantment.", "",
                        (player.hasMetadata("protection") ? "&cYou have this equipped." : "&7Cost: &6150 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack knockback = new ItemBuilder(Material.DIAMOND_SWORD).addGlow().name("&aKnockback")
                .lore(Arrays.asList("&7Adds the &fKnockback II &7enchantment.", "",
                        (player.hasMetadata("knockback") ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack sharpness = new ItemBuilder(Material.DIAMOND_SWORD).hideInfo().addGlow().name("&aSharpness")
                .lore(Arrays.asList("&7Adds the &fSharpness II &7enchantment.", "",
                        (player.hasMetadata("sharpness") ? "&cYou have this equipped." : "&7Cost: &6200 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack punch = new ItemBuilder(Material.BOW).addGlow().name("&aPunch")
                .lore(Arrays.asList("&7Adds the &fPunch II &7enchantment.", "",
                        (player.hasMetadata("punch") ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack power = new ItemBuilder(Material.BOW).addGlow().name("&aPower")
                .lore(Arrays.asList("&7Adds the &fPower II &7enchantment.", "",
                        (player.hasMetadata("power") ? "&cYou have this equipped." : "&7Cost: &6200 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        inv.setItem(10, featherFalling);
        inv.setItem(11, protection);
        inv.setItem(12, thorns);
        inv.setItem(13, knockback);
        inv.setItem(14, sharpness);
        inv.setItem(15, power);
        inv.setItem(16, punch);
    }

    public Inventory getInventory() {
        return inv;
    }
}

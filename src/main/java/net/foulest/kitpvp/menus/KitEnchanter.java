package net.foulest.kitpvp.menus;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Inventory for Kit Enchanter
 */
public class KitEnchanter {

    private static final String inventoryName = MessageUtil.colorize("Kit Enchanter");
    private final Inventory inventory;

    public KitEnchanter(Player player) {
        inventory = Bukkit.createInventory(player, 27, inventoryName);

        populateInventory(player);
        player.closeInventory();
        player.openInventory(inventory);
    }

    /**
     * Populates the GUI's inventory.
     */
    private void populateInventory(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int maxSlots = 27;

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        for (int i = 0; i < maxSlots; i++) {
            inventory.setItem(i, glass);
        }

        ItemStack featherFalling = new ItemBuilder(Material.DIAMOND_BOOTS).addGlow().name("&aFeather Falling")
                .lore(Arrays.asList("&7Adds the &fFeather Falling IV &7enchantment.", "",
                        (playerData.isFeatherFallingEnchant() ? "&cYou have this equipped." : "&7Cost: &650 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack thorns = new ItemBuilder(Material.DIAMOND_CHESTPLATE).addGlow().name("&aThorns")
                .lore(Arrays.asList("&7Adds the &fThorns II &7enchantment.", "",
                        (playerData.isThornsEnchant() ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack protection = new ItemBuilder(Material.DIAMOND_CHESTPLATE).addGlow().name("&aProtection")
                .lore(Arrays.asList("&7Adds the &fProtection II &7enchantment.", "",
                        (playerData.isProtectionEnchant() ? "&cYou have this equipped." : "&7Cost: &6150 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack knockback = new ItemBuilder(Material.DIAMOND_SWORD).hideInfo().addGlow().name("&aKnockback")
                .lore(Arrays.asList("&7Adds the &fKnockback II &7enchantment.", "",
                        (playerData.isKnockbackEnchant() ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack sharpness = new ItemBuilder(Material.DIAMOND_SWORD).hideInfo().addGlow().name("&aSharpness")
                .lore(Arrays.asList("&7Adds the &fSharpness II &7enchantment.", "",
                        (playerData.isSharpnessEnchant() ? "&cYou have this equipped." : "&7Cost: &6200 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack punch = new ItemBuilder(Material.BOW).addGlow().name("&aPunch")
                .lore(Arrays.asList("&7Adds the &fPunch II &7enchantment.", "",
                        (playerData.isPunchEnchant() ? "&cYou have this equipped." : "&7Cost: &6100 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        ItemStack power = new ItemBuilder(Material.BOW).addGlow().name("&aPower")
                .lore(Arrays.asList("&7Adds the &fPower II &7enchantment.", "",
                        (playerData.isPunchEnchant() ? "&cYou have this equipped." : "&7Cost: &6200 coins"),
                        "", "&cNote: &7This enchantment is temporary.", "&7Kits that already have this enchantment",
                        "&7will be upgraded to a higher level.")).getItem();

        inventory.setItem(10, featherFalling);
        inventory.setItem(11, thorns);
        inventory.setItem(12, protection);
        inventory.setItem(13, knockback);
        inventory.setItem(14, sharpness);
        inventory.setItem(15, punch);
        inventory.setItem(16, power);
    }
}

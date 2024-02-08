package net.foulest.kitpvp.menus;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Represents the Kit Enchanter GUI.
 *
 * @author Foulest
 * @project KitPvP
 */
public class KitEnchanter {

    private static final String inventoryName = MessageUtil.colorize("Kit Enchanter");
    private final Inventory inventory;

    /**
     * Creates a new instance of the Kit Enchanter GUI.
     *
     * @param player The player to open the GUI for.
     */
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
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int maxSlots = 27;

        for (int i = 0; i < maxSlots; i++) {
            inventory.setItem(i, glass);
        }

        ItemStack featherFalling = new ItemBuilder(Material.DIAMOND_BOOTS).addGlow().name("&aFeather Falling").lore(Arrays.asList(
                "&7Adds the &fFeather Falling IV &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.FEATHER_FALLING) ? "&aYou have this equipped."
                        : (Settings.featherFallingEnabled ? "&7Cost: &6" + Settings.featherFallingCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        ItemStack thorns = new ItemBuilder(Material.DIAMOND_CHESTPLATE).addGlow().name("&aThorns").lore(Arrays.asList(
                "&7Adds the &fThorns II &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.THORNS) ? "&aYou have this equipped."
                        : (Settings.thornsEnabled ? "&7Cost: &6" + Settings.thornsCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        ItemStack protection = new ItemBuilder(Material.DIAMOND_CHESTPLATE).addGlow().name("&aProtection").lore(Arrays.asList(
                "&7Adds the &fProtection II &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.PROTECTION) ? "&aYou have this equipped."
                        : (Settings.protectionEnabled ? "&7Cost: &6" + Settings.protectionCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        ItemStack knockback = new ItemBuilder(Material.DIAMOND_SWORD).hideInfo().addGlow().name("&aKnockback").lore(Arrays.asList(
                "&7Adds the &fKnockback II &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.KNOCKBACK) ? "&aYou have this equipped."
                        : (Settings.knockbackEnabled ? "&7Cost: &6" + Settings.knockbackCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        ItemStack sharpness = new ItemBuilder(Material.DIAMOND_SWORD).hideInfo().addGlow().name("&aSharpness").lore(Arrays.asList(
                "&7Adds the &fSharpness II &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.SHARPNESS) ? "&aYou have this equipped."
                        : (Settings.sharpnessEnabled ? "&7Cost: &6" + Settings.sharpnessCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        ItemStack punch = new ItemBuilder(Material.BOW).addGlow().name("&aPunch").lore(Arrays.asList(
                "&7Adds the &fPunch II &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.PUNCH) ? "&aYou have this equipped."
                        : (Settings.punchEnabled ? "&7Cost: &6" + Settings.punchCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        ItemStack power = new ItemBuilder(Material.BOW).addGlow().name("&aPower").lore(Arrays.asList(
                "&7Adds the &fPower II &7enchantment.",
                "",
                (playerData.getEnchants().contains(Enchants.POWER) ? "&aYou have this equipped."
                        : (Settings.powerEnabled ? "&7Cost: &6" + Settings.powerCost + " coins"
                        : "&cThis enchantment is disabled.")),
                "",
                "&cNote: &7This enchantment is temporary.",
                "&7Kits that already have this enchantment",
                "&7will be upgraded to a higher level.")
        ).getItem();

        inventory.setItem(10, featherFalling);
        inventory.setItem(11, thorns);
        inventory.setItem(12, protection);
        inventory.setItem(13, knockback);
        inventory.setItem(14, sharpness);
        inventory.setItem(15, punch);
        inventory.setItem(16, power);
    }
}

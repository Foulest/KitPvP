package net.foulest.kitpvp.util.kits;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Foulest
 * @project KitPvP
 */
public interface Kit {

    /**
     * The name of the kit.
     */
    String getName();

    /**
     * The display item of the kit.
     */
    ItemStack getDisplayItem();

    /**
     * The items of the kit.
     */
    List<ItemBuilder> getItems();

    /**
     * The armor of the kit.
     */
    ItemBuilder[] getArmor();

    /**
     * The potion effects of the kit.
     */
    PotionEffect[] getPotionEffects();

    /**
     * The lore of the kit.
     */
    List<String> getLore();

    /**
     * The cost of the kit in coins.
     */
    int getCost();

    /**
     * Applies a kit to a player.
     *
     * @param player The player to apply the kit to.
     */
    default void apply(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);
        List<Integer> airSlots = new ArrayList<>();

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        // Checks if the player owns the kit they're trying to equip.
        if (!playerData.getOwnedKits().contains(this)) {
            MessageUtil.messagePlayer(player, "&cYou do not own the " + getName() + " kit.");
            return;
        }

        // Clears the player's inventory and armor.
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        // Clears the player's potion effects.
        for (PotionEffect effects : player.getActivePotionEffects()) {
            player.removePotionEffect(effects.getType());
        }

        // Sets the player's kit data.
        playerData.setKit(this);

        // Sets the player's potion effects.
        if (getPotionEffects() != null) {
            for (PotionEffect effect : getPotionEffects()) {
                if (effect == null) {
                    break;
                }

                player.addPotionEffect(effect);
            }
        }

        // Sets the player's kit items.
        for (ItemBuilder item : getItems()) {
            if (item.getItem().getType().toString().toLowerCase().contains("sword")
                    || item.getItem().getType().toString().toLowerCase().contains("cactus")
                    || item.getItem().getType().toString().toLowerCase().contains("axe")) {
                if (playerData.isKnockbackEnchant()) {
                    item = item.enchant(Enchantment.KNOCKBACK, 2);
                }

                if (playerData.isSharpnessEnchant()) {
                    item = item.enchant(Enchantment.DAMAGE_ALL, 2);
                }
            }

            if (item.getItem().getType().toString().toLowerCase().contains("bow")) {
                if (playerData.isPunchEnchant()) {
                    item = item.enchant(Enchantment.ARROW_KNOCKBACK, 2);
                }

                if (playerData.isPowerEnchant()) {
                    item = item.enchant(Enchantment.ARROW_DAMAGE, 2);
                }
            }

            if (item.getSlot() != 0) {
                if (item.getItem().getType() == Material.AIR) {
                    airSlots.add(item.getSlot());
                }

                player.getInventory().setItem(item.getSlot(), item.getItem());

            } else {
                player.getInventory().addItem(item.getItem());
            }
        }

        // Sets the player's healing item.
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            if (airSlots.contains(i) || player.getInventory().getItem(i) != null) {
                continue;
            }

            if (playerData.isUsingSoup()) {
                player.getInventory().setItem(i, new ItemBuilder(Material.MUSHROOM_SOUP).name("&fMushroom Stew").getItem());
            } else {
                player.getInventory().setItem(i, new ItemBuilder(Material.POTION).durability(16421).name("&fSplash Potion of Healing").getItem());
            }
        }

        // Sets the player's armor.
        ItemBuilder helmet = (getArmor()[0] == null ? new ItemBuilder(Material.AIR) : getArmor()[0]);
        ItemBuilder chestplate = (getArmor()[1] == null ? new ItemBuilder(Material.AIR) : getArmor()[1]);
        ItemBuilder leggings = (getArmor()[2] == null ? new ItemBuilder(Material.AIR) : getArmor()[2]);
        ItemBuilder boots = (getArmor()[3] == null ? new ItemBuilder(Material.AIR) : getArmor()[3]);

        if (playerData.isThornsEnchant()) {
            if (helmet.getItem().getType() != Material.AIR && helmet.getItem().getType() != Material.SKULL_ITEM) {
                helmet = helmet.enchant(Enchantment.THORNS, 2);
            }

            if (chestplate.getItem().getType() != Material.AIR) {
                chestplate = chestplate.enchant(Enchantment.THORNS, 2);
            }

            if (leggings.getItem().getType() != Material.AIR) {
                leggings = leggings.enchant(Enchantment.THORNS, 2);
            }

            if (boots.getItem().getType() != Material.AIR) {
                boots = boots.enchant(Enchantment.THORNS, 2);
            }
        }

        if (playerData.isProtectionEnchant()) {
            if (helmet.getItem().getType() != Material.AIR && helmet.getItem().getType() != Material.SKULL_ITEM) {
                helmet = helmet.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (chestplate.getItem().getType() != Material.AIR) {
                chestplate = chestplate.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (leggings.getItem().getType() != Material.AIR) {
                leggings = leggings.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (boots.getItem().getType() != Material.AIR) {
                boots = boots.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
        }

        if (playerData.isFeatherFallingEnchant() && boots.getItem().getType() != Material.AIR) {
            boots = boots.enchant(Enchantment.PROTECTION_FALL, 4);
        }

        player.getInventory().setHelmet(helmet.getItem());
        player.getInventory().setChestplate(chestplate.getItem());
        player.getInventory().setLeggings(leggings.getItem());
        player.getInventory().setBoots(boots.getItem());
    }
}

package net.foulest.kitpvp.utils.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public interface Kit {

    /**
     * The name of the kit.
     *
     * @return -
     */
    String getName();

    /**
     * The display item of the kit.
     *
     * @return -
     */
    ItemStack getDisplayItem();

    /**
     * The items of the kit.
     *
     * @return -
     */
    List<ItemStack> getItems();

    /**
     * The armor of the kit.
     *
     * @return -
     */
    ItemStack[] getArmor();

    /**
     * The potion effects of the kit.
     *
     * @return -
     */
    PotionEffect[] getPotionEffects();

    /**
     * The lore of the kit.
     *
     * @return -
     */
    List<String> getLore();

    /**
     * The cost of the kit in coins.
     *
     * @return -
     */
    int getCost();

    /**
     * Applies a kit to a player.
     *
     * @param player The player to apply the kit to.
     */
    default void apply(Player player) {
        String featherFallingMetadata = "featherFalling";
        String protectionMetadata = "protection";
        String sharpnessMetadata = "sharpness";
        String powerMetadata = "power";
        PlayerData playerData = PlayerData.getInstance(player);

        // Checks if the player owns the kit they're trying to equip.
        if (!playerData.ownsKit(this)) {
            MessageUtil.messagePlayer(player, "&cYou do not own the " + getName() + " kit.");
            return;
        }

        // Sets the player's kit data.
        playerData.setKit(this);

        // Clears the player's inventory and armor.
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        // Clears the player's potion effects.
        for (PotionEffect effects : player.getActivePotionEffects()) {
            player.removePotionEffect(effects.getType());
        }

        // Sets the player's potion effects.
        if (getPotionEffects() != null) {
            for (PotionEffect effect : getPotionEffects()) {
                if (effect == null) {
                    break;
                }

                player.addPotionEffect(effect);
            }
        }

        // Sets the player's healing item.
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            if (playerData.isUsingSoup()) {
                player.getInventory().addItem(new ItemBuilder(Material.MUSHROOM_SOUP).name("&fMushroom Stew").build());
            } else {
                player.getInventory().addItem(new ItemBuilder(Material.POTION).durability(16421).name("&fSplash Potion of Healing").build());
            }
        }

        // Sets the player's kit items.
        for (int i = 0; i < getItems().size(); ++i) {
            player.getInventory().setItem(i, getItems().get(i));
        }

        // Sets the player's armor.
        ItemStack helmet = getArmor()[0];
        ItemStack chestplate = getArmor()[1];
        ItemStack leggings = getArmor()[2];
        ItemStack boots = getArmor()[3];

        if (player.hasMetadata(protectionMetadata)) {
            if (helmet != null && helmet.getType() != Material.AIR) {
                helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (chestplate != null && chestplate.getType() != Material.AIR) {
                chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (leggings != null && leggings.getType() != Material.AIR) {
                leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (boots != null && boots.getType() != Material.AIR) {
                boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
        }

        if (player.hasMetadata(featherFallingMetadata)
                && boots != null && boots.getType() != Material.AIR) {
            boots.addEnchantment(Enchantment.PROTECTION_FALL, 4);
        }

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }
}

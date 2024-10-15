/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.kits;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Represents a kit.
 *
 * @author Foulest
 */
public interface Kit {

    /**
     * Gets the name of the kit.
     *
     * @return The name of the kit.
     */
    String getName();

    /**
     * Gets the display item of the kit.
     *
     * @return The display item of the kit.
     */
    ItemStack getDisplayItem();

    /**
     * Gets the items of the kit.
     *
     * @return The items of the kit.
     */
    List<ItemBuilder> getItems();

    /**
     * Gets the armor of the kit.
     *
     * @return The armor of the kit.
     */
    ItemBuilder[] getArmor();

    /**
     * Gets the potion effects of the kit.
     *
     * @return The potion effects of the kit.
     */
    PotionEffect[] getPotionEffects();

    /**
     * Gets the lore of the kit.
     *
     * @return The lore of the kit.
     */
    List<String> getLore();

    /**
     * Gets the enabled status of the kit.
     *
     * @return The enabled status of the kit.
     */
    boolean enabled();

    /**
     * Gets the cost of the kit.
     *
     * @return The cost of the kit.
     */
    int getCost();

    /**
     * Gets the permission of the kit.
     *
     * @return The permission of the kit.
     */
    Permission permission();

    /**
     * Applies a kit to a player.
     *
     * @param player The player to apply the kit to.
     */
    default void apply(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Collection<Integer> airSlots = new ArrayList<>();

        // Checks if the player owns the kit they're trying to equip.
        if (getCost() > 0 && !playerData.getOwnedKits().contains(this)) {
            MessageUtil.messagePlayer(player, "&cYou do not own the " + getName() + " kit.");
            return;
        }

        // Checks if the kit is enabled.
        if (!enabled()) {
            MessageUtil.messagePlayer(player, "&cThis kit is currently disabled.");
            return;
        }

        // Checks if the player has permission to use the kit.
        if (permission() != null
                && permission().getDefault() != PermissionDefault.TRUE
                && !player.hasPermission(permission())) {
            MessageUtil.messagePlayer(player, "&cYou do not have permission to use this kit.");
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
        playerData.setActiveKit(this);

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
            ItemBuilder itemBuilder = item;

            if (itemBuilder.getItem().getType().toString().toLowerCase(Locale.ROOT).contains("sword")
                    || itemBuilder.getItem().getType().toString().toLowerCase(Locale.ROOT).contains("cactus")
                    || itemBuilder.getItem().getType().toString().toLowerCase(Locale.ROOT).contains("axe")) {
                if (playerData.getEnchants().contains(Enchants.KNOCKBACK)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.KNOCKBACK, 2);
                }

                if (playerData.getEnchants().contains(Enchants.SHARPNESS)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.DAMAGE_ALL, 2);
                }
            }

            if (itemBuilder.getItem().getType().toString().toLowerCase(Locale.ROOT).contains("bow")) {
                if (playerData.getEnchants().contains(Enchants.PUNCH)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.ARROW_KNOCKBACK, 2);
                }

                if (playerData.getEnchants().contains(Enchants.POWER)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.ARROW_DAMAGE, 2);
                }
            }

            if (itemBuilder.getSlot() == 0) {
                player.getInventory().addItem(itemBuilder.getItem());
            } else {
                if (itemBuilder.getItem().getType() == Material.AIR) {
                    airSlots.add(itemBuilder.getSlot());
                }

                player.getInventory().setItem(itemBuilder.getSlot(), itemBuilder.getItem());
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

        // Sets the player's thorns enchantments.
        if (playerData.getEnchants().contains(Enchants.THORNS)) {
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

        // Sets the player's protection enchantments.
        if (playerData.getEnchants().contains(Enchants.PROTECTION)) {
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

        // Sets the player's feather falling enchantments.
        if (playerData.getEnchants().contains(Enchants.FEATHER_FALLING) && boots.getItem().getType() != Material.AIR) {
            boots = boots.enchant(Enchantment.PROTECTION_FALL, 4);
        }

        // Sets the player's armor.
        player.getInventory().setHelmet(helmet.getItem());
        player.getInventory().setChestplate(chestplate.getItem());
        player.getInventory().setLeggings(leggings.getItem());
        player.getInventory().setBoots(boots.getItem());

        // Sends the player a message and plays a sound.
        MessageUtil.messagePlayer(player, "&aYou equipped the " + getName() + " kit.");
        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
        player.updateInventory();
        player.closeInventory();
    }
}

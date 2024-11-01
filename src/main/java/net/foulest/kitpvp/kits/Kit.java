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
import net.foulest.kitpvp.listeners.FlaskListener;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

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
    default String getName() {
        return "Default";
    }

    /**
     * Gets the config path of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>
     *
     * @return The config path of the kit.
     */
    default String getConfigPath() {
        return "kitpvp.kits." + getName().toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the display item of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.display-item (String)
     *
     * @return The display item of the kit.
     */
    default ItemStack getDisplayItem() {
        String kitName = getName();
        String configPath = getConfigPath();
        String itemName = Settings.config.getString(configPath + ".display-item");
        Material material = Material.getMaterial(itemName);

        if (material == null) {
            MessageUtil.log(Level.WARNING, "Invalid display item for " + kitName + ": " + itemName);
            return new ItemBuilder(Material.BARRIER).hideInfo().getItem();
        }
        return new ItemBuilder(material).hideInfo().getItem();
    }

    /**
     * Gets the items of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.items.<item-name>.material (String)
     * - kitpvp.kits.<kit-name>.items.<item-name>.name (String)
     * - kitpvp.kits.<kit-name>.items.<item-name>.lore (String List)
     * - kitpvp.kits.<kit-name>.items.<item-name>.amount (Integer)
     * - kitpvp.kits.<kit-name>.items.<item-name>.slot (Integer)
     * - kitpvp.kits.<kit-name>.items.<item-name>.durability (Integer)
     * - kitpvp.kits.<kit-name>.items.<item-name>.enchants (String List) (Format: "ENCHANTMENT:LEVEL")
     * - kitpvp.kits.<kit-name>.items.<item-name>.unbreakable (Boolean)
     * - kitpvp.kits.<kit-name>.items.<item-name>.hide-info (Boolean)
     * - kitpvp.kits.<kit-name>.items.<item-name>.base64 (String) (Only for SKULL_ITEM)
     *
     * @return The items of the kit.
     */
    default List<ItemBuilder> getItems() {
        String kitName = getName();
        String configPath = getConfigPath();
        List<Map<?, ?>> itemsConfigList = Settings.config.getMapList(configPath + ".items");

        // Check if the config list is valid.
        if (itemsConfigList == null || itemsConfigList.isEmpty()) {
            MessageUtil.log(Level.WARNING, kitName + " items not found at path: " + configPath + ".items");
            return Collections.emptyList();
        }

        List<ItemBuilder> items = new ArrayList<>();

        // Get the items from the config.
        for (Map<?, ?> itemConfig : itemsConfigList) {
            String materialName = (String) itemConfig.get("material");
            Material material = Material.getMaterial(materialName);

            // Check if the material is valid.
            if (material == null) {
                MessageUtil.log(Level.WARNING, "Invalid material for " + kitName + "'s item: " + material);
                continue;
            }

            // Construct the item with the specified material.
            ItemBuilder item = new ItemBuilder(material);

            // Set the item's name.
            if (itemConfig.containsKey("name")) {
                String name = (String) itemConfig.get("name");
                item.name(name);
            }

            // Set the item's lore.
            if (itemConfig.containsKey("lore")) {
                List<String> lore = (List<String>) itemConfig.get("lore");
                item.lore(lore);
            }

            // Set the item's amount, if specified.
            if (itemConfig.containsKey("amount")) {
                int amount = (Integer) itemConfig.get("amount");
                item.amount(amount);
            }

            // Set the item's slot, if specified.
            if (itemConfig.containsKey("slot")) {
                int slot = (Integer) itemConfig.get("slot");
                item.slot(slot);
            }

            // Set the item's durability, if specified.
            if (itemConfig.containsKey("durability")) {
                int durability = (Integer) itemConfig.get("durability");
                item.durability((short) durability);
            }

            // Set the item's enchantments, if any.
            if (itemConfig.containsKey("enchants")) {
                List<String> enchants = (List<String>) itemConfig.get("enchants");

                for (String enchant : enchants) {
                    String[] enchantData = enchant.split(":");
                    Enchantment enchantment = Enchantment.getByName(enchantData[0]);
                    int level = Integer.parseInt(enchantData[1]);

                    if (enchantment == null) {
                        MessageUtil.log(Level.WARNING, "Invalid enchantment for " + kitName + "'s item: " + enchant);
                        continue;
                    }

                    item.enchant(enchantment, level);
                }
            }

            // Set the item's unbreakable status.
            boolean unbreakable = (Boolean) itemConfig.get("unbreakable");
            item.unbreakable(unbreakable);

            // Set the item's hide-info status, if specified.
            if (itemConfig.containsKey("hide-info")) {
                boolean hideInfo = (Boolean) itemConfig.get("hide-info");

                if (hideInfo) {
                    item.hideInfo();
                }
            }

            // Add the constructed item to the list.
            items.add(item);
        }
        return items;
    }

    /**
     * Gets the armor of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.material (String)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.name (String)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.color (String) (RGB, e.g. "0xFFFFFF") (Only for LEATHER_ARMOR)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.lore (String List)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.amount (Integer)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.slot (Integer)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.durability (Integer)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.unbreakable (Boolean)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.hide-info (Boolean)
     * - kitpvp.kits.<kit-name>.armor.<armor-piece>.base64 (String) (Only for SKULL_ITEM)
     *
     * @return The armor of the kit.
     */
    default List<ItemBuilder> getArmor() {
        String kitName = getName();
        String configPath = getConfigPath();
        ConfigurationSection config = Settings.config.getConfigurationSection(configPath + ".armor");

        // Check if the config is valid.
        if (config == null) {
            MessageUtil.log(Level.WARNING, kitName + " armor not found.");
            return Collections.emptyList();
        }

        List<ItemBuilder> armor = new ArrayList<>();

        // Get the items from the config.
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            String material = section.getString("material");

            // Check if the material is valid.
            if (Material.getMaterial(material) == null) {
                MessageUtil.log(Level.WARNING, "Invalid material for " + kitName + "'s armor: " + material);
                continue;
            }

            // Construct the item.
            ItemBuilder item = new ItemBuilder(Material.getMaterial(material));

            // Set the item's texture if it's a skull and has a base64 value.
            if (material.equals("SKULL_ITEM") && section.contains("base64")) {
                String base64 = section.getString("base64");

                if (base64 != null) {
                    item.setItem(SkullBuilder.itemFromBase64(base64));
                }
            }

            // Set the item's name.
            if (section.contains("name")) {
                String name = section.getString("name");
                item.name(name);
            }

            // Set the item's color.
            if (section.contains("color")) {
                String color = section.getString("color");

                // Remove the color prefix if present.
                color = color.replace("#", "");
                color = color.replace("0x", "");

                try {
                    // Parse the color hex string as a single integer value.
                    int hexColor = Integer.parseInt(color, 16);

                    // Set the color on the item.
                    item.color(Color.fromRGB(hexColor));
                } catch (NumberFormatException ex) {
                    MessageUtil.log(Level.WARNING, "Invalid color for " + kitName + "'s armor: " + color);
                }
            }

            // Set the item's lore if present, else assign an empty list.
            List<String> lore = section.getStringList("lore");
            item.lore(lore != null ? lore : Collections.emptyList());

            // Set the item's amount.
            if (section.contains("amount")) {
                int amount = section.getInt("amount");
                item.amount(amount);
            }

            // Set the item's slot.
            if (section.contains("slot")) {
                int slot = section.getInt("slot");
                item.slot(slot);
            }

            // Set the item's durability.
            if (section.contains("durability")) {
                int durability = section.getInt("durability");
                item.durability((short) durability);
            }

            // Set the item's unbreakable status.
            boolean unbreakable = section.getBoolean("unbreakable", false);
            item.unbreakable(unbreakable);

            // Set the item's hideInfo status.
            if (section.getBoolean("hide-info", false)) {
                item.hideInfo();
            }

            // Add the item to the armor list.
            armor.add(item);
        }
        return armor;
    }

    /**
     * Gets the potion effects of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.effects.<effect-name>.type (String)
     * - kitpvp.kits.<kit-name>.effects.<effect-name>.duration (Integer)
     * - kitpvp.kits.<kit-name>.effects.<effect-name>.amplifier (Integer)
     *
     * @return The potion effects of the kit.
     */
    default List<PotionEffect> getPotionEffects() {
        String kitName = getName();
        String configPath = getConfigPath();
        List<Map<?, ?>> effectsConfigList = Settings.config.getMapList(configPath + ".effects");

        // Check if the config list is valid.
        if (effectsConfigList == null || effectsConfigList.isEmpty()) {
            return Collections.emptyList();
        }

        List<PotionEffect> effects = new ArrayList<>();

        // Get each effect from the list.
        for (Map<?, ?> effectConfig : effectsConfigList) {
            PotionEffectType effectType;
            int duration = Integer.MAX_VALUE;
            int amplifier = 0;

            // Check for effect type.
            if (effectConfig.containsKey("type")) {
                String type = (String) effectConfig.get("type");
                effectType = PotionEffectType.getByName(type);

                if (effectType == null) {
                    MessageUtil.log(Level.WARNING, "Invalid effect type '" + type + "' for " + kitName + " effect.");
                    continue;
                }
            } else {
                MessageUtil.log(Level.WARNING, "Effect type not specified for one of " + kitName + "'s effects.");
                continue;
            }

            // Set the effect's duration, if provided.
            if (effectConfig.containsKey("duration")) {
                duration = (Integer) effectConfig.get("duration");
            }

            // Set the effect's amplifier, if provided.
            if (effectConfig.containsKey("amplifier")) {
                amplifier = (Integer) effectConfig.get("amplifier");
            }

            // Construct and add the effect.
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier, false, false);
            effects.add(effect);
        }
        return effects;
    }

    /**
     * Gets the lore of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.lore (String List)
     *
     * @return The lore of the kit.
     */
    default List<String> getLore() {
        String kitName = getName();
        String configPath = getConfigPath();
        List<String> lore = Settings.config.getStringList(configPath + ".lore");

        if (lore == null) {
            MessageUtil.log(Level.WARNING, kitName + " lore not found.");
            return Collections.emptyList();
        }
        return lore;
    }

    /**
     * Gets the enabled status of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.enabled (Boolean)
     *
     * @return The enabled status of the kit.
     */
    default boolean enabled() {
        String configPath = getConfigPath();
        return Settings.config.getBoolean(configPath + ".enabled");
    }

    /**
     * Gets the cost of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.cost (Integer)
     *
     * @return The cost of the kit.
     */
    default int getCost() {
        String configPath = getConfigPath();
        return Settings.config.getInt(configPath + ".cost");
    }

    /**
     * Gets the permission of the kit.
     * <p>
     * Default location(s):
     * - kitpvp.kits.<kit-name>.permission.name (String)
     * - kitpvp.kits.<kit-name>.permission.default (Boolean)
     *
     * @return The permission of the kit.
     */
    default Permission permission() {
        String configPath = getConfigPath();
        String permissionName = Settings.config.getString(configPath + ".permission.name");
        boolean defaultState = Settings.config.getBoolean(configPath + ".permission.default");
        return new Permission(permissionName, PermissionDefault.getByName(defaultState ? "TRUE" : "FALSE"));
    }

    /**
     * Applies a kit to a player.
     *
     * @param player The player to apply the kit to.
     */
    default void apply(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Collection<Integer> airSlots = new ArrayList<>();

        String kitName = getName();

        // Checks if the player owns the kit they're trying to equip.
        if (getCost() > 0 && !playerData.getOwnedKits().contains(this)) {
            MessageUtil.messagePlayer(player, "&cYou do not own the " + kitName + " kit.");
            return;
        }

        // Checks if the kit is enabled.
        if (!enabled()) {
            MessageUtil.messagePlayer(player, "&cThis kit is currently disabled.");
            return;
        }

        // Checks if the player has permission to use the kit.
        Permission permission = permission();
        if (permission != null
                && permission.getDefault() != PermissionDefault.TRUE
                && !player.hasPermission(permission)) {
            MessageUtil.messagePlayer(player, "&cYou do not have permission to use this kit.");
            return;
        }

        // Clears the player's inventory and armor.
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        // Clears the player's potion effects.
        for (PotionEffect effects : player.getActivePotionEffects()) {
            PotionEffectType effectType = effects.getType();
            player.removePotionEffect(effectType);
        }

        // Sets the player's kit data.
        playerData.setActiveKit(this);

        // Sets the player's potion effects.
        List<PotionEffect> effects = getPotionEffects();
        if (effects != null) {
            for (PotionEffect effect : effects) {
                if (effect == null) {
                    break;
                }

                player.addPotionEffect(effect);
            }
        }

        // Sets the player's kit items.
        for (ItemBuilder item : getItems()) {
            ItemBuilder itemBuilder = item;
            ItemStack itemStack = itemBuilder.getItem();
            int slot = itemBuilder.getSlot();

            if (itemStack.getType().toString().toLowerCase(Locale.ROOT).contains("sword")
                    || itemStack.getType().toString().toLowerCase(Locale.ROOT).contains("cactus")
                    || itemStack.getType().toString().toLowerCase(Locale.ROOT).contains("axe")) {
                if (playerData.getEnchants().contains(Enchants.KNOCKBACK)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.KNOCKBACK, 2);
                }

                if (playerData.getEnchants().contains(Enchants.SHARPNESS)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.DAMAGE_ALL, 2);
                }
            }

            if (itemStack.getType().toString().toLowerCase(Locale.ROOT).contains("bow")) {
                if (playerData.getEnchants().contains(Enchants.PUNCH)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.ARROW_KNOCKBACK, 2);
                }

                if (playerData.getEnchants().contains(Enchants.POWER)) {
                    itemBuilder = itemBuilder.enchant(Enchantment.ARROW_DAMAGE, 2);
                }
            }

            itemStack = itemBuilder.getItem();

            if (slot == 0) {
                player.getInventory().addItem(itemStack);
            } else {
                if (itemStack.getType() == Material.AIR) {
                    airSlots.add(slot);
                }

                player.getInventory().setItem(slot, itemStack);
            }
        }

        // Sets the player's healing item.
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            if (airSlots.contains(i) || player.getInventory().getItem(i) != null) {
                continue;
            }

            // Set the flask item.
            if (Settings.flaskEnabled) {
                ItemStack flaskItem = FlaskListener.FLASK;
                flaskItem.setAmount(Settings.flaskAmount);
                player.getInventory().setItem(i, flaskItem);
                break;
            } else {
                if (playerData.isUsingSoup()) {
                    ItemBuilder soupBuilder = new ItemBuilder(Material.MUSHROOM_SOUP).name("&fMushroom Stew");
                    ItemStack soupItem = soupBuilder.getItem();
                    player.getInventory().setItem(i, soupItem);
                } else {
                    ItemBuilder potionBuilder = new ItemBuilder(Material.POTION).durability(16421).name("&fSplash Potion of Healing");
                    ItemStack potionItem = potionBuilder.getItem();
                    player.getInventory().setItem(i, potionItem);
                }
            }
        }

        // Sets the player's armor.
        List<ItemBuilder> armor = getArmor();
        ItemBuilder helmet = (armor.get(0) == null ? new ItemBuilder(Material.AIR) : armor.get(0));
        ItemBuilder chestplate = (armor.get(1) == null ? new ItemBuilder(Material.AIR) : armor.get(1));
        ItemBuilder leggings = (armor.get(2) == null ? new ItemBuilder(Material.AIR) : armor.get(2));
        ItemBuilder boots = (armor.get(3) == null ? new ItemBuilder(Material.AIR) : armor.get(3));

        // Sets the player's thorns enchantments.
        ItemStack helmetItem = helmet.getItem();
        ItemStack chestplateItem = chestplate.getItem();
        ItemStack leggingsItem = leggings.getItem();
        ItemStack bootsItem = boots.getItem();

        if (playerData.getEnchants().contains(Enchants.THORNS)) {
            if (helmetItem.getType() != Material.AIR && helmetItem.getType() != Material.SKULL_ITEM) {
                helmet = helmet.enchant(Enchantment.THORNS, 2);
            }

            if (chestplateItem.getType() != Material.AIR) {
                chestplate = chestplate.enchant(Enchantment.THORNS, 2);
            }

            if (leggingsItem.getType() != Material.AIR) {
                leggings = leggings.enchant(Enchantment.THORNS, 2);
            }

            if (bootsItem.getType() != Material.AIR) {
                boots = boots.enchant(Enchantment.THORNS, 2);
            }
        }

        // Sets the player's protection enchantments.
        if (playerData.getEnchants().contains(Enchants.PROTECTION)) {
            if (helmetItem.getType() != Material.AIR && helmetItem.getType() != Material.SKULL_ITEM) {
                helmet = helmet.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (chestplateItem.getType() != Material.AIR) {
                chestplate = chestplate.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (leggingsItem.getType() != Material.AIR) {
                leggings = leggings.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }

            if (bootsItem.getType() != Material.AIR) {
                boots = boots.enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
        }

        // Sets the player's feather falling enchantments.
        if (playerData.getEnchants().contains(Enchants.FEATHER_FALLING) && bootsItem.getType() != Material.AIR) {
            boots = boots.enchant(Enchantment.PROTECTION_FALL, 4);
        }

        helmetItem = helmet.getItem();
        chestplateItem = chestplate.getItem();
        leggingsItem = leggings.getItem();
        bootsItem = boots.getItem();

        // Sets the player's armor.
        player.getInventory().setHelmet(helmetItem);
        player.getInventory().setChestplate(chestplateItem);
        player.getInventory().setLeggings(leggingsItem);
        player.getInventory().setBoots(bootsItem);

        // Sends the player a message and plays a sound.
        MessageUtil.messagePlayer(player, "&aYou equipped the " + kitName + " kit.");
        Location location = player.getLocation();
        player.playSound(location, Sound.SLIME_WALK, 1, 1);
        player.updateInventory();
        player.closeInventory();
    }
}

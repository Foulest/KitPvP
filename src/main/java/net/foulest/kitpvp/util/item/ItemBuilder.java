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
package net.foulest.kitpvp.util.item;

import lombok.Data;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom ItemBuilder class that allows for easy item creation.
 *
 * @author nonamesldev
 * @see <a href="https://www.spigotmc.org/threads/util-itembuilder-manage-items-easily.48397">Spigot Thread</a>
 */
@Data
public class ItemBuilder {

    private final ItemStack item;
    private int slot;

    /**
     * Creates a new ItemBuilder with the given Material.
     *
     * @param material The Material to create the ItemBuilder with.
     */
    public ItemBuilder(Material material) {
        item = new ItemStack(material);
    }

    /**
     * Creates a new ItemBuilder with the given ItemStack.
     *
     * @param itemStack The ItemStack to create the ItemBuilder with.
     */
    public ItemBuilder(ItemStack itemStack) {
        item = new ItemStack(itemStack);
    }

    /**
     * Sets the name of the item.
     *
     * @param name The name to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.colorize(name));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Sets the slot of the item.
     *
     * @param slot The slot to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder slot(int slot) {
        this.slot = slot;
        return this;
    }

    /**
     * Sets the unbreakable status of the item.
     *
     * @param status The status to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder unbreakable(boolean status) {
        ItemMeta meta = item.getItemMeta();

        if (item.getType() == Material.AIR) {
            return this;
        }

        meta.spigot().setUnbreakable(status);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Adds a glow effect to the item.
     *
     * @return The ItemBuilder instance.
     */
    public ItemBuilder addGlow() {
        enchant(Enchantment.WATER_WORKER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Hides the item's information.
     *
     * @return The ItemBuilder instance.
     */
    public ItemBuilder hideInfo() {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The lore to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder lore(String lore) {
        List<String> itemLore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        itemLore.add(MessageUtil.colorize(lore));
        lore(itemLore);
        return this;
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The lore to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder lore(@NotNull Iterable<String> lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> loreList = new ArrayList<>();

        for (String str : lore) {
            loreList.add(MessageUtil.colorize(str));
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Clears the lore of the item.
     *
     * @return The ItemBuilder instance.
     */
    public ItemBuilder clearLore() {
        lore(Collections.emptyList());
        return this;
    }

    /**
     * Clears the enchantments of the item.
     *
     * @return The ItemBuilder instance.
     */
    public ItemBuilder clearEnchantments() {
        if (item.getItemMeta().hasEnchants()) {
            for (Enchantment enchantments : item.getEnchantments().keySet()) {
                item.removeEnchantment(enchantments);
            }
        }
        return this;
    }

    /**
     * Clears the specified enchantment of the item.
     *
     * @param enchantment The enchantment to clear.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder clearEnchantment(Enchantment enchantment) {
        ItemMeta meta;

        if (item.getItemMeta().hasEnchants() && item.getEnchantments().containsKey(enchantment)) {
            meta = item.getItemMeta();
            meta.removeEnchant(enchantment);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment The enchantment to add.
     * @param level       The level of the enchantment.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (item.containsEnchantment(enchantment)) {
            int enchantmentLevel = item.getEnchantmentLevel(enchantment);
            item.addUnsafeEnchantment(enchantment, level + enchantmentLevel);
        } else {
            item.addUnsafeEnchantment(enchantment, level);
        }
        return this;
    }

    /**
     * Sets the amount of the item.
     *
     * @param amount The amount to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount > 0 ? amount : 1);
        return this;
    }

    /**
     * Sets the durability of the item.
     *
     * @param durability The durability to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder durability(int durability) {
        item.setDurability((short) durability);
        return this;
    }

    /**
     * Sets the color of the item.
     *
     * @param color The color to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder color(Color color) {
        LeatherArmorMeta meta;

        if (item.getType().toString().contains("LEATHER_")) {
            meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return this;
    }
}

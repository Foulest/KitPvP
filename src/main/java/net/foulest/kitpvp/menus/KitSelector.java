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
package net.foulest.kitpvp.menus;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the Kit Selector GUI.
 *
 * @author Foulest
 */
public class KitSelector {

    private final String inventoryName = MessageUtil.colorize("Kit Selector");
    private static final Map<Player, Integer> pages = new HashMap<>();
    private final Inventory inventory;

    /**
     * Creates a new instance of the Kit Selector GUI.
     *
     * @param player The player to open the GUI for.
     */
    public KitSelector(Player player) {
        inventory = Bukkit.createInventory(player, ensureSize(KitManager.getKits().size()) + 18, inventoryName);

        populateInventory(player, 0);
        player.closeInventory();
        player.openInventory(inventory);
        pages.put(player, 0);
    }

    /**
     * Creates a new instance of the Kit Selector GUI.
     *
     * @param player The player to open the GUI for.
     * @param page   The page to open the GUI on.
     */
    public KitSelector(Player player, int page) {
        inventory = Bukkit.createInventory(player, ensureSize(KitManager.getKits().size()) + 18,
                inventoryName + " - Page: " + (page + 1));

        populateInventory(player, page);
        player.closeInventory();
        player.openInventory(inventory);
        pages.put(player, page);
    }

    /**
     * Gets the page the GUI was opened on.
     *
     * @param player The player to open the GUI for.
     * @return The page the GUI was opened on.
     */
    public static int getPage(Player player) {
        return pages.get(player);
    }

    /**
     * Ensures that we use enough slots to hold all the kit items.
     *
     * @param size The size of the inventory.
     */
    private static int ensureSize(int size) {
        int maxSize = 36;
        int halfMaxSize = 18;
        int rowSize = 9;

        if (size >= maxSize) {
            return maxSize;
        }

        if ((size + halfMaxSize) % rowSize == 0) {
            return size;
        }
        ++size;
        return ensureSize(size);
    }

    /**
     * Creates a kit item.
     *
     * @param kit The kit to create an item for.
     * @return The kit item.
     */
    private static ItemStack createKitItem(@NotNull Kit kit) {
        List<String> lore = kit.getLore();

        if (kit.getCost() == 0) {
            lore.add(1, "&7Cost: &fFree");
        } else {
            lore.add(1, "&7Cost: &f" + kit.getCost() + " coins");
        }

        lore.add("&7");
        lore.add("&aClick to equip this kit.");
        return new ItemBuilder(kit.getDisplayItem()).name("&a" + kit.getName()).lore(lore).getItem();
    }

    /**
     * Populates the GUI's inventory.
     *
     * @param player The player to open the GUI for.
     * @param page   The page to open the GUI on.
     */
    private void populateInventory(Player player, int page) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int kitsPerPage = 36; // Assuming 36 kits per page
        int rowSize = 9;

        // Sets non-present items to glass.
        for (int i = 0; i < rowSize; i++) {
            inventory.setItem(i, glass);
        }
        for (int i = (inventory.getSize() - rowSize); i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        // Previous page item
        if (page > 0) {
            inventory.setItem(inventory.getSize() - 9, new ItemBuilder(Material.BOOK).name("&aPrevious Page").getItem());
        }

        int start = page * kitsPerPage;
        int end = Math.min((page + 1) * kitsPerPage, KitManager.getKits().size());

        // Next page item
        if (end < KitManager.getKits().size()) { // Correct check for the existence of a next page
            inventory.setItem(inventory.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").getItem());
        }

        List<Kit> pageKits = KitManager.getKits().subList(start, end);

        // Sort kits alphabetically
        List<Kit> sortedKits = new ArrayList<>(pageKits);
        sortedKits.sort(Comparator.comparing(Kit::getName));

        // Add sorted kits in alphabetical order
        for (Kit kit : sortedKits) {
            if (kit.getCost() == 0 || playerData.getOwnedKits().contains(kit)) {
                inventory.addItem(createKitItem(kit));
            }
        }
    }
}

package net.foulest.kitpvp.menus;

import lombok.NonNull;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.kits.Kit;
import net.foulest.kitpvp.util.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Inventory for Kit Selector
 */
public class KitSelector {

    private static final String inventoryName = MessageUtil.colorize("Kit Selector");
    private static final Map<Player, Integer> pages = new HashMap<>();
    private final Inventory inventory;

    public KitSelector(@NonNull Player player) {
        inventory = Bukkit.createInventory(player, ensureSize(KitManager.kits.size()) + 18, inventoryName);

        populateInventory(player, 0);
        player.closeInventory();
        player.openInventory(inventory);
        pages.put(player, 0);
    }

    public KitSelector(@NonNull Player player, int page) {
        inventory = Bukkit.createInventory(player, ensureSize(KitManager.kits.size()) + 18, inventoryName + " - Page: " + (page + 1));

        populateInventory(player, page);
        player.closeInventory();
        player.openInventory(inventory);
        pages.put(player, page);
    }

    public static int getPage(@NonNull Player player) {
        return pages.get(player);
    }

    /**
     * Ensures that we use enough slots to hold all the kit items.
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
        return ensureSize(++size);
    }

    private static int ensureKits(int size) {
        return (Math.min(size, 36));
    }

    private static ItemStack createKitItem(Kit kit) {
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
     */
    private void populateInventory(@NonNull Player player, int page) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int rowSize = 9;

        // Sets non-present items to glass.
        for (int i = 0; i < rowSize; i++) {
            inventory.setItem(i, glass);
        }
        for (int i = (inventory.getSize() - rowSize); i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        if (page > 0) {
            inventory.setItem(inventory.getSize() - 9, new ItemBuilder(Material.BOOK).name("&aPrevious Page").getItem());
        }

        List<Kit> checkedKits = KitManager.kits.subList(page * 36, (page * 36) + ensureKits(KitManager.kits.size() - (page * 36)));

        try {
            List<Kit> futureCheck = KitManager.kits.subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(KitManager.kits.size() - ((page + 1) * 36)));

            if (!futureCheck.isEmpty()) {
                inventory.setItem(inventory.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").getItem());
            }
        } catch (IllegalArgumentException ignored) {
        }

        // Sort kits alphabetically using the kits.getName() function
        List<Kit> sortedKits = new ArrayList<>(checkedKits);
        sortedKits.sort(Comparator.comparing(Kit::getName));

        // Add sorted kits in alphabetical order
        for (Kit kit : sortedKits) {
            if (playerData.getOwnedKits().contains(kit)) {
                inventory.addItem(createKitItem(kit));
            }
        }
    }
}

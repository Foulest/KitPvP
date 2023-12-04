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
 * Inventory for Kit Shop
 */
public class KitShop {

    private static final String inventoryName = MessageUtil.colorize("Kit Shop");
    private static final Map<Player, Integer> pages = new HashMap<>();
    private final Inventory inventory;

    public KitShop(@NonNull Player player) {
        this(player, 0);
    }

    public KitShop(@NonNull Player player, int page) {
        player.closeInventory();

        if (page > 0) {
            inventory = Bukkit.createInventory(player, ensureSize(KitManager.kits.size()) + 18, inventoryName + " - Page: " + (page + 1));
        } else {
            inventory = Bukkit.createInventory(player, ensureSize(KitManager.kits.size()) + 18, inventoryName);
        }

        if (populateInventory(player, page)) {
            player.openInventory(inventory);
            pages.put(player, page);
        } else {
            MessageUtil.messagePlayer(player, "&cYou own all of the kits.");
        }
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

    private static ItemStack createKitItem(@NonNull Kit kit) {
        List<String> lore = kit.getLore();

        if (kit.getCost() == 0) {
            lore.add(1, "&7Cost: &fFree");
        } else {
            lore.add(1, "&7Cost: &f" + kit.getCost() + " coins");
        }

        lore.add("");
        lore.add("&aClick to purchase this kit.");
        return new ItemBuilder(kit.getDisplayItem()).name("&c" + kit.getName()).lore(lore).getItem();
    }

    private boolean populateInventory(@NonNull Player player, int page) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int paidKits = 0;
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

        // Future kits check
        List<Kit> checkedKits = KitManager.kits.subList(page * 36, (page * 36) + ensureKits(KitManager.kits.size() - (page * 36)));

        // Next page item
        try {
            List<Kit> futureCheck = KitManager.kits.subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(KitManager.kits.size() - ((page + 1) * 36)));

            if (!futureCheck.isEmpty()) {
                inventory.setItem(inventory.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").getItem());
            }
        } catch (IllegalArgumentException ex) {
            // ignored
        }

        // Sort kits alphabetically using the kits.getName() function
        List<Kit> sortedKits = new ArrayList<>(checkedKits);
        sortedKits.sort(Comparator.comparing(Kit::getName));

        // Add sorted kits in alphabetical order
        for (Kit kits : sortedKits) {
            if (!playerData.getOwnedKits().contains(kits)) {
                inventory.addItem(createKitItem(kits));
                paidKits++;
            }
        }
        return paidKits != 0;
    }
}

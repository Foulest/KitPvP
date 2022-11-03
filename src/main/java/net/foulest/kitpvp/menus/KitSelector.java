package net.foulest.kitpvp.menus;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.kits.Kit;
import net.foulest.kitpvp.util.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Inventory for Kit Selector
 */
public class KitSelector {

    private static final String inventoryName = MessageUtil.colorize("Kit Selector");
    private static final Map<Player, Integer> pages = new HashMap<>();
    private final Inventory inv;

    public KitSelector(Player player) {
        inv = Bukkit.createInventory(player, ensureSize(KitManager.kits.size()) + 18, inventoryName);

        populateInventory(player, 0);
        player.closeInventory();
        player.openInventory(inv);
        pages.put(player, 0);
    }

    public KitSelector(Player player, int page) {
        inv = Bukkit.createInventory(player, ensureSize(KitManager.kits.size()) + 18, inventoryName + " - Page: " + (page + 1));

        populateInventory(player, page);
        player.closeInventory();
        player.openInventory(inv);
        pages.put(player, page);
    }

    public static int getPage(Player player) {
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
    private void populateInventory(Player player, int page) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int rowSize = 9;

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        // Sets non-present items to glass.
        for (int i = 0; i < rowSize; i++) {
            inv.setItem(i, glass);
        }
        for (int i = (inv.getSize() - rowSize); i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        if (page > 0) {
            inv.setItem(inv.getSize() - 9, new ItemBuilder(Material.BOOK).name("&aPrevious Page").getItem());
        }

        List<Kit> checkedKits = KitManager.kits.subList(page * 36, (page * 36) + ensureKits(KitManager.kits.size() - (page * 36)));

        try {
            List<Kit> futureCheck = KitManager.kits.subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(KitManager.kits.size() - ((page + 1) * 36)));

            if (!futureCheck.isEmpty()) {
                inv.setItem(inv.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").getItem());
            }
        } catch (IllegalArgumentException ex) {
            // ignored
        }

        // TODO: Sort alphabetically
        for (Kit kits : checkedKits) {
            if (playerData.getOwnedKits().contains(kits)) {
                inv.addItem(createKitItem(kits));
            }
        }
    }

    public Inventory getInventory() {
        return inv;
    }
}

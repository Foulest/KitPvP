package net.foulest.kitpvp.utils.menus;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
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
 * @created 02/18/2021
 * @project KitPvP
 */
public class KitSelector {

    public static final String INVENTORY_NAME = MessageUtil.colorize("Kit Selector");
    private static final Map<Player, Integer> PAGE = new HashMap<>();
    private static final KitManager KIT_MANAGER = KitManager.getInstance();
    private final Inventory inv;

    public KitSelector(Player player) {
        inv = Bukkit.createInventory(player, ensureSize(KIT_MANAGER.getKits().size()) + 18, INVENTORY_NAME);

        populateInventory(player, 0);
        player.closeInventory();
        player.openInventory(inv);
        PAGE.put(player, 0);
    }

    public KitSelector(Player player, int page) {
        inv = Bukkit.createInventory(player, ensureSize(KIT_MANAGER.getKits().size()) + 18, INVENTORY_NAME + " - Page: " + (page + 1));

        populateInventory(player, page);
        player.closeInventory();
        player.openInventory(inv);
        KitSelector.PAGE.put(player, page);
    }

    public static int getPage(Player player) {
        return PAGE.get(player);
    }

    /**
     * Ensures that we use enough slots to hold all the kit items.
     */
    private int ensureSize(int size) {
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

    /**
     * Populates the GUI's inventory.
     */
    private void populateInventory(Player player, int page) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int rowSize = 9;

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

        List<Kit> checkedKits = KIT_MANAGER.getKits().subList(page * 36, (page * 36) + ensureKits(KIT_MANAGER.getKits().size() - (page * 36)));

        try {
            List<Kit> futureCheck = KIT_MANAGER.getKits().subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(KIT_MANAGER.getKits().size() - ((page + 1) * 36)));

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

    private int ensureKits(int size) {
        return (Math.min(size, 36));
    }

    private ItemStack createKitItem(Kit kit) {
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
}

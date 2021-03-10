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
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class KitShop {

    public static final String INVENTORY_NAME = MessageUtil.colorize("Kit Shop");
    private static final Map<Player, Integer> PAGE = new HashMap<>();
    private static final KitManager KIT_MANAGER = KitManager.getInstance();
    private final Inventory inv;

    public KitShop(Player player) {
        this(player, 0);
    }

    public KitShop(Player player, int page) {
        player.closeInventory();

        if (page > 0) {
            inv = Bukkit.createInventory(player, ensureSize(KIT_MANAGER.getKits().size()) + 18, INVENTORY_NAME + " - Page: " + (page + 1));
        } else {
            inv = Bukkit.createInventory(player, ensureSize(KIT_MANAGER.getKits().size()) + 18, INVENTORY_NAME);
        }

        if (populateInventory(player, page)) {
            player.openInventory(inv);
            KitShop.PAGE.put(player, page);
        } else {
            MessageUtil.messagePlayer(player, "&cYou own all of the kits.");
        }
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

    private boolean populateInventory(Player player, int page) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").getItem();
        int paidKits = 0;
        int rowSize = 9;

        // Sets non-present items to glass.
        for (int i = 0; i < rowSize; i++) {
            inv.setItem(i, glass);
        }
        for (int i = (inv.getSize() - rowSize); i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Previous page item
        if (page > 0) {
            inv.setItem(inv.getSize() - 9, new ItemBuilder(Material.BOOK).name("&aPrevious Page").getItem());
        }

        // Future kits check
        List<Kit> checkedKits = KIT_MANAGER.getKits().subList(page * 36, (page * 36) + ensureKits(KIT_MANAGER.getKits().size() - (page * 36)));

        // Next page item
        try {
            List<Kit> futureCheck = KIT_MANAGER.getKits().subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(KIT_MANAGER.getKits().size() - ((page + 1) * 36)));

            if (!futureCheck.isEmpty()) {
                inv.setItem(inv.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").getItem());
            }
        } catch (IllegalArgumentException ex) {
            // ignored
        }

        for (Kit kits : checkedKits) {
            if (!playerData.getOwnedKits().contains(kits)) {
                inv.addItem(createKitItem(kits));
                paidKits++;
            }
        }

        return paidKits != 0;
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

        lore.add("");
        lore.add("&aClick to purchase this kit.");
        return new ItemBuilder(kit.getDisplayItem()).name("&c" + kit.getName()).lore(lore).getItem();
    }
}

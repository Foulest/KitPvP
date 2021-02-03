package net.foulest.kitpvp.utils;

import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class KitShop {

    public static final String INVENTORY_NAME = MiscUtils.colorize("Kit Shop");
    private static final Map<Player, Integer> page = new HashMap<>();
    private final Inventory inv;
    private final KitManager kitManager = KitManager.getInstance();

    public KitShop(Player player) {
        this(player, 0);
    }

    public KitShop(Player player, int page) {
        player.closeInventory();

        if (page > 0) {
            inv = Bukkit.createInventory(player, ensureSize(kitManager.getKits().size()) + 18, INVENTORY_NAME + " - Page: " + (page + 1));
        } else {
            inv = Bukkit.createInventory(player, ensureSize(kitManager.getKits().size()) + 18, INVENTORY_NAME);
        }

        if (populateInventory(player, page)) {
            player.openInventory(inv);
            KitShop.page.put(player, page);
        } else {
            MiscUtils.messagePlayer(player, "&cYou own all of the kits.");
        }
    }

    // Ensures that we use enough slots to hold all the kit items.
    private int ensureSize(int size) {
        if (size >= 36) {
            return 36;
        }

        if ((size + 18) % 9 == 0) {
            return size;
        }

        return ensureSize(++size);
    }

    private boolean populateInventory(Player player, int page) {
        int paidKits = 0;
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build();

        // Sets non-present items to glass.
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }
        for (int i = (inv.getSize() - 9); i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Previous page item
        if (page > 0) {
            inv.setItem(inv.getSize() - 9, new ItemBuilder(Material.BOOK).name("&aPrevious Page").build());
        }

        // Future kits check
        List<Kit> checkedKits = kitManager.getKits().subList(page * 36, (page * 36) + ensureKits(kitManager.getKits().size() - (page * 36)));

        // Next page item
        try {
            List<Kit> futureCheck = kitManager.getKits().subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(kitManager.getKits().size() - ((page + 1) * 36)));

            if (!futureCheck.isEmpty()) {
                inv.setItem(inv.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").build());
            }
        } catch (IllegalArgumentException ignored) {
        }

        for (Kit kits : checkedKits) {
            if (!playerData.ownsKit(kits)) {
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
        List<String> lore = new ArrayList<>();
        lore.add("&7Attack: &f" + kit.getAttack());
        lore.add("&7Defense: &f" + kit.getDefense());
        lore.add("&7Cost: &f" + kit.getCost() + " coins");
        lore.add("");
        lore.add("&f" + kit.getDescription());
        lore.add("");
        lore.add("&aClick to purchase this kit.");
        return new ItemBuilder(kit.getDisplayItem()).name("&c" + kit.getName()).lore(lore).build();
    }
}

package net.foulest.kitpvp.utils.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitSelector {

    public static final String INVENTORY_NAME = MiscUtils.colorize("Kit Selector");
    private static final Map<Player, Integer> page = new HashMap<>();
    private final Inventory inv;
    private final KitManager kitManager = KitManager.getInstance();

    public KitSelector(Player player) {
        inv = Bukkit.createInventory(player, ensureSize(kitManager.getKits().size()) + 18, INVENTORY_NAME);

        populateInventory(player, 0);
        player.closeInventory();
        player.openInventory(inv);
        page.put(player, 0);
    }

    public KitSelector(Player player, int page) {
        inv = Bukkit.createInventory(player, ensureSize(kitManager.getKits().size()) + 18, INVENTORY_NAME + " - Page: " + (page + 1));

        populateInventory(player, page);
        player.closeInventory();
        player.openInventory(inv);
        KitSelector.page.put(player, page);
    }

    public static int getPage(Player player) {
        return page.get(player);
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

    // Populates the GUI's inventory.
    private void populateInventory(Player player, int page) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build();

        // Sets non-present items to glass.
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }
        for (int i = (inv.getSize() - 9); i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        if (page > 0) {
            inv.setItem(inv.getSize() - 9, new ItemBuilder(Material.BOOK).name("&aPrevious Page").build());
        }

        List<Kit> checkedKits = kitManager.getKits().subList(page * 36, (page * 36) + ensureKits(kitManager.getKits().size() - (page * 36)));

        try {
            List<Kit> futureCheck = kitManager.getKits().subList((page + 1) * 36, ((page + 1) * 36) + ensureKits(kitManager.getKits().size() - ((page + 1) * 36)));

            if (!futureCheck.isEmpty()) {
                inv.setItem(inv.getSize() - 1, new ItemBuilder(Material.BOOK).name("&aNext Page").build());
            }
        } catch (IllegalArgumentException ignored) {
        }

        List<Kit> kitsOrderedByPrice = new ArrayList<>();

        for (Kit kits : checkedKits) {
            if (playerData.ownsKit(kits) && (kitsOrderedByPrice.isEmpty()
                    || kits.getCost() > kitsOrderedByPrice.get(0).getCost())) {
                kitsOrderedByPrice.add(0, kits);
            } else if (kits.getCost() == 0) {
                kitsOrderedByPrice.add(kits);
            }
        }

        for (Kit kits : kitsOrderedByPrice) {
            inv.addItem(createKitItem(kits));
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
        return new ItemBuilder(kit.getDisplayItem()).name("&a" + kit.getName()).lore(lore).build();
    }
}

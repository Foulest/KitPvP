package net.foulest.kitpvp.utils;

import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class KitUpgrades {

    public static final String INVENTORY_NAME = MiscUtils.colorize("Kit Upgrades");
    private final Inventory inv;
    private final KitManager kitManager = KitManager.getInstance();

    public KitUpgrades(Player player) {
        inv = Bukkit.createInventory(player, 36, INVENTORY_NAME);

        populateInventory(player);
        player.closeInventory();
        player.openInventory(inv);
    }

    // Populates the GUI's inventory.
    private void populateInventory(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build();

        // Sets non-present items to glass.
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }
        for (int i = (inv.getSize() - 9); i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // TODO: Add the kit upgrade items (enchants, potions, etc.)
    }

    public Inventory getInventory() {
        return inv;
    }

//    private ItemStack createKitItem(Kit kit) {
//        List<String> lore = new ArrayList<>();
//        lore.add("&7Attack: &f" + kit.getAttack());
//        lore.add("&7Defense: &f" + kit.getDefense());
//        lore.add("");
//        lore.add("&f" + kit.getDescription());
//        lore.add("");
//        lore.add("&aClick to equip this kit.");
//        return new ItemBuilder(kit.getDisplayItem()).name("&a" + kit.getName()).lore(lore).build();
//    }
}

package net.foulest.kitpvp.utils;

import net.foulest.kitpvp.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class StaffMode {

    private static final StaffMode instance = new StaffMode();
    private final Spawn spawn = Spawn.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();

    public static StaffMode getInstance() {
        return instance;
    }

    public void toggleStaffMode(Player player, boolean status, boolean silent) {
        PlayerData playerData = PlayerData.getInstance(player);

        // Error handling.
        if (playerData.isInStaffMode() == status) {
            MiscUtils.messagePlayer(player, "&cSomething is very wrong with StaffMode.");
            return;
        }

        // Changes the staff mode status.
        playerData.setStaffMode(status);

        // Enables staff mode.
        if (playerData.isInStaffMode()) {
            if (!silent) {
                MiscUtils.messagePlayer(player, "&aStaff mode has been enabled.");
            }

            // Enables vanish.
            player.setMetadata("vanished", new FixedMetadataValue(kitPvP, true));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("kitpvp.staff")) {
                    p.hidePlayer(player);
                }
            }

            player.setGameMode(GameMode.CREATIVE);

            playerData.setKit(null);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            player.updateInventory();

            ItemStack randomTeleport = new ItemBuilder(Material.COMPASS).name("&aRandom Teleport &7(Right Click)").build();
            player.getInventory().setItem(0, randomTeleport);

            ItemStack exitStaffMode = new ItemBuilder(Material.BED).name("&cExit Staff Mode &7(Right Click)").build();
            player.getInventory().setItem(8, exitStaffMode);

        } else {
            // Disables staff mode.
            if (!silent) {
                MiscUtils.messagePlayer(player, "&cStaff mode has been disabled.");
            }

            // Teleports the player to spawn.
            spawn.teleport(player);
            player.setGameMode(GameMode.ADVENTURE);

            // Disables vanish.
            player.removeMetadata("vanished", kitPvP);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
        }
    }
}

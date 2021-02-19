package net.foulest.kitpvp.listeners;

import net.foulest.fstaff.staffmode.StaffMode;
import net.foulest.fstaff.staffmode.event.StaffModeEvent;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class StaffModeListener implements Listener {

    private final Spawn spawn = Spawn.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStaffMode(StaffModeEvent event) {
        StaffMode staffMode = event.getStaffMode();
        Player player = Bukkit.getPlayer(staffMode.getUUID());
        PlayerData playerData = PlayerData.getInstance(player);

        if (staffMode.enteredStaffMode()) {
            playerData.setKit(null);
        } else {
            spawn.teleport(player);
        }
    }
}

/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.listeners.kits;

import lombok.Data;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.type.Ninja;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

@Data
public class NinjaListener implements Listener {

    /**
     * Handles Ninjas damaging players with their blade.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onNinjaBladeHit(@NotNull EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Checks if the entities are both players.
        if (!(damagerEntity instanceof Player)
                || !(targetEntity instanceof Player)) {
            return;
        }

        Player player = (Player) damagerEntity;
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Ignores hits from players who aren't Ninjas.
        if (!(playerData.getActiveKit() instanceof Ninja)) {
            return;
        }

        // Ignores hits that aren't with Ninja's Blade.
        if (player.getItemInHand() == null
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Ninja's Blade")) {
            return;
        }

        Player target = (Player) targetEntity;
        Location playerEyeLoc = player.getEyeLocation();
        Location targetEyeLoc = target.getEyeLocation();
        Location targetLoc = target.getLocation();

        int playerYaw = (int) playerEyeLoc.getYaw();
        int targetYaw = (int) targetEyeLoc.getYaw();

        // Normalize yaw values.
        if (playerYaw < 0) {
            playerYaw += 360;
        }
        if (targetYaw < 0) {
            targetYaw += 360;
        }

        // Calculate the yaw difference.
        int difference = Math.abs(playerYaw - targetYaw);

        // The player is behind the target; apply 50% damage increase.
        if (difference <= 90) {
            target.getWorld().playSound(targetLoc, Sound.BAT_HURT, 1, 1);
            double damage = event.getDamage();
            event.setDamage(damage * 1.5);
        }
    }
}

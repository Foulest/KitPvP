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
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.Vampire;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.AbilityUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

@Data
public class VampireListener implements Listener {

    /**
     * Handles the Vampire ability.
     *
     * @param event The event.
     */
    @EventHandler
    @SuppressWarnings("NestedMethodCall")
    public static void onVampireAbility(@NotNull PlayerInteractEvent event) {
        // Ignores the event if the player isn't right-clicking.
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();
        ItemStack item = event.getItem();

        // Checks for common ability exclusions.
        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit, item, Material.REDSTONE)) {
            return;
        }

        // Create a task to notify the player that the Life Steal ability has ended.
        BukkitTask lifeStealTask = new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(playerLoc, Sound.BAT_IDLE, 1, 1);
                MessageUtil.messagePlayer(player, "&cLife Steal is no longer active.");
            }
        }.runTaskLater(KitPvP.instance, Settings.vampireKitDuration * 20L);

        // Cancel the Life Steal cooldown task if it's active.
        TaskUtil.runTaskLater(() -> playerData.setLifeStealCooldown(null), Settings.vampireKitDuration * 20L);

        // Set the Life Steal cooldown task.
        playerData.setLifeStealCooldown(lifeStealTask);

        // Sets the player's ability cooldown.
        player.playSound(playerLoc, Sound.BAT_HURT, 1, 1);
        MessageUtil.messagePlayer(player, "&aLife Steal has been activated.");
        playerData.setCooldown(playerKit, Settings.vampireKitCooldown, true);
    }

    /**
     * Handles the Life Steal ability.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onVampireHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Player data
        Player player = (Player) event.getDamager();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();

        // Target data
        Player target = (Player) event.getEntity();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);
        Location targetLoc = target.getLocation();

        // Ignores the event if the damager is not using the Vampire kit.
        if (!(playerData.getActiveKit() instanceof Vampire)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Gives the player half a heart per hit.
        if (playerData.getLifeStealCooldown() != null) {
            double health = player.getHealth();
            double maxHealth = player.getMaxHealth();
            player.setHealth(Math.min(health + 1, maxHealth));
        }
    }
}

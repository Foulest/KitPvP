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
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.Kangaroo;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.*;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Data
public class KangarooListener implements Listener {

    /**
     * Handles Kangaroo's ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onKangarooAbility(@NotNull PlayerInteractEvent event) {
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
        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit, item, Material.FIREWORK)) {
            return;
        }

        // Ignores the event if the player isn't using the Kangaroo ability.
        if (!(playerKit instanceof Kangaroo)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.FIREWORK) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Play the ability sound.
        player.getWorld().playSound(playerLoc, Sound.SLIME_WALK2, 1, 1);

        // Gets the launch vector.
        Vector direction = player.getEyeLocation().getDirection();

        // Adjusts the direction based on whether the player is sneaking.
        if (BlockUtil.isOnGroundOffset(player, 0.1)) {
            direction.setY(0.3);
            direction.multiply(2.5);
        } else {
            direction.setY(1.2);
        }

        // Launches the player into the air.
        player.setVelocity(direction);
        playerData.setNoFall(true);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.kangarooKitCooldown, true);
    }

    /**
     * Handles Kangaroo's Market Gardener item.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onMarketGardenerHit(@NotNull EntityDamageByEntityEvent event) {
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
        String targetName = target.getName();

        // Ignores the event if the damager is not using the Kangaroo kit.
        if (!(playerData.getActiveKit() instanceof Kangaroo)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Ignores hits that aren't with the Market Gardener.
        if (player.getItemInHand() == null
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Market Gardener")) {
            return;
        }

        // If a player hits a target with the Market Gardener...
        // 1. If the player is airborne and has a cooldown...
        // 1a. Deal +150% damage to the target.

        // 1. If the player is airborne and has a cooldown...
        if (playerData.isNoFall() && playerData.hasCooldown(false)) {
            // 1a. Deal +150% damage to the target.
            MessageUtil.messagePlayer(player, "&aYou landed a critical hit on &e" + targetName + "&a!");
            player.getWorld().playSound(playerLoc, Sound.ITEM_BREAK, 1, 1);
            double damage = event.getDamage();
            event.setDamage(damage * 2.5); // * 1.5 is +50% damage; * 2.5 is +150% damage.
        }
    }
}

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
import net.foulest.kitpvp.kits.type.Ninja;
import net.foulest.kitpvp.util.AbilityUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.TaskUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class NinjaListener implements Listener {

    /**
     * Handles the Ninja ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onNinjaAbility(@NotNull PlayerInteractEvent event) {
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
        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit, item, Material.INK_SACK)) {
            return;
        }

        // Gives the player Invisibility and Jump Boost III.
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                Settings.ninjaKitDuration * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                Settings.ninjaKitDuration * 20, 2, false, false));

        // Plays a smoke sound effect.
        player.getWorld().playEffect(playerLoc, Effect.LARGE_SMOKE, 1, 1);

        // Hides the player from other players.
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == player) {
                continue;
            }

            target.hidePlayer(player);
        }

        // Create a task that restores the player's visibility.
        TaskUtil.runTaskLater(() -> {
            if (playerData.getActiveKit() instanceof Ninja) {
                player.getWorld().playSound(playerLoc, Sound.BAT_IDLE, 1, 1);
                player.removePotionEffect(PotionEffectType.JUMP);
                MessageUtil.messagePlayer(player, "&cYou are no longer invisible.");

                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target == player) {
                        continue;
                    }

                    target.showPlayer(player);
                }
            }
        }, Settings.ninjaKitDuration * 20L);

        // Sets the player's ability cooldown.
        player.getWorld().playSound(playerLoc, Sound.BAT_DEATH, 1, 1);
        MessageUtil.messagePlayer(player, "&aYou are now invisible.");
        playerData.setCooldown(playerKit, Settings.ninjaKitCooldown, true);
    }

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

    /**
     * Handles Ninjas damaging players while invisible.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onNinjaInvisHit(@NotNull EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Checks if the entities are both players.
        if (!(damagerEntity instanceof Player)
                || !(targetEntity instanceof Player)) {
            return;
        }

        Player damager = (Player) damagerEntity;
        PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

        // Cancels hits while Ninja is invisible.
        if (damagerData.getActiveKit() instanceof Ninja
                && damager.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            MessageUtil.messagePlayer(damager, "&cYou can't damage other players while invisible.");
            event.setCancelled(true);
        }
    }
}

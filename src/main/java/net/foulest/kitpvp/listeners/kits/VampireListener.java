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
import net.foulest.kitpvp.kits.type.Vampire;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
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
public class VampireListener implements Listener {

//    /**
//     * Handles the Vampire ability.
//     *
//     * @param event The event.
//     */
//    @EventHandler
//    @SuppressWarnings("NestedMethodCall")
//    public static void onVampireAbility(@NotNull PlayerInteractEvent event) {
//        // Ignores the event if the player isn't right-clicking.
//        if (event.getAction() != Action.RIGHT_CLICK_AIR
//                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
//            return;
//        }
//
//        Player player = event.getPlayer();
//        PlayerData playerData = PlayerDataManager.getPlayerData(player);
//        Location playerLoc = player.getLocation();
//        Kit playerKit = playerData.getActiveKit();
//        ItemStack itemStack = event.getItem();
//        Material abilityItem = Material.REDSTONE;
//
//        // Ignores the event if the given item does not match the desired item.
//        if (itemStack == null
//                || !itemStack.hasItemMeta()
//                || !itemStack.getItemMeta().hasDisplayName()
//                || itemStack.getType() != abilityItem) {
//            return;
//        }
//
//        // Ignores the event if the player is not using the desired kit.
//        if (!(playerKit instanceof Vampire)) {
//            return;
//        }
//
//        // Ignores the event if the player is in a safe zone.
//        if (Regions.isInSafezone(playerLoc)) {
//            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
//            return;
//        }
//
//        // Ignores the event if the player's ability is on cooldown.
//        if (playerData.hasCooldown(abilityItem, true)) {
//            return;
//        }
//
//        // TODO: Replace this with metadata.
//
//        // Create a task to notify the player that the Life Steal ability has ended.
//        BukkitTask lifeStealTask = new BukkitRunnable() {
//            @Override
//            public void run() {
//                player.playSound(playerLoc, Sound.BAT_IDLE, 1, 1);
//                MessageUtil.messagePlayer(player, "&cLife Steal is no longer active.");
//            }
//        }.runTaskLater(KitPvP.instance, Settings.vampireKitDuration * 20L);
//
//        // Cancel the Life Steal cooldown task if it's active.
//        TaskUtil.runTaskLater(() -> playerData.setLifeStealCooldown(null), Settings.vampireKitDuration * 20L);
//
//        // Set the Life Steal cooldown task.
//        playerData.setLifeStealCooldown(lifeStealTask);
//
//        // Sets the player's ability cooldown.
//        player.playSound(playerLoc, Sound.BAT_HURT, 1, 1);
//        MessageUtil.messagePlayer(player, "&aLife Steal has been activated.");
//        playerData.setCooldown(playerKit, abilityItem, Settings.vampireKitCooldown, true);
//    }

//    /**
//     * Handles the Life Steal ability.
//     *
//     * @param event The event.
//     */
//    @EventHandler(ignoreCancelled = true)
//    public static void onVampireHit(@NotNull EntityDamageByEntityEvent event) {
//        // Ignores the event if the damager or target is not a player.
//        if (!(event.getDamager() instanceof Player)
//                || !(event.getEntity() instanceof Player)) {
//            return;
//        }
//
//        // Player data
//        Player player = (Player) event.getDamager();
//        PlayerData playerData = PlayerDataManager.getPlayerData(player);
//        Location playerLoc = player.getLocation();
//
//        // Target data
//        Player target = (Player) event.getEntity();
//        PlayerData targetData = PlayerDataManager.getPlayerData(target);
//        Location targetLoc = target.getLocation();
//
//        // Ignores the event if the damager is not using the Vampire kit.
//        if (!(playerData.getActiveKit() instanceof Vampire)
//                || targetData.getActiveKit() == null
//                || Regions.isInSafezone(playerLoc)
//                || Regions.isInSafezone(targetLoc)) {
//            return;
//        }
//
//        // Gives the player half a heart per hit.
//        if (playerData.getLifeStealCooldown() != null) {
//            double health = player.getHealth();
//            double maxHealth = player.getMaxHealth();
//            player.setHealth(Math.min(health + 1, maxHealth));
//        }
//    }

    /**
     * Handles the Vampire ability.
     *
     * @param event The event.
     */
    @EventHandler
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
        ItemStack itemStack = event.getItem();
        Material abilityItem = Material.INK_SACK;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Vampire)) {
            return;
        }

        // Ignores the event if the player is in a safe zone.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(abilityItem, true)) {
            return;
        }

        // Gives the player Invisibility, Speed II, and Jump Boost III.
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                Settings.vampireKitDuration * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                Settings.vampireKitDuration * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                Settings.vampireKitDuration * 20, 2, false, false));

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
        int changeCount = playerData.getChangeCount();
        TaskUtil.runTaskLater(() -> {
            if (playerData.getChangeCount() == changeCount) {
                player.getWorld().playSound(playerLoc, Sound.BAT_IDLE, 1, 1);
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.JUMP);
                MessageUtil.messagePlayer(player, "&cYou are no longer invisible.");
            }

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target == player) {
                    continue;
                }

                target.showPlayer(player);
            }
        }, Settings.vampireKitDuration * 20L + 1L);

        // Sets the player's ability cooldown.
        playerData.setNoFall(true);
        player.getWorld().playSound(playerLoc, Sound.BAT_DEATH, 1, 1);
        MessageUtil.messagePlayer(player, "&aYou are now invisible.");
        playerData.setCooldown(playerKit, abilityItem, Settings.vampireKitCooldown, true);
    }

    /**
     * Handles damaging players while invisible.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onInvisHit(@NotNull EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Checks if the entities are both players.
        if (!(damagerEntity instanceof Player)
                || !(targetEntity instanceof Player)) {
            return;
        }

        Player damager = (Player) damagerEntity;
        PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

        // Cancels hits while the player is invisible.
        if (damagerData.getActiveKit() instanceof Vampire
                && damager.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            MessageUtil.messagePlayer(damager, "&cYou can't damage other players while invisible.");
            event.setCancelled(true);
        }
    }
}

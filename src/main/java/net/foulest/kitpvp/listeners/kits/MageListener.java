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
import net.foulest.kitpvp.kits.type.Mage;
import net.foulest.kitpvp.kits.type.Pyro;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Data
public class MageListener implements Listener {

    /**
     * Handles the Mage ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onMageAbility(@NotNull PlayerInteractEvent event) {
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
        Material abilityItem = Material.GLOWSTONE_DUST;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Mage)) {
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

        // Gets the nearby players within a 5 block radius.
        Collection<Player> nearbyPlayers = AbilityUtil.getNearbyPlayers(player, 5, 5, 5);

        // Ignores the event if there are no players nearby.
        if (nearbyPlayers.isEmpty()) {
            player.playSound(playerLoc, Sound.VILLAGER_NO, 1, 1);
            MessageUtil.messagePlayer(player, "&cAbility failed: no players nearby.");
            playerData.setCooldown(playerKit, abilityItem, 3, true);
            return;
        }

        for (Player target : nearbyPlayers) {
            PlayerData targetData = PlayerDataManager.getPlayerData(target);
            Kit targetKit = targetData.getActiveKit();
            Location targetLoc = target.getLocation();

            // Give the target Slowness, Blindness, and Weakness for 3 seconds.
            target.playSound(targetLoc, Sound.FIZZ, 1, 1);
            target.getWorld().playSound(targetLoc, Sound.ZOMBIE_WOODBREAK, 0.5F, 1);
            target.getWorld().playEffect(targetLoc, Effect.LARGE_SMOKE, 1);
            target.getWorld().playEffect(targetLoc, Effect.LARGE_SMOKE, 1);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Settings.mageKitDuration * 20, 1, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Settings.mageKitDuration * 20, 1, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Settings.mageKitDuration * 20, 1, false, false));
            MessageUtil.messagePlayer(target, "&cYou have been debuffed by a Mage!");

            // Gives the kit's default potion effects back after the ability duration.
            int changeCount = targetData.getChangeCount();
            TaskUtil.runTaskLater(() -> {
                if (targetData.getChangeCount() == changeCount) {
                    for (PotionEffect effect : targetKit.getPotionEffects()) {
                        target.addPotionEffect(effect);
                    }
                }
            }, Settings.mageKitDuration * 20L + 1L);
        }

        // Sets the player's ability cooldown.
        player.playSound(playerLoc, Sound.FIZZ, 1, 1);
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, abilityItem, Settings.mageKitCooldown, true);
    }

    /**
     * Handles damaging players with a Sun Staff.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onSunStaffHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Player data
        Player player = (Player) event.getDamager();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Target data
        Player target = (Player) event.getEntity();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);
        Location targetLoc = target.getLocation();

        // ----------------------------------------------------------------
        // If the player, who's holding a Sun Staff, attacks the target...
        // 1. Check if the target is on fire and not a Pyro.
        // 1a. If they are, deal 6.0 damage.
        // 1b. Else, deal 3.0 damage.
        // ----------------------------------------------------------------

        // If the player, who's holding a Sun Staff, attacks the target...
        if (playerData.getActiveKit() instanceof Mage
                && player.getItemInHand().getType() == Material.BLAZE_ROD
                && player.getItemInHand().getItemMeta().getDisplayName().contains("Sun Staff")) {

            // If the target is on fire and the target is not a Pyro...
            if (target.getFireTicks() > 0 && !(targetData.getActiveKit() instanceof Pyro)) {
                target.getWorld().playSound(targetLoc, Sound.FIZZ, 0.5F, 1);
                event.setDamage(6.0);
            } else {
                event.setDamage(3.0);
            }
        }
    }

    /**
     * Handles taking reduced fire damage while holding a Sun Staff.
     *
     * @param event EntityDamageEvent
     */
    @EventHandler
    public static void onFireDamage(@NotNull EntityDamageEvent event) {
        // Ignores the event if the target is not a player.
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Player data
        Player player = (Player) event.getEntity();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Ignores the event if the player is not a Mage.
        if (!(playerData.getActiveKit() instanceof Mage)) {
            return;
        }

        // Ignores the event if the player is not holding a Sun Staff.
        if (player.getItemInHand() == null
                || player.getItemInHand().getType() != Material.BLAZE_ROD
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Sun Staff")) {
            return;
        }

        // If the player is taking fire damage...
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            // Reduce the damage by 40%.
            double damage = event.getDamage();
            event.setDamage(damage * 0.6);
        }
    }
}

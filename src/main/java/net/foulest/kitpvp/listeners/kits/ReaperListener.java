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
import net.foulest.kitpvp.kits.type.Reaper;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class ReaperListener implements Listener {

    /**
     * Handles the Reaper ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onReaperAbility(@NotNull PlayerInteractEvent event) {
        // Ignores the event if the player isn't right-clicking.
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Player activeReaperMark = playerData.getActiveReaperMark();
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();
        ItemStack itemStack = event.getItem();
        Material abilityItem = Material.PAPER;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Reaper)) {
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

        // Check if the player has any active marks.
        if (activeReaperMark == null) {
            MessageUtil.messagePlayer(player, "&cYou do not have an active mark.");
            return;
        }

        // Removes the mark from the player.
        removeReaperMark(playerData, true, true);

        // Play the ability sound.
        player.getWorld().playSound(playerLoc, Sound.CREEPER_DEATH, 1, 1);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour active mark has been cleared.");
        playerData.setCooldown(playerKit, abilityItem, Settings.reaperKitCooldown, false);
    }

    /**
     * Handles the Reaper's Scythe ability.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onScytheHit(@NotNull EntityDamageByEntityEvent event) {
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

        // Ignores the event if the damager is not using the Reaper kit.
        if (!(playerData.getActiveKit() instanceof Reaper)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Ignores hits that aren't with Reaper's Scythe.
        if (player.getItemInHand() == null
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Reaper's Scythe")) {
            return;
        }

        // ------------------------------------------------
        // When a player is hit by a Reaper...
        // 1. Check if the Reaper has an active mark.
        // 1a. If the Reaper has an active mark, do nothing.
        // 1b. If the Reaper does not have an active mark, apply a mark to the target.
        // ------------------------------------------------

        // Check if the Reaper has an active mark.
        if (playerData.getActiveReaperMark() != null) {
            return;
        }

        // Check if that player is already marked.
        if (target.hasMetadata("reaperMark")) {
            return;
        }

        // Apply a mark to the target.
        playerData.setActiveReaperMark(target);
        MessageUtil.messagePlayer(player, "&aYou have marked &e" + targetName + " &aas your target!");
        MessageUtil.messagePlayer(target, "&cYou have been marked by a Reaper!");

        // Set the mark metadata on the target.
        target.setMetadata("reaperMark", new FixedMetadataValue(KitPvP.getInstance(), true));

        // Remove the mark after the designated time.
        TaskUtil.runTaskLater(() -> {
            if (target.hasMetadata("reaperMark")) {
                MessageUtil.messagePlayer(player, "&aYour mark on &e" + targetName + " &ahas expired.");
                removeReaperMark(targetData, false, true);
            }
        }, Settings.reaperKitDuration * 20L);

        // Play the ability sounds.
        target.getWorld().playSound(targetLoc, Sound.SKELETON_WALK, 1, 1);
        target.getWorld().playSound(targetLoc, Sound.SHEEP_SHEAR, 1, 1);

        // Give the target one second of Blindness if they don't have it.
        if (!target.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
        }
    }

    /**
     * Handles the Reaper's Mark hit event.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onMarkHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Damager data
        Player damager = (Player) event.getDamager();
        PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

        // Target data
        Player target = (Player) event.getEntity();

        // Set the damage dealt by the Scythe to 4 hearts.
        if (damagerData.getActiveKit() instanceof Reaper
                && damager.getItemInHand() != null
                && damager.getItemInHand().hasItemMeta()
                && damager.getItemInHand().getItemMeta().hasDisplayName()
                && damager.getItemInHand().getItemMeta().getDisplayName().contains("Reaper's Scythe")) {
            event.setDamage(4.0);
        }

        // Ignores the event if the target does not have a mark.
        if (target.getMetadata("reaperMark").isEmpty()) {
            return;
        }

        // Applies a 50% damage increase to the target.
        double damage = event.getDamage();
        event.setDamage(damage * 1.5);

        // Play the ability sound.
        Location location = target.getLocation();
        target.getWorld().playSound(location, Sound.SKELETON_HURT, 0.5F, 1);
    }

    /**
     * Removes the Reaper mark from the player.
     *
     * @param playerData The player's data.
     * @param isPlayerDataReaper If the PlayerData object is the Reaper.
     */
    public static void removeReaperMark(@NotNull PlayerData playerData, boolean isPlayerDataReaper, boolean messageTarget) {
        Player player = playerData.getPlayer();
        Player reaperMark = playerData.getActiveReaperMark();

        // Removes marks if you are the Reaper.
        if (isPlayerDataReaper && reaperMark != null) {
            Location location = reaperMark.getLocation();
            reaperMark.getWorld().playSound(location, Sound.FIZZ, 1, 1);
            reaperMark.removeMetadata("reaperMark", KitPvP.getInstance());
            playerData.setActiveReaperMark(null);

            if (messageTarget) {
                if (!Regions.isInSafezone(location)) {
                    MessageUtil.messagePlayer(reaperMark, "&aYou are no longer marked by a Reaper.");
                }

                MessageUtil.messagePlayer(player, "&aYour active mark has been cleared.");
            }
        }

        // Removes marks if you are the target.
        if (!isPlayerDataReaper && player.hasMetadata("reaperMark")) {
            Location location = player.getLocation();
            player.getWorld().playSound(location, Sound.FIZZ, 1, 1);
            player.removeMetadata("reaperMark", KitPvP.getInstance());

            if (messageTarget && !Regions.isInSafezone(location)) {
                MessageUtil.messagePlayer(player, "&aYou are no longer marked by a Reaper.");
            }

            // Iterates through all online players to find the Reaper who marked the target.
            for (Player onlinePlayer : KitPvP.getInstance().getServer().getOnlinePlayers()) {
                PlayerData onlinePlayerData = PlayerDataManager.getPlayerData(onlinePlayer);
                Player activeReaperMark = onlinePlayerData.getActiveReaperMark();

                if (player.equals(activeReaperMark)) {
                    onlinePlayerData.setActiveReaperMark(null);
                    MessageUtil.messagePlayer(onlinePlayer, "&aYour active mark has been cleared.");
                    break;
                }
            }
        }
    }
}

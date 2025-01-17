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
import net.foulest.kitpvp.kits.type.Soldier;
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
public class SoldierListener implements Listener {

    /**
     * Handles the Soldier's rage build-up on hit.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onRageBuild(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Damager data
        Player damager = (Player) event.getDamager();
        PlayerData damagerData = PlayerDataManager.getPlayerData(damager);
        Kit playerKit = damagerData.getActiveKit();

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Soldier)) {
            return;
        }

        // Ignores the event if the damager's rage meter is already full.
        if (damagerData.getSoldierRage() >= Settings.soldierKitMaxRage) {
            return;
        }

        double damage = event.getDamage();
        double soldierRage = damagerData.getSoldierRage();

        // Adds the damage dealt to the damager's rage meter.
        damagerData.setSoldierRage(Math.min(soldierRage + damage, Settings.soldierKitMaxRage));

        // Sends a message to the damager if their rage meter is full.
        if (damagerData.getSoldierRage() >= Settings.soldierKitMaxRage) {
            Location location = damager.getLocation();
            damager.playSound(location, Sound.WOLF_GROWL, 0.5F, 1.0F);
            MessageUtil.messagePlayer(damager, "&eYour rage is fully built and ready for use!");
        }
    }

    /**
     * Handles the Soldier's Sword hit event.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onSoldierSwordHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Damager data
        Player damager = (Player) event.getDamager();
        PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

        // Give the damager a speed boost on hit.
        if (damagerData.getActiveKit() instanceof Soldier
                && damager.getItemInHand() != null
                && damager.getItemInHand().hasItemMeta()
                && damager.getItemInHand().getItemMeta().hasDisplayName()
                && damager.getItemInHand().getItemMeta().getDisplayName().contains("Soldier's Sword")) {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0, false, false));
        }
    }

    /**
     * Handles the Buff Banner hit event.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onBuffBannerHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Damager data
        Player damager = (Player) event.getDamager();

        // Ignores the event if the damager doesn't have the Buff Banner metadata.
        if (!damager.hasMetadata("buffBanner")) {
            return;
        }

        double damage = event.getDamage();

        // Adds 50% damage to the hit.
        event.setDamage(damage * 1.5);
    }

    /**
     * Handles the Buff Banner ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onBuffBannerAbility(@NotNull PlayerInteractEvent event) {
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
        Material abilityItem = Material.BOAT;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Soldier)) {
            return;
        }

        // Ignores the event if the player is in a safe zone.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Checks if the player has a full rage meter.
        if (playerData.getSoldierRage() < Settings.soldierKitMaxRage) {
            int percentage = (int) ((playerData.getSoldierRage() / Settings.soldierKitMaxRage) * 100);
            MessageUtil.messagePlayer(player, "&cYou need to build up your rage meter! (" + percentage + "%)");
            return;
        }

        // Sets the player's rage meter to 0.
        playerData.setSoldierRage(0);

        // Play the ability sound.
        MessageUtil.messagePlayer(player, "&eYou have activated your Buff Banner!");
        player.getWorld().playSound(playerLoc, Sound.ZOMBIE_PIG_ANGRY, 1, 1);

        // Sets the 'buffBanner' metadata on the player.
        player.setMetadata("buffBanner", new FixedMetadataValue(KitPvP.getInstance(), true));

        // Remove the metadata after the designated time.
        TaskUtil.runTaskLater(() -> {
            if (player.hasMetadata("buffBanner")) {
                player.removeMetadata("buffBanner", KitPvP.getInstance());
                player.playSound(playerLoc, Sound.CREEPER_DEATH, 1, 1);
                MessageUtil.messagePlayer(player, "&cYour rage meter is back to normal.");
            }
        }, Settings.soldierKitBannerDuration * 20L);
    }

    /**
     * Handles the Battalion's Backup ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onBattalionAbility(@NotNull PlayerInteractEvent event) {
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
        Material abilityItem = Material.POWERED_MINECART;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Soldier)) {
            return;
        }

        // Ignores the event if the player is in a safe zone.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Checks if the player has a full rage meter.
        if (playerData.getSoldierRage() < Settings.soldierKitMaxRage) {
            int percentage = (int) ((playerData.getSoldierRage() / Settings.soldierKitMaxRage) * 100);
            MessageUtil.messagePlayer(player, "&cYou need to build up your rage meter! (" + percentage + "%)");
            return;
        }

        // Sets the player's rage meter to 0.
        playerData.setSoldierRage(0);

        // Play the ability sound.
        MessageUtil.messagePlayer(player, "&eYou have activated your Battalion's Backup!");
        player.getWorld().playSound(playerLoc, Sound.ZOMBIE_REMEDY, 1, 1);

        // Give the player the resistance and absorption effects.
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Settings.soldierKitBattalionDuration * 20, 0, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Settings.soldierKitBattalionDuration * 20, 2, true, true));

        // Gives the kit's default potion effects back after the ability duration.
        int changeCount = playerData.getChangeCount();
        TaskUtil.runTaskLater(() -> {
            player.playSound(playerLoc, Sound.CREEPER_DEATH, 1, 1);
            MessageUtil.messagePlayer(player, "&cYour rage meter is back to normal.");

            if (playerData.getChangeCount() == changeCount) {
                for (PotionEffect effect : playerKit.getPotionEffects()) {
                    player.addPotionEffect(effect);
                }
            }
        }, Settings.archerKitDuration * 20L + 1L);
    }
}

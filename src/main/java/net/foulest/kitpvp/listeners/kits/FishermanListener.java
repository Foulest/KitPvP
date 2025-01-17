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
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.Fisherman;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class FishermanListener implements Listener {

    /**
     * Handles the Fisherman kit's ability.
     *
     * @param event The event being handled.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onFishermanAbility(@NotNull PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();
        PlayerFishEvent.State state = event.getState();
        Material rodSpamItem = Material.RAW_FISH;
        Material abilityItem = Material.FISHING_ROD;

        // Ignores the event if the player isn't using the Fisherman kit.
        if (!(playerKit instanceof Fisherman)) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            event.setCancelled(true);
            return;
        }

        // Checks if the player has an active rod cooldown.
        if (state == PlayerFishEvent.State.FISHING
                && playerData.hasCooldown(rodSpamItem, true)) {
            event.setCancelled(true);
            return;
        }

        // Sets the player's rod cooldown if the state is FISHING.
        if (state == PlayerFishEvent.State.FISHING) {
            playerData.setCooldown(playerKit, rodSpamItem, Settings.fishermanKitRodCooldown, false);
        }

        // Handles hooking players with the Fishing Rod.
        if (event.getCaught() instanceof Player) {
            Player target = (Player) event.getCaught();

            // Ignores the event if the player hooks themselves.
            if (target == player) {
                MessageUtil.messagePlayer(player, "&cYou can't hook yourself.");
                event.setCancelled(true);
                event.getHook().remove();
                return;
            }

            PlayerData targetData = PlayerDataManager.getPlayerData(target);
            Location targetLoc = target.getLocation();

            // Ignores ineligible players.
            if (targetData.getActiveKit() == null || Regions.isInSafezone(targetLoc)) {
                MessageUtil.messagePlayer(player, "&cYou can't hook this player.");
                event.setCancelled(true);
                event.getHook().remove();
                return;
            }

            // Marks both players for combat.
            CombatTag.markForCombat(player, target);

            // Handles players reeling other players in.
            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
                // Checks if the player's ability is on cooldown.
                if (playerData.hasCooldown(abilityItem, true)) {
                    event.setCancelled(true);
                    return;
                }

                // Play the ability sound and effect.
                target.getWorld().playSound(targetLoc, Sound.SPLASH2, 1, 1);
                target.getWorld().playEffect(targetLoc, Effect.SPLASH, 1);
                player.playSound(playerLoc, Sound.SPLASH2, 1, 1);

                // Teleports the target to the player's location.
                MessageUtil.messagePlayer(target, "&cYou have been hooked by a Fisherman!");
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Settings.fishermanKitDuration * 20, 0, false, false));
                event.getCaught().teleport(playerLoc);

                // Sets the player's ability cooldown.
                MessageUtil.messagePlayer(player, "&aYour ability has been used.");
                playerData.setCooldown(playerKit, abilityItem, Settings.fishermanKitCooldown, true);
            }
        }
    }

    /**
     * Handles the Fisherman's Sword ability.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onFishermanSwordHit(@NotNull EntityDamageByEntityEvent event) {
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

        // Ignores the event if the damager is not using the Jester kit.
        if (!(playerData.getActiveKit() instanceof Fisherman)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Ignores hits that aren't with the Fisherman's Sword.
        if (player.getItemInHand() == null
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Fisherman's Sword")) {
            return;
        }

        // Increases the damage dealt by 50% if the target is in water.
        if (target.getLocation().getBlock().getType() == Material.WATER
                || target.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
            double damage = event.getDamage();
            event.setDamage(damage * 1.5);
            target.getWorld().playSound(targetLoc, Sound.SPLASH2, 0.5F, 1);
        }
    }
}

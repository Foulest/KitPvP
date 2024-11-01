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
import net.foulest.kitpvp.kits.type.Tank;
import net.foulest.kitpvp.util.AbilityUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
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

@Data
public class TankListener implements Listener {

    /**
     * Handles the Tank's Fortify ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onTankAbility(@NotNull PlayerInteractEvent event) {
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
        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit, item, Material.ANVIL)) {
            return;
        }

        // ----------------------------------------------------------------
        // When the player uses the Fortify ability...
        // 1. Play the ability sound.
        // 2. Add the slowness and resistance effects.
        // 3. Create a task that restores the Tank's slowness.
        // 4. Sets the player's ability cooldown.
        // ----------------------------------------------------------------

        // 1. Play the ability sound.
        player.getWorld().playSound(playerLoc, Sound.ANVIL_BREAK, 1, 1);

        // 2. Add the slowness and resistance effects.
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Settings.tankKitDuration * 20, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Settings.tankKitDuration * 20, 2));

        // 3. Create a task that restores the Tank's slowness.
        TaskUtil.runTaskLater(() -> {
            if (playerData.getActiveKit() instanceof Tank) {
                MessageUtil.messagePlayer(player, "&cYour effects have been restored.");
                player.removePotionEffect(PotionEffectType.SLOW);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false, false));
            }
        }, Settings.tankKitDuration * 20L);

        // 4. Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.tankKitCooldown, true);
    }

    /**
     * Handles getting hit while holding Tank's Shovel.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onTankShovelHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the target is not a player.
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Target data
        Player target = (Player) event.getEntity();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        // ----------------------------------------------------------------
        // If the target, who's holding Tank's Shovel, takes damage...
        // 1. Check the source of the damage.
        // 2. If the source is melee, increase the damage by 40%.
        // 3. If the source is ranged, decrease the damage by 40%.
        // ----------------------------------------------------------------

        // If the target, who's holding Tank's Shovel, takes damage...
        if (targetData.getActiveKit() instanceof Tank
                && target.getItemInHand() != null
                && target.getItemInHand().hasItemMeta()
                && target.getItemInHand().getItemMeta().hasDisplayName()
                && target.getItemInHand().getItemMeta().getDisplayName().contains("Tank's Shovel")) {

            // 1. Check the source of the damage.
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {

                // 2. If the source is melee, increase the damage by 40%.
                double damage = event.getDamage();
                event.setDamage(damage * 1.4);

            } else if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();

                // Handles players shooting other players.
                if (arrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                    Player damager = (Player) arrow.getShooter();
                    Player receiver = (Player) event.getEntity();

                    // Cancels the event if the receiver is the damager.
                    if (receiver.equals(damager)) {
                        event.setCancelled(true);
                        return;
                    }

                    // 3. If the source is ranged, decrease the damage by 40%.
                    double damage = event.getDamage();
                    event.setDamage(damage * 0.6);
                }
            }
        }
    }
}

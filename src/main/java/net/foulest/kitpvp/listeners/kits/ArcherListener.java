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
import net.foulest.kitpvp.util.AbilityUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class ArcherListener implements Listener {

    /**
     * Handles Archer's ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onArcherAbility(@NotNull PlayerInteractEvent event) {
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
        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit, item, Material.FEATHER)) {
            return;
        }

        // Plays the ability sound.
        player.getWorld().playSound(playerLoc, Sound.BAT_TAKEOFF, 1, 1);

        // Remove the player's existing speed, resistance, and weakness effects.
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.WEAKNESS);

        // Gives the player speed, resistance, weakness, and regeneration.
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Settings.archerKitDuration * 20, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Settings.archerKitDuration * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Settings.archerKitDuration * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Settings.archerKitDuration * 20, 1, false, false));

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.archerKitCooldown, true);
    }
}

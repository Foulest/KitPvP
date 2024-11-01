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
import net.foulest.kitpvp.kits.type.Archer;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class ArcherListener implements Listener {

    /**
     * Handles the Archer ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onArcherAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Archer ability.
        if (!(playerKit instanceof Archer)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.FEATHER) {
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

//        // Create a task that restores the Archer's speed.
//        TaskUtil.runTaskLater(() -> {
//            if (playerData.getActiveKit() instanceof Archer) {
//                player.removePotionEffect(PotionEffectType.SPEED);
//                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
//            }
//        }, Settings.archerKitDuration * 20L);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.archerKitCooldown, true);
    }
}

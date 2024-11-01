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
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.AbilityUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class FishermanListener implements Listener {

    /**
     * Handles Fisherman's ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onFishermanAbility(@NotNull PlayerFishEvent event) {
        // Ignores the event if the target is not a player.
        if (!(event.getCaught() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();
        Player target = (Player) event.getCaught();

        // Ignores the event if the player hooks themselves.
        if (target == player) {
            MessageUtil.messagePlayer(player, "&cYou can't hook yourself.");
            event.setCancelled(true);
            return;
        }

        PlayerData targetData = PlayerDataManager.getPlayerData(target);
        Location targetLoc = target.getLocation();

        // Ignores ineligible players.
        if (targetData.getActiveKit() == null || Regions.isInSafezone(targetLoc)) {
            event.setCancelled(true);
            return;
        }

        // Marks both players for combat.
        CombatTag.markForCombat(player, target);

        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Checks for ability exclusions.
        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit)) {
            return;
        }

        // Ignores the event if the player isn't using the Fisherman ability.
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        // Play the ability sound.
        target.getWorld().playSound(targetLoc, Sound.WATER, 1, 1);
        target.getWorld().playEffect(targetLoc, Effect.SPLASH, 1);
        player.playSound(playerLoc, Sound.SPLASH2, 1, 1);

        // Teleports the target to the player's location.
        MessageUtil.messagePlayer(target, "&cYou have been hooked by a Fisherman!");
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Settings.fishermanKitDuration * 20, 0, false, false));
        event.getCaught().teleport(playerLoc);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.fishermanKitCooldown, true);
    }
}

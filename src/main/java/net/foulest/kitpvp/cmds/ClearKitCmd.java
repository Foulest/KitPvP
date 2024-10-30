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
package net.foulest.kitpvp.cmds;

import lombok.Data;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Command for clearing your kit.
 *
 * @author Foulest
 */
@Data
public class ClearKitCmd {

    @Command(name = "clearkit", description = "Clears your kit.",
            aliases = {"ck", "ckit"},
            permission = "kitpvp.clearkit", usage = "/clearkit (player)", inGameOnly = true)
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        Location location = player.getLocation();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Handles clearing your own kit.
        if (args.length() == 0) {
            if (CombatTag.isInCombat(player)) {
                MessageUtil.messagePlayer(player, ConstantUtil.COMBAT_TAGGED);
                return;
            }

            if (Regions.isInSafezone(location)) {
                if (playerData.getActiveKit() == null) {
                    MessageUtil.messagePlayer(player, ConstantUtil.NO_KIT_SELECTED);
                    return;
                }

                clearKit(playerData);
                MessageUtil.messagePlayer(player, "&aYour kit has been cleared.");
                return;
            }

            MessageUtil.messagePlayer(player, "&cYou need to be in spawn to clear your kit.");
            return;
        }

        // Handles clearing kits from other players.
        if (args.getPlayer().hasPermission("kitpvp.clearkit.others")) {
            String target = args.getArgs(1);
            Player targetPlayer = Bukkit.getPlayer(target);
            String targetName = targetPlayer.getName();
            PlayerData targetData = PlayerDataManager.getPlayerData(targetPlayer);

            if (!targetPlayer.isOnline()) {
                MessageUtil.messagePlayer(player, ConstantUtil.PLAYER_NOT_FOUND);
                return;
            }

            if (targetData.getActiveKit() == null) {
                MessageUtil.messagePlayer(targetPlayer, ConstantUtil.NO_KIT_SELECTED);
                return;
            }

            clearKit(targetData);
            MessageUtil.messagePlayer(targetPlayer, "&aYour kit has been cleared by a staff member.");
            MessageUtil.messagePlayer(player, "&aYou cleared " + targetName + "'s kit.");
        }
    }

    /**
     * Clears a player's kit.
     *
     * @param playerData The player's data.
     */
    private static void clearKit(@NotNull PlayerData playerData) {
        Player player = playerData.getPlayer();
        Location location = player.getLocation();
        Kit activeKit = playerData.getActiveKit();

        playerData.setPreviousKit(activeKit);
        playerData.clearCooldowns();
        playerData.setActiveKit(null);

        player.setHealth(20);
        player.getInventory().setHeldItemSlot(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType effectType = effect.getType();
            player.removePotionEffect(effectType);
        }

        playerData.giveDefaultItems();
        player.playSound(location, Sound.SLIME_WALK, 1, 1);
    }
}

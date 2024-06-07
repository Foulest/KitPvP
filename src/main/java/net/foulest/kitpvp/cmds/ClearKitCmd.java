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

import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * Command for clearing your kit.
 *
 * @author Foulest
 * @project KitPvP
 */
public class ClearKitCmd {

    @Command(name = "clearkit", description = "Clears your kit.",
            permission = "kitpvp.clearkit", usage = "/clearkit (player)", inGameOnly = true)
    public void onCommand(@NotNull CommandArgs args) {
        if (!(args.getSender() instanceof Player)) {
            MessageUtil.messagePlayer(args.getSender(), "Only players can execute this command.");
            return;
        }

        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Handles clearing your own kit.
        if (args.length() == 0) {
            if (CombatTag.isInCombat(args.getPlayer())) {
                MessageUtil.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
                return;
            }

            if (Regions.isInSafezone(player.getLocation())) {
                if (playerData.getActiveKit() == null) {
                    MessageUtil.messagePlayer(player, "&cYou do not have a kit selected.");
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
            Player target = Bukkit.getPlayer(args.getArgs(1));
            PlayerData targetData = PlayerDataManager.getPlayerData(target);

            if (!target.isOnline()) {
                MessageUtil.messagePlayer(player, "&cThat player is not online.");
                return;
            }

            if (targetData.getActiveKit() == null) {
                MessageUtil.messagePlayer(target, "&cYou do not have a kit selected.");
                return;
            }

            clearKit(targetData);
            MessageUtil.messagePlayer(target, "&aYour kit has been cleared by a staff member.");
            MessageUtil.messagePlayer(player, "&aYou cleared " + target.getName() + "'s kit.");
        }
    }

    /**
     * Clears a player's kit.
     *
     * @param playerData The player's data.
     */
    public static void clearKit(@NotNull PlayerData playerData) {
        Player player = playerData.getPlayer();

        playerData.setPreviousKit(playerData.getActiveKit());
        playerData.clearCooldowns();
        playerData.setActiveKit(null);

        player.setHealth(20);
        player.getInventory().setHeldItemSlot(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        playerData.giveDefaultItems();
        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
    }
}

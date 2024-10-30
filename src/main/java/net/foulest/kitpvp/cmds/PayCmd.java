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
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for paying other players.
 *
 * @author Foulest
 */
@Data
public class PayCmd {

    @Command(name = "pay", description = "Send coins to another player.",
            usage = "/pay <player> <amount>", inGameOnly = true, permission = "kitpvp.pay")
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();

        if (args.length() == 2) {
            Player player = args.getPlayer();

            // Checks if the player is null.
            if (player == null) {
                MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
                return;
            }

            Location location = player.getLocation();
            String playerName = player.getName();

            String desiredTarget = args.getArgs(0);
            Player target = Bukkit.getPlayer(desiredTarget);

            // Checks if the target is online.
            if (target == null) {
                MessageUtil.messagePlayer(player, ConstantUtil.PLAYER_NOT_FOUND);
                return;
            }

            String desiredAmount = args.getArgs(1);

            // Checks if the amount is a number.
            if (!StringUtils.isNumeric(desiredAmount)) {
                MessageUtil.messagePlayer(player, "&c'" + desiredAmount + "' is not a valid amount.");
                return;
            }

            int amount = Integer.parseInt(desiredAmount);

            // Checks if the amount is negative.
            if (amount < 0) {
                MessageUtil.messagePlayer(player, "&cThe amount must be positive.");
                return;
            }

            // Checks if the sender is the target.
            if (sender == target) {
                player.playSound(location, Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cYou can't pay yourself.");
                return;
            }

            PlayerData targetData = PlayerDataManager.getPlayerData(target);
            PlayerData senderData = PlayerDataManager.getPlayerData(player);

            // Checks if the sender has enough coins.
            if (senderData.getCoins() - amount <= 0) {
                MessageUtil.messagePlayer(sender, ConstantUtil.NOT_ENOUGH_COINS);
                return;
            }

            int targetCoins = targetData.getCoins();

            targetData.setCoins(targetCoins + amount);
            senderData.removeCoins(amount);

            MessageUtil.messagePlayer(player, "&a" + playerName + " sent you " + amount + " coins!");
            MessageUtil.messagePlayer(sender, "&aYou sent " + playerName + " " + amount + " coins!");
            return;
        }

        MessageUtil.messagePlayer(sender, "&cUsage: /pay <player> <amount>");
    }
}

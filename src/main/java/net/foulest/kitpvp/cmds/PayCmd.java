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
        CommandSender commandSender = args.getSender();
        Player sender = args.getPlayer();

        // Checks if the player is null.
        if (sender == null) {
            MessageUtil.messagePlayer(commandSender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        if (args.length() == 2) {
            // Sender data
            PlayerData senderData = PlayerDataManager.getPlayerData(sender);
            Location senderLoc = sender.getLocation();
            String senderName = sender.getName();

            // Target data
            String desiredTarget = args.getArgs(0);
            Player target = Bukkit.getPlayer(desiredTarget);

            // Checks if the target is online.
            if (target == null) {
                MessageUtil.messagePlayer(sender, ConstantUtil.PLAYER_NOT_FOUND);
                return;
            }

            String desiredAmount = args.getArgs(1);

            // Checks if the amount is a number.
            if (!StringUtils.isNumeric(desiredAmount)) {
                MessageUtil.messagePlayer(sender, "&c'" + desiredAmount + "' is not a valid amount.");
                return;
            }

            int amount = Integer.parseInt(desiredAmount);

            // Checks if the amount is negative.
            if (amount < 0) {
                MessageUtil.messagePlayer(sender, "&cThe amount must be positive.");
                return;
            }

            // Checks if the sender is the target.
            if (commandSender == target) {
                sender.playSound(senderLoc, Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(sender, "&cYou can't pay yourself.");
                return;
            }

            // Target data
            PlayerData targetData = PlayerDataManager.getPlayerData(target);
            int targetCoins = targetData.getCoins();
            String targetName = target.getName();

            // Checks if the sender has enough coins.
            if (senderData.getCoins() - amount <= 0) {
                MessageUtil.messagePlayer(commandSender, ConstantUtil.NOT_ENOUGH_COINS);
                return;
            }

            // Transfers the coins.
            targetData.setCoins(targetCoins + amount);
            senderData.removeCoins(amount);

            // Sends messages to the sender and target.
            MessageUtil.messagePlayer(target, "&a" + senderName + " sent you " + amount + " coins!");
            MessageUtil.messagePlayer(sender, "&aYou sent " + targetName + " " + amount + " coins!");
        } else {
            MessageUtil.messagePlayer(commandSender, "&cUsage: /pay <player> <amount>");
        }
    }
}

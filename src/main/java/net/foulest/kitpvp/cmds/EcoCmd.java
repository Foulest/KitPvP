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
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Command for managing a player's balance.
 *
 * @author Foulest
 */
@Data
public class EcoCmd {

    @Command(name = "eco", description = "Main command for KitPvP's economy.",
            usage = "/eco <give/set/take> <player> <amount>", permission = "kitpvp.eco")
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();

        // No additional arguments, display usage message.
        if (args.length() != 3) {
            MessageUtil.messagePlayer(sender, "&cUsage: /eco <give/set/take> <player> <amount>");
            return;
        }

        String target = args.getArgs(1);
        Player targetPlayer = Bukkit.getPlayer(target);

        // Checks if the target is online.
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            MessageUtil.messagePlayer(sender, ConstantUtil.PLAYER_NOT_FOUND);
            return;
        }

        String targetName = targetPlayer.getName();
        PlayerData targetData = PlayerDataManager.getPlayerData(targetPlayer);
        int targetCoins = targetData.getCoins();

        String desiredAmount = args.getArgs(2);

        // Checks if the amount is a number.
        if (!StringUtils.isNumeric(desiredAmount)) {
            MessageUtil.messagePlayer(sender, "&c'" + target + "' is not a valid amount.");
            return;
        }

        int totalCoins;
        int amount = Integer.parseInt(desiredAmount);

        // Checks if the amount is negative.
        if (amount < 0) {
            MessageUtil.messagePlayer(sender, "&cYou cannot use negative numbers.");
            return;
        }

        // Handles sub-commands.
        String subCommand = args.getArgs(0);
        switch (subCommand.toLowerCase(Locale.ROOT)) {
            case "give":
                if (!sender.hasPermission("kitpvp.eco.give")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                targetData.setCoins(targetCoins + amount);
                totalCoins = targetData.getCoins();

                if (sender instanceof Player && targetPlayer.equals(sender)) {
                    MessageUtil.messagePlayer(targetPlayer, "&aYou set your balance to " + totalCoins + " coins. &7(+" + amount + ")");
                    return;
                }

                MessageUtil.messagePlayer(targetPlayer, "&aYou were given " + amount + " coins! &7(Total: " + totalCoins + ")");
                MessageUtil.messagePlayer(sender, "&aYou set " + targetName + "'s balance to " + totalCoins + " coins. &7(+" + amount + ")");
                break;

            case "set":
                if (!sender.hasPermission("kitpvp.eco.set")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                targetData.setCoins(amount);
                totalCoins = targetData.getCoins();

                if (sender instanceof Player && targetPlayer.equals(sender)) {
                    MessageUtil.messagePlayer(targetPlayer, "&aYou set your balance to " + totalCoins + " coins.");
                    return;
                }

                MessageUtil.messagePlayer(targetPlayer, "&aYour balance was set to " + totalCoins + " coins.");
                MessageUtil.messagePlayer(sender, "&aYou set " + targetName + "'s balance to " + totalCoins + " coins.");
                break;

            case "take":
                if (!sender.hasPermission("kitpvp.eco.take")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                targetData.removeCoins(amount);
                totalCoins = targetData.getCoins();

                if (sender instanceof Player && targetPlayer.equals(sender)) {
                    MessageUtil.messagePlayer(targetPlayer, "&aYou set your balance to " + totalCoins + " coins. &7(-" + amount + ")");
                    return;
                }

                MessageUtil.messagePlayer(targetPlayer, "&aYour balance was set to " + totalCoins + " coins. &7(-" + amount + ")");
                MessageUtil.messagePlayer(sender, "&aYou set " + targetName + "'s balance to " + totalCoins + " coins. &7(-" + amount + ")");
                break;

            default:
                MessageUtil.messagePlayer(sender, "&cUsage: /eco <give/set/take> <player> <amount>");
                break;
        }
    }
}

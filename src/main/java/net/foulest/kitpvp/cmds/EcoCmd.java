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

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for managing a player's balance.
 *
 * @author Foulest
 * @project KitPvP
 */
public class EcoCmd {

    @Command(name = "eco", description = "Main command for KitPvP's economy.",
            usage = "/eco <give/set/take> <player> <amount>", permission = "kitpvp.eco")
    public void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();

        // No additional arguments, display usage message.
        if (args.length() != 3) {
            MessageUtil.messagePlayer(sender, "&cUsage: /eco <give/set/take> <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(1));

        // Checks if the target is online.
        if (target == null || !target.isOnline()) {
            MessageUtil.messagePlayer(sender, "Player not found.");
            return;
        }

        String targetName = target.getName();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        // Checks if the amount is a number.
        if (!StringUtils.isNumeric(args.getArgs(1))) {
            MessageUtil.messagePlayer(args.getSender(), "&c'" + args.getArgs(1) + "' is not a valid amount.");
            return;
        }

        int totalCoins;
        int amount = Integer.parseInt(args.getArgs(2));

        // Checks if the amount is negative.
        if (amount < 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cYou cannot use negative numbers.");
            return;
        }

        // Handles sub-commands.
        String subCommand = args.getArgs(0);
        switch (subCommand.toLowerCase()) {
            case "give":
                if (!sender.hasPermission("kitpvp.eco.give")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                targetData.setCoins(targetData.getCoins() + amount);
                totalCoins = targetData.getCoins();

                if (sender instanceof Player && target.equals(sender)) {
                    MessageUtil.messagePlayer(target, "&aYou set your balance to " + totalCoins + " coins. &7(+" + amount + ")");
                    return;
                }

                MessageUtil.messagePlayer(target, "&aYou were given " + amount + " coins! &7(Total: " + totalCoins + ")");
                MessageUtil.messagePlayer(sender, "&aYou set " + targetName + "'s balance to " + totalCoins + " coins. &7(+" + amount + ")");
                break;

            case "set":
                if (!sender.hasPermission("kitpvp.eco.set")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                targetData.setCoins(amount);
                totalCoins = targetData.getCoins();

                if (sender instanceof Player && target.equals(sender)) {
                    MessageUtil.messagePlayer(target, "&aYou set your balance to " + totalCoins + " coins.");
                    return;
                }

                MessageUtil.messagePlayer(target, "&aYour balance was set to " + totalCoins + " coins.");
                MessageUtil.messagePlayer(sender, "&aYou set " + targetName + "'s balance to " + totalCoins + " coins.");
                break;

            case "take":
                if (!sender.hasPermission("kitpvp.eco.take")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                targetData.removeCoins(amount);
                totalCoins = targetData.getCoins();

                if (sender instanceof Player && target.equals(sender)) {
                    MessageUtil.messagePlayer(target, "&aYou set your balance to " + totalCoins + " coins. &7(-" + amount + ")");
                    return;
                }

                MessageUtil.messagePlayer(target, "&aYour balance was set to " + totalCoins + " coins. &7(-" + amount + ")");
                MessageUtil.messagePlayer(sender, "&aYou set " + targetName + "'s balance to " + totalCoins + " coins. &7(-" + amount + ")");
                break;

            default:
                MessageUtil.messagePlayer(sender, "&cUsage: /eco <give/set/take> <player> <amount>");
                break;
        }
    }
}

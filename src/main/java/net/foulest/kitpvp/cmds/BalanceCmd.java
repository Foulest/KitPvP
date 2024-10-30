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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for checking a player's balance.
 *
 * @author Foulest
 */
@Data
public class BalanceCmd {

    @Command(name = "balance", aliases = {"bal", "money", "coins"},
            description = "Shows your current balance.",
            permission = "kitpvp.balance", usage = "/balance [player]", inGameOnly = true)
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Prints the usage message.
        if (args.length() != 1) {
            int coins = playerData.getCoins();
            MessageUtil.messagePlayer(sender, "&fCoins: &6" + coins);
            return;
        }

        String targetName = args.getArgs(0);
        Player target = Bukkit.getPlayer(targetName);
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (!target.isOnline()) {
            MessageUtil.messagePlayer(sender, ConstantUtil.PLAYER_NOT_FOUND);
            return;
        }

        targetName = target.getName();
        int targetCoins = targetData.getCoins();

        MessageUtil.messagePlayer(sender, "&f" + targetName + "'s Coins: &6" + targetCoins);
    }
}

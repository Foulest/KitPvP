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

import lombok.NoArgsConstructor;
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
 * @project KitPvP
 */
@NoArgsConstructor
public class BalanceCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "balance", aliases = {"bal", "money", "coins"},
            description = "Shows your current balance.",
            permission = "kitpvp.balance", usage = "/balance [player]", inGameOnly = true)
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        CommandSender sender = args.getSender();

        // Prints the usage message.
        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&fCoins: &6" + playerData.getCoins());
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (!target.isOnline()) {
            MessageUtil.messagePlayer(sender, ConstantUtil.PLAYER_NOT_FOUND);
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&f" + target.getName() + "'s Coins: &6" + targetData.getCoins());
    }
}

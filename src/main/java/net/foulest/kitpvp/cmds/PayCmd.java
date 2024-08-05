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
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for paying other players.
 *
 * @author Foulest
 * @project KitPvP
 */
@NoArgsConstructor
public class PayCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "pay", description = "Send coins to another player.",
            usage = "/pay <player> <amount>", inGameOnly = true, permission = "kitpvp.pay")
    public void onCommand(@NotNull CommandArgs args) {
        if (args.length() == 2) {
            Player target = Bukkit.getPlayer(args.getArgs(0));

            if (target == null) {
                MessageUtil.messagePlayer(args.getSender(), ConstantUtil.PLAYER_NOT_FOUND);
                return;
            }

            if (!StringUtils.isNumeric(args.getArgs(1))) {
                MessageUtil.messagePlayer(args.getSender(), "&c'" + args.getArgs(1) + "' is not a valid amount.");
                return;
            }

            int amount = Integer.parseInt(args.getArgs(1));

            if (amount < 0) {
                MessageUtil.messagePlayer(args.getPlayer(), "&cThe amount must be positive.");
                return;
            }

            Player player = args.getPlayer();
            Player sender = (Player) args.getSender();
            PlayerData targetData = PlayerDataManager.getPlayerData(player);
            PlayerData senderData = PlayerDataManager.getPlayerData(sender);

            if (senderData.getCoins() - amount <= 0) {
                MessageUtil.messagePlayer(sender, ConstantUtil.NOT_ENOUGH_COINS);
                return;
            }

            targetData.setCoins(targetData.getCoins() + amount);
            senderData.removeCoins(amount);

            if ((args.getSender() instanceof Player) && player == args.getSender()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cYou can't pay yourself.");
                return;
            }

            MessageUtil.messagePlayer(player, "&a" + player.getName() + " sent you " + amount + " coins!");
            MessageUtil.messagePlayer(args.getSender(), "&aYou sent " + player.getName() + " " + amount + " coins!");
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&cUsage: /pay <player> <amount>");
    }
}

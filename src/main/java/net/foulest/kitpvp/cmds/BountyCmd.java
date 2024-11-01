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
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Command for placing bounties on other players.
 *
 * @author Foulest
 */
@Data
public class BountyCmd {

    @Command(name = "bounty", aliases = "bounties",
            description = "Allows players to place bounties on each other.",
            permission = "kitpvp.bounties", usage = "/bounty [player]", inGameOnly = true)
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();

        UUID benefactorUUID = playerData.getBenefactor();
        Player benefactor = Bukkit.getPlayer(benefactorUUID);

        // Checks if the bounties feature is enabled.
        if (!Settings.bountiesEnabled) {
            MessageUtil.messagePlayer(player, ConstantUtil.COMMAND_DISABLED);
            return;
        }

        if (args.length() == 0) {
            MessageUtil.messagePlayer(player, "");
            int activeBounty = playerData.getBounty();

            if (activeBounty == 0 || benefactorUUID == null || !benefactor.isOnline()) {
                MessageUtil.messagePlayer(player, " &aYou currently don't have a");
                MessageUtil.messagePlayer(player, " &abounty on your head.");
            } else {
                String benefactorName = benefactor.getName();
                MessageUtil.messagePlayer(player, " &cYou currently have a &e$" + activeBounty + " &cbounty");
                MessageUtil.messagePlayer(player, " &con your head set by &e" + benefactorName + "&c.");
            }

            MessageUtil.messagePlayer(player, "");

            if (player.hasPermission("kitpvp.bounties.place")) {
                MessageUtil.messagePlayer(player, " &fYou can place one on another player");
                MessageUtil.messagePlayer(player, " &fusing &e/bounty set <player> <amount>&f.");
                MessageUtil.messagePlayer(player, "");
            }
            return;
        }

        if (args.length() != 3 || !(args.getArgs(0).equals("set"))) {
            MessageUtil.messagePlayer(player, "&cUsage: /bounty set <player> <amount>");
            return;
        }

        String targetName = args.getArgs(1);
        Player target = Bukkit.getPlayer(targetName);
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (target.equals(player)) {
            MessageUtil.messagePlayer(player, "&cYou can't set a bounty on yourself.");
            return;
        }

        if (!target.isOnline()) {
            MessageUtil.messagePlayer(player, ConstantUtil.PLAYER_NOT_FOUND);
            return;
        }

        targetName = target.getName();
        String bountyAmount = args.getArgs(2);

        if (!StringUtils.isNumeric(bountyAmount)) {
            MessageUtil.messagePlayer(player, "&c'" + bountyAmount + "' is not a valid amount.");
            return;
        }

        int amount = Integer.parseInt(bountyAmount);
        int minCoins = Settings.bountiesMinAmount;
        int maxCoins = Settings.bountiesMaxAmount;

        if (amount < minCoins) {
            MessageUtil.messagePlayer(player, "&cThe amount needs to be at least 50 coins.");
            return;
        }

        if (amount > maxCoins) {
            MessageUtil.messagePlayer(player, "&cThe amount cannot exceed " + maxCoins + " coins.");
            return;
        }

        if (playerData.getCoins() - amount < 0) {
            MessageUtil.messagePlayer(player, ConstantUtil.NOT_ENOUGH_COINS);
            return;
        }

        int targetBounty = targetData.getBounty();

        if (targetBounty > amount) {
            MessageUtil.messagePlayer(player, "&c" + targetName + " already has a higher bounty.");
            return;
        }

        MessageUtil.messagePlayer(player, "&aYou set a $" + amount + " bounty on " + targetName + "'s head.");

        MessageUtil.messagePlayer(target, "");
        MessageUtil.messagePlayer(target, " &c" + playerName + " &eset a &c$" + amount + " &ebounty on your head.");
        MessageUtil.messagePlayer(target, "");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(target) && !online.equals(player)) {
                MessageUtil.messagePlayer(online, "&c" + playerName + " &eset a &c$" + amount + " &ebounty on &c" + targetName + "&e's head.");
            }
        }

        targetData.addBounty(amount, playerUUID);
        playerData.removeCoins(amount);

        UUID targetBenefactor = targetData.getBenefactor();

        // Refund the original benefactor if they set a new bounty on the same player.
        if (targetBenefactor != null) {
            Player targetBenefactorPlayer = Bukkit.getPlayer(targetBenefactor);
            PlayerData targetBenefactorData = PlayerDataManager.getPlayerData(targetBenefactorPlayer);

            if (targetBenefactorPlayer.isOnline()) {
                MessageUtil.messagePlayer(targetBenefactorPlayer, "&aYour bounty on " + targetName + "'s head has been refunded.");
                targetBenefactorData.addCoins(targetBounty);
            } else {
                // Load player data asynchronously
                targetBenefactorData.load().thenAccept(success -> {
                    if (success) {
                        targetBenefactorData.addCoins(targetBounty);
                    } else {
                        String benefactorPlayerName = targetBenefactorPlayer.getName();
                        MessageUtil.messagePlayer(player, "&cAn error occurred while loading " + benefactorPlayerName + "'s data.");
                    }
                });
            }
        }
    }
}

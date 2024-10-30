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
 * Command for viewing a player's statistics.
 *
 * @author Foulest
 */
@Data
public class StatsCmd {

    @Command(name = "stats", description = "Shows a player's statistics.",
            usage = "/stats", inGameOnly = true, permission = "kitpvp.stats")
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        // Prints the usage message.
        if (args.length() > 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /stats [player]");
            return;
        }

        // Display the sender's stats.
        if (args.length() == 0) {
            displayStats(player, true);
        }

        // Displays the stats of another player.
        if (args.length() == 1) {
            String targetName = args.getArgs(0);

            if (targetName.length() > 16) {
                MessageUtil.messagePlayer(sender, ConstantUtil.PLAYER_NOT_FOUND);
                return;
            }

            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                MessageUtil.messagePlayer(sender, ConstantUtil.PLAYER_NOT_FOUND);
                return;
            }

            displayStats(targetPlayer, false);
        }
    }

    /**
     * Displays the stats of a player.
     *
     * @param player     The player to display the stats to.
     * @param samePlayer Whether the player is the same as the sender.
     */
    public static void displayStats(Player player, boolean samePlayer) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        String playerName = player.getName();
        int kills = playerData.getKills();
        int deaths = playerData.getDeaths();
        String kdrText = playerData.getKDRText();
        int killstreak = playerData.getKillstreak();
        int topKillstreak = playerData.getTopKillstreak();
        int level = playerData.getLevel();
        int expPercent = playerData.getExpPercent();
        int coins = playerData.getCoins();
        int bounty = playerData.getBounty();

        MessageUtil.messagePlayer(player, "");
        MessageUtil.messagePlayer(player, " &a&l" + (samePlayer ? "Your" : playerName + "'s") + " Stats");
        MessageUtil.messagePlayer(player, " &fKills: &e" + kills);
        MessageUtil.messagePlayer(player, " &fDeaths: &e" + deaths);
        MessageUtil.messagePlayer(player, " &fK/D Ratio: &e" + kdrText);
        MessageUtil.messagePlayer(player, " &fStreak: &e" + killstreak + " &7(" + topKillstreak + ")");
        MessageUtil.messagePlayer(player, " &fLevel: &e" + level + " &7(" + expPercent + "%)");
        MessageUtil.messagePlayer(player, " &fCoins: &6" + coins);
        MessageUtil.messagePlayer(player, " &fBounty: &6" + bounty);
        MessageUtil.messagePlayer(player, "");
    }
}

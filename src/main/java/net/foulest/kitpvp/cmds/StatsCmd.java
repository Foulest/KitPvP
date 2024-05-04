package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for viewing a player's statistics.
 *
 * @author Foulest
 * @project KitPvP
 */
public class StatsCmd {

    @Command(name = "stats", description = "Shows a player's statistics.",
            usage = "/stats", inGameOnly = true, permission = "kitpvp.stats")
    public void onCommand(@NotNull CommandArgs args) {
        Player player;
        Player sender = args.getPlayer();

        // Prints the usage message.
        if (args.length() > 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /stats [player]");
            return;
        }

        // Display the sender's stats.
        if (args.length() == 0) {
            displayStats(sender, true);
        }

        // Displays the stats of another player.
        if (args.length() == 1) {
            if (args.getArgs(0).length() > 16) {
                MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                return;
            }

            player = Bukkit.getPlayer(args.getArgs(0));

            if (player == null || !player.isOnline()) {
                MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                return;
            }

            displayStats(sender, false);
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

        MessageUtil.messagePlayer(player, "");
        MessageUtil.messagePlayer(player, " &a&l" + (samePlayer ? "Your" : player.getName() + "'s") + " Stats");
        MessageUtil.messagePlayer(player, " &fKills: &e" + playerData.getKills());
        MessageUtil.messagePlayer(player, " &fDeaths: &e" + playerData.getDeaths());
        MessageUtil.messagePlayer(player, " &fK/D Ratio: &e" + playerData.getKDRText());
        MessageUtil.messagePlayer(player, " &fStreak: &e" + playerData.getKillstreak() + " &7(" + playerData.getTopKillstreak() + ")");
        MessageUtil.messagePlayer(player, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
        MessageUtil.messagePlayer(player, " &fCoins: &6" + playerData.getCoins());
        MessageUtil.messagePlayer(player, " &fBounty: &6" + playerData.getBounty());
        MessageUtil.messagePlayer(player, "");
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class StatsCmd {

    private static final int MAX_NAME_LENGTH = 16;

    @Command(name = "stats", description = "Shows a player's statistics.", usage = "/stats", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player;
        Player sender = args.getPlayer();
        PlayerData playerData;

        if (args.length() > 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /stats [player]");
            return;
        }

        if (args.length() == 0) {
            playerData = PlayerData.getInstance(sender);

            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &alYour Stats");
            MessageUtil.messagePlayer(sender, " &fKills: &e" + playerData.getKills());
            MessageUtil.messagePlayer(sender, " &fDeaths: &e" + playerData.getDeaths());
            MessageUtil.messagePlayer(sender, " &fK/D Ratio: &e" + playerData.getKDRText());
            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &fStreak: &e" + playerData.getKillstreak());
            MessageUtil.messagePlayer(sender, " &fHighest Streak: &e" + playerData.getTopKillstreak());
            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
            MessageUtil.messagePlayer(sender, " &fCoins: &6" + playerData.getCoins());
            MessageUtil.messagePlayer(sender, " &fBounty: &6" + playerData.getBounty());
            MessageUtil.messagePlayer(sender, "");
        }

        if (args.length() == 1) {
            if (args.getArgs(0).length() > MAX_NAME_LENGTH) {
                MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                return;
            }

            player = Bukkit.getPlayer(args.getArgs(0));

            if (player == null) {
                MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                return;
            }

            playerData = PlayerData.getInstance(player);

            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &a&l" + player.getName() + " Stats");
            MessageUtil.messagePlayer(sender, " &fKills: &e" + playerData.getKills());
            MessageUtil.messagePlayer(sender, " &fDeaths: &e" + playerData.getDeaths());
            MessageUtil.messagePlayer(sender, " &fK/D Ratio: &e" + playerData.getKDRText());
            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &fStreak: &e" + playerData.getKillstreak());
            MessageUtil.messagePlayer(sender, " &fHighest Streak: &e" + playerData.getTopKillstreak());
            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
            MessageUtil.messagePlayer(sender, " &fCoins: &6" + playerData.getCoins());
            MessageUtil.messagePlayer(sender, " &fBounty: &6" + playerData.getBounty());
            MessageUtil.messagePlayer(sender, "");
        }
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatsCmd {

    @Command(name = "stats", description = "Shows a player's statistics.", usage = "/stats", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player;
        Player sender = args.getPlayer();
        PlayerData playerData;

        if (args.length() > 1) {
            MiscUtils.messagePlayer(sender, "&cUsage: /stats [player]");
            return;
        }

        if (args.length() == 0) {
            playerData = PlayerData.getInstance(sender);

            MiscUtils.messagePlayer(sender, "");
            MiscUtils.messagePlayer(sender, " &aYour Stats");
            MiscUtils.messagePlayer(sender, " &fKills: &e" + playerData.getKills());
            MiscUtils.messagePlayer(sender, " &fDeaths: &e" + playerData.getDeaths());
            MiscUtils.messagePlayer(sender, " &fK/D Ratio: &e" + playerData.getKDRText());
            MiscUtils.messagePlayer(sender, "");
            MiscUtils.messagePlayer(sender, " &fStreak: &e" + playerData.getKillstreak());
            MiscUtils.messagePlayer(sender, " &fHighest Streak: &e" + playerData.getTopKillstreak());
            MiscUtils.messagePlayer(sender, "");
            MiscUtils.messagePlayer(sender, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
            MiscUtils.messagePlayer(sender, " &fCoins: &6" + playerData.getCoins());
            MiscUtils.messagePlayer(sender, " &fBounty: &cWIP");
            MiscUtils.messagePlayer(sender, " &fEvents Won: &cWIP");
            MiscUtils.messagePlayer(sender, "");
        }

        if (args.length() == 1) {
            if (args.getArgs(0).length() > 16) {
                MiscUtils.messagePlayer(sender, "&cPlayer not found.");
                return;
            }

            player = Bukkit.getPlayer(args.getArgs(0));

            if (player == null) {
                MiscUtils.messagePlayer(sender, "&cPlayer not found.");
                return;
            }

            playerData = PlayerData.getInstance(player);

            MiscUtils.messagePlayer(sender, "");
            MiscUtils.messagePlayer(sender, " &a" + player.getName() + " Stats");
            MiscUtils.messagePlayer(sender, " &fKills: &e" + playerData.getKills());
            MiscUtils.messagePlayer(sender, " &fDeaths: &e" + playerData.getDeaths());
            MiscUtils.messagePlayer(sender, " &fK/D Ratio: &e" + playerData.getKDRText());
            MiscUtils.messagePlayer(sender, "");
            MiscUtils.messagePlayer(sender, " &fStreak: &e" + playerData.getKillstreak());
            MiscUtils.messagePlayer(sender, " &fHighest Streak: &e" + playerData.getTopKillstreak());
            MiscUtils.messagePlayer(sender, "");
            MiscUtils.messagePlayer(sender, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
            MiscUtils.messagePlayer(sender, " &fCoins: &6" + playerData.getCoins());
            MiscUtils.messagePlayer(sender, " &fBounty: &cWIP");
            MiscUtils.messagePlayer(sender, " &fEvents Won: &cWIP");
            MiscUtils.messagePlayer(sender, "");
        }
    }
}

package net.foulest.kitpvp.util.scoreboard;

import dev.jcsoftware.jscoreboards.JGlobalMethodBasedScoreboard;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardUtil {

    public static final List<Scoreboard> scoreboards = new ArrayList<>();
    public static Scoreboard defaultScoreboard;
    public static Scoreboard kothScoreboard;

    public static void loadScoreboards() {
        if (Settings.config.getConfigurationSection("scoreboards") != null
                && !Settings.config.getConfigurationSection("scoreboards").getKeys(false).isEmpty()) {
            scoreboards.clear();

            for (String internalName : Settings.config.getConfigurationSection("scoreboards").getKeys(false)) {
                JGlobalMethodBasedScoreboard contents = new JGlobalMethodBasedScoreboard();
                contents.setTitle(Settings.config.getString("scoreboards." + internalName + ".title"));
                contents.setLines(Settings.config.getStringList("scoreboards." + internalName + ".lines"));
                Scoreboard scoreboard = new Scoreboard(internalName, contents);

                scoreboards.add(scoreboard);

                if (internalName.equals(Settings.config.getString("scoreboards.default-scoreboard"))) {
                    defaultScoreboard = scoreboard;
                } else if (internalName.equals(Settings.config.getString("scoreboards.koth-scoreboard"))) {
                    kothScoreboard = scoreboard;
                }
            }
        }
    }

    public static Scoreboard getScoreboard(String name) {
        for (Scoreboard scoreboard : scoreboards) {
            if (scoreboard.getName().equals(name)) {
                return scoreboard;
            }
        }

        return null;
    }

    public static boolean showDefaultScoreboard(Player player) {
        return showScoreboard(player, defaultScoreboard);
    }

    public static boolean showKOTHScoreboard(Player player) {
        return showScoreboard(player, kothScoreboard);
    }

    private static boolean showScoreboard(Player player, String name) {
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return false;
        }

        playerData.clearScoreboard();
        playerData.setActiveScoreboard(getScoreboard(name));

        if (getScoreboard(name) != null) {
            playerData.getActiveScoreboard().getContents().addPlayer(player);
        }
        return true;
    }

    private static boolean showScoreboard(Player player, Scoreboard scoreboard) {
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return false;
        }

        playerData.clearScoreboard();
        playerData.setActiveScoreboard(scoreboard);
        playerData.getActiveScoreboard().getContents().addPlayer(player);
        return true;
    }
}

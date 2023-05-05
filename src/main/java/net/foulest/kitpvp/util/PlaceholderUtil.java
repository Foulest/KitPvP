package net.foulest.kitpvp.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.listeners.CombatLog;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Sets up placeholders with PlaceholderAPI
 */
public class PlaceholderUtil extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "kitpvp";
    }

    @Override
    public String getAuthor() {
        return "Foulest";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData playerData = PlayerData.getInstance(player);
        StringBuilder builder = new StringBuilder();

        switch (identifier) {
            case "kills":
                builder.append(player == null ? "0" : playerData.getKills());
                break;

            case "deaths":
                builder.append(player == null ? "0" : playerData.getDeaths());
                break;

            case "kdr":
                builder.append(player == null ? "0" : playerData.getKDRText());
                break;

            case "killstreak":
                builder.append(player == null ? "0" : playerData.getKillstreak());
                break;

            case "top_killstreak":
                builder.append(player == null ? "0" : playerData.getTopKillstreak());
                break;

            case "coins":
                builder.append(player == null ? "0" : playerData.getCoins());
                break;

            case "level":
                builder.append(player == null ? "0" : playerData.getLevel());
                break;

            case "experience":
                builder.append(player == null ? "0" : playerData.getExperience());
                break;

            case "experience_percent":
                builder.append(player == null ? "0" : playerData.getExpPercent()).append("%");
                break;

            case "combattag":
                builder.append(CombatLog.isInCombat(player) ? "&c00:" + String.format("%02d", CombatLog.getRemainingTime(player)) : "&aSafe");
                break;

            case "activekit":
                builder.append(player == null || playerData.getKit() == null ? "None" : playerData.getKit().getName());
                break;

            case "bounty":
                builder.append(player == null || playerData.getBounty() == 0 ? "" : playerData.getBounty());
                break;

            case "bounty_tab":
                builder.append(player == null || playerData.getBounty() == 0 ? "" : "&6Bounty: &e&l$" + playerData.getBounty());
                break;
        }

        return builder.toString();
    }
}

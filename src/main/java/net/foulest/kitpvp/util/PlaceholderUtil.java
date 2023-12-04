package net.foulest.kitpvp.util;

import lombok.NonNull;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.listeners.CombatLog;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Sets up placeholders with PlaceholderAPI
 */
public class PlaceholderUtil extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "kitpvp";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Foulest";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(@NonNull Player player, @NonNull String identifier) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        StringBuilder builder = new StringBuilder();

        switch (identifier) {
            case "kills":
                builder.append(playerData.getKills());
                break;

            case "deaths":
                builder.append(playerData.getDeaths());
                break;

            case "kdr":
                builder.append(playerData.getKDRText());
                break;

            case "killstreak":
                builder.append(playerData.getKillstreak());
                break;

            case "top_killstreak":
                builder.append(playerData.getTopKillstreak());
                break;

            case "coins":
                builder.append(playerData.getCoins());
                break;

            case "level":
                builder.append(playerData.getLevel());
                break;

            case "experience":
                builder.append(playerData.getExperience());
                break;

            case "experience_percent":
                builder.append(playerData.getExpPercent()).append("%");
                break;

            case "combattag":
                builder.append(CombatLog.isInCombat(player) ? "&c00:" + String.format("%02d", CombatLog.getRemainingTime(player)) : "&aSafe");
                break;

            case "activekit":
                builder.append(playerData.getKit() == null ? "None" : playerData.getKit().getName());
                break;

            case "bounty":
                builder.append(playerData.getBounty() == 0 ? "" : playerData.getBounty());
                break;

            case "bounty_tab":
                builder.append(playerData.getBounty() == 0 ? "" : "&6Bounty: &e&l$" + playerData.getBounty());
                break;

            default:
                break;
        }
        return builder.toString();
    }
}

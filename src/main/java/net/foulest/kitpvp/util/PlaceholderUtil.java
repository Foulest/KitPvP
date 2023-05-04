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
        return "1.1.7";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return "";
        }

        // Placeholder: %kitpvp_kills%
        if (identifier.equals("kills")) {
            return (player == null) ? "0" : String.valueOf(playerData.getKills());
        }

        // Placeholder: %kitpvp_deaths%
        if (identifier.equals("deaths")) {
            return (player == null) ? "0" : String.valueOf(playerData.getDeaths());
        }

        // Placeholder: %kitpvp_kdr%
        if (identifier.equals("kdr")) {
            return (player == null) ? "0" : playerData.getKDRText();
        }

        // Placeholder: %kitpvp_killstreak%
        if (identifier.equals("killstreak")) {
            return (player == null) ? "0" : String.valueOf(playerData.getKillstreak());
        }

        // Placeholder: %kitpvp_top_killstreak%
        if (identifier.equals("top_killstreak")) {
            return (player == null) ? "0" : String.valueOf(playerData.getTopKillstreak());
        }

        // Placeholder: %kitpvp_coins%
        if (identifier.equals("coins")) {
            return (player == null) ? "0" : String.valueOf(playerData.getCoins());
        }

        // Placeholder: %kitpvp_level%
        if (identifier.equals("level")) {
            return (player == null) ? "0" : String.valueOf(playerData.getLevel());
        }

        // Placeholder: %kitpvp_experience%
        if (identifier.equals("experience")) {
            return (player == null) ? "0" : String.valueOf(playerData.getExperience());
        }

        // Placeholder: %kitpvp_experience_percent%
        if (identifier.equals("experience_percent")) {
            return (player == null) ? "0" : playerData.getExpPercent() + "%";
        }

        // Placeholder: %kitpvp_combattag%
        if (identifier.equals("combattag")) {
            return CombatLog.isInCombat(player) ? "&c00:" + String.format("%02d", CombatLog.getRemainingTime(player)) : "&aSafe";
        }

        // Placeholder: %kitpvp_activekit%
        if (identifier.equals("activekit")) {
            return (player == null || playerData.getKit() == null) ? "None" : playerData.getKit().getName();
        }

        // Placeholder: %kitpvp_bounty%
        if (identifier.equals("bounty")) {
            return (player == null || playerData.getBounty() == 0) ? "" : Integer.toString(playerData.getBounty());
        }

        // Placeholder: %kitpvp_bounty_tab%
        if (identifier.equals("bounty_tab")) {
            return (player == null || playerData.getBounty() == 0) ? "" : "&6Bounty: &e&l$" + playerData.getBounty();
        }

        return null;
    }
}

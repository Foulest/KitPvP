package net.foulest.kitpvp.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.foulest.kitpvp.listeners.CombatLog;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private final CombatLog combatLog = CombatLog.getInstance();

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
        return "1.0.6";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData playerData = PlayerData.getInstance(player);

        // Placeholder: %kitpvp_kills%
        if (identifier.equals("kills")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getKills();
        }

        // Placeholder: %kitpvp_deaths%
        if (identifier.equals("deaths")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getDeaths();
        }

        // Placeholder: %kitpvp_kdr%
        if (identifier.equals("kdr")) {
            if (player == null) {
                return "0.0";
            }

            return playerData.getKDRText();
        }

        // Placeholder: %kitpvp_killstreak%
        if (identifier.equals("killstreak")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getKillstreak();
        }

        // Placeholder: %kitpvp_top_killstreak%
        if (identifier.equals("top_killstreak")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getTopKillstreak();
        }

        // Placeholder: %kitpvp_coins%
        if (identifier.equals("coins")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getCoins();
        }

        // Placeholder: %kitpvp_level%
        if (identifier.equals("level")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getLevel();
        }

        // Placeholder: %kitpvp_experience%
        if (identifier.equals("experience")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getExperience();
        }

        // Placeholder: %kitpvp_experience_percent%
        if (identifier.equals("experience_percent")) {
            if (player == null) {
                return "0";
            }

            return "" + playerData.getExpPercent() + "%";
        }

        // Placeholder: %kitpvp_combattag%
        if (identifier.equals("combattag")) {
            return combatLog.isInCombat(player) ? "&c00:" + String.format("%02d", combatLog.getRemainingTime(player)) : "&aSafe";
        }

        // Placeholder: %kitpvp_activekit%
        if (identifier.equals("activekit")) {
            if (player == null || playerData.getKit() == null) {
                return "None";
            }

            return playerData.getKit().getName();
        }

        return null;
    }
}

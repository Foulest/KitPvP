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
        return "1.0.3";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        KitUser user = KitUser.getInstance(player);

        // Placeholder: %kitpvp_kills%
        if (identifier.equals("kills")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getKills();
        }

        // Placeholder: %kitpvp_deaths%
        if (identifier.equals("deaths")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getDeaths();
        }

        // Placeholder: %kitpvp_kdr%
        if (identifier.equals("kdr")) {
            if (player == null) {
                return "0.0";
            }

            return user.getKDRText();
        }

        // Placeholder: %kitpvp_killstreak%
        if (identifier.equals("killstreak")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getKillstreak();
        }

        // Placeholder: %kitpvp_top_killstreak%
        if (identifier.equals("top_killstreak")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getTopKillstreak();
        }

        // Placeholder: %kitpvp_coins%
        if (identifier.equals("coins")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getCoins();
        }

        // Placeholder: %kitpvp_level%
        if (identifier.equals("level")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getLevel();
        }

        // Placeholder: %kitpvp_experience%
        if (identifier.equals("experience")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getExperience();
        }

        // Placeholder: %kitpvp_experience_percent%
        if (identifier.equals("experience_percent")) {
            if (player == null) {
                return "0";
            }

            return "" + user.getExpPercent() + "%";
        }

        // Placeholder: %kitpvp_combattag%
        if (identifier.equals("combattag")) {
            return combatLog.isInCombat(player) ? "&c00:" + String.format("%02d", combatLog.getRemainingTime(player)) : "&aSafe";
        }

        // Placeholder: %kitpvp_activekit%
        if (identifier.equals("activekit")) {
            if (player == null || user.getKit() == null) {
                return "None";
            }

            return user.getKit().getName();
        }

        return null;
    }
}

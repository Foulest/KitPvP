package net.foulest.kitpvp.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.foulest.kitpvp.listeners.CombatLog;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class Placeholders extends PlaceholderExpansion {

    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();

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
        return "1.0.8";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData playerData = PlayerData.getInstance(player);

        // Placeholder: %kitpvp_kills%
        if (identifier.equals("kills")) {
            return (player == null) ? "0" : "" + playerData.getKills();
        }

        // Placeholder: %kitpvp_deaths%
        if (identifier.equals("deaths")) {
            return (player == null) ? "0" : "" + playerData.getDeaths();
        }

        // Placeholder: %kitpvp_kdr%
        if (identifier.equals("kdr")) {
            return (player == null) ? "0" : "" + playerData.getKDRText();
        }

        // Placeholder: %kitpvp_killstreak%
        if (identifier.equals("killstreak")) {
            return (player == null) ? "0" : "" + playerData.getKillstreak();
        }

        // Placeholder: %kitpvp_top_killstreak%
        if (identifier.equals("top_killstreak")) {
            return (player == null) ? "0" : "" + playerData.getTopKillstreak();
        }

        // Placeholder: %kitpvp_coins%
        if (identifier.equals("coins")) {
            return (player == null) ? "0" : "" + playerData.getCoins();
        }

        // Placeholder: %kitpvp_level%
        if (identifier.equals("level")) {
            return (player == null) ? "0" : "" + playerData.getLevel();
        }

        // Placeholder: %kitpvp_experience%
        if (identifier.equals("experience")) {
            return (player == null) ? "0" : "" + playerData.getExperience();
        }

        // Placeholder: %kitpvp_experience_percent%
        if (identifier.equals("experience_percent")) {
            return (player == null) ? "0" : "" + playerData.getExpPercent() + "%";
        }

        // Placeholder: %kitpvp_combattag%
        if (identifier.equals("combattag")) {
            return COMBAT_LOG.isInCombat(player) ? "&c00:" + String.format("%02d", COMBAT_LOG.getRemainingTime(player)) : "&aSafe";
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

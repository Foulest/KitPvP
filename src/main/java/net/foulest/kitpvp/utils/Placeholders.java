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
        return "1.0.7";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData playerData = PlayerData.getInstance(player);
        String killsPlaceholder = "kills";
        String deathsPlaceholder = "deaths";
        String kdrPlaceholder = "kdr";
        String killstreakPlaceholder = "killstreak";
        String topKillstreakPlaceholder = "top_killstreak";
        String coinsPlaceholder = "coins";
        String levelPlaceholder = "level";
        String experiencePlaceholder = "experience";
        String experiencePercentPlaceholder = "experience_percent";
        String combatTagPlaceholder = "combattag";
        String activeKitPlaceholder = "activekit";
        String bountyPlaceholder = "bounty";
        String bountyTabPlaceholder = "bounty_tab";

        // Placeholder: %kitpvp_kills%
        if (killsPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getKills();
        }

        // Placeholder: %kitpvp_deaths%
        if (deathsPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getDeaths();
        }

        // Placeholder: %kitpvp_kdr%
        if (kdrPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getKDRText();
        }

        // Placeholder: %kitpvp_killstreak%
        if (killstreakPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getKillstreak();
        }

        // Placeholder: %kitpvp_top_killstreak%
        if (topKillstreakPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getTopKillstreak();
        }

        // Placeholder: %kitpvp_coins%
        if (coinsPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getCoins();
        }

        // Placeholder: %kitpvp_level%
        if (levelPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getLevel();
        }

        // Placeholder: %kitpvp_experience%
        if (experiencePlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getExperience();
        }

        // Placeholder: %kitpvp_experience_percent%
        if (experiencePercentPlaceholder.equals(identifier)) {
            return (player == null) ? "0" : "" + playerData.getExpPercent() + "%";
        }

        // Placeholder: %kitpvp_combattag%
        if (combatTagPlaceholder.equals(identifier)) {
            return combatLog.isInCombat(player) ? "&c00:" + String.format("%02d", combatLog.getRemainingTime(player)) : "&aSafe";
        }

        // Placeholder: %kitpvp_activekit%
        if (activeKitPlaceholder.equals(identifier)) {
            return (player == null || playerData.getKit() == null) ? "None" : playerData.getKit().getName();
        }

        // Placeholder: %kitpvp_bounty%
        if (bountyPlaceholder.equals(identifier)) {
            return (player == null || playerData.getBounty() == 0) ? "" : Integer.toString(playerData.getBounty());
        }

        // Placeholder: %kitpvp_bounty_tab%
        if (bountyTabPlaceholder.equals(identifier)) {
            return (player == null || playerData.getBounty() == 0) ? "" : "&6Bounty: &e&l" + playerData.getBounty();
        }

        return null;
    }
}

/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Sets up placeholders with PlaceholderAPI.
 *
 * @author Foulest
 */
public class PlaceholderUtil extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "kitpvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Foulest";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Kit activeKit = playerData.getActiveKit();
        int bounty = playerData.getBounty();

        StringBuilder builder = new StringBuilder();

        switch (identifier) {
            case "kills":
                int kills = playerData.getKills();
                builder.append(kills);
                break;

            case "deaths":
                int deaths = playerData.getDeaths();
                builder.append(deaths);
                break;

            case "kdr":
                String kdrText = playerData.getKDRText();
                builder.append(kdrText);
                break;

            case "killstreak":
                int killstreak = playerData.getKillstreak();
                builder.append(killstreak);
                break;

            case "top_killstreak":
                int topKillstreak = playerData.getTopKillstreak();
                builder.append(topKillstreak);
                break;

            case "coins":
                int coins = playerData.getCoins();
                builder.append(coins);
                break;

            case "level":
                int level = playerData.getLevel();
                builder.append(level);
                break;

            case "experience":
                int experience = playerData.getExperience();
                builder.append(experience);
                break;

            case "experience_percent":
                int expPercent = playerData.getExpPercent();
                builder.append(expPercent).append("%");
                break;

            case "combattag":
                builder.append(CombatTag.isInCombat(player)
                        ? "&c00:" + String.format("%02d", CombatTag.getRemainingTime(player))
                        : "&aSafe");
                break;

            case "activekit":
                if (activeKit == null) {
                    builder.append("None");
                    break;
                }

                String kitName = activeKit.getName();
                builder.append(kitName);
                break;

            case "bounty":
                builder.append(bounty == 0 ? "" : bounty);
                break;

            case "bounty_tab":
                builder.append(bounty == 0 ? "" : "&6Bounty: &e&l$" + bounty);
                break;

            default:
                break;
        }
        return builder.toString();
    }
}

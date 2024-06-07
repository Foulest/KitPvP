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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Sets up placeholders with PlaceholderAPI.
 *
 * @author Foulest
 * @project KitPvP
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
                builder.append(CombatTag.isInCombat(player) ? "&c00:" + String.format("%02d", CombatTag.getRemainingTime(player)) : "&aSafe");
                break;

            case "activekit":
                builder.append(playerData.getActiveKit() == null ? "None" : playerData.getActiveKit().getName());
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

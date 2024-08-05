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
package net.foulest.kitpvp.combattag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CombatTag {

    private static final Map<Player, BukkitTask> combatScheduler = new HashMap<>();
    private static final Map<Player, Integer> combatHandler = new HashMap<>();
    private static final Map<Player, Player> lastAttacker = new HashMap<>();

    /**
     * Marks both players for combat.
     *
     * @param attacker The player attacking.
     * @param target   The player being attacked.
     */
    public static void markForCombat(Player attacker, Player target) {
        // Checks if combat tagging is enabled.
        if (!Settings.combatTagEnabled) {
            return;
        }

        Iterable<Player> players = new ArrayList<>(Arrays.asList(attacker, target));

        for (Player player : players) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            // Handles combat tagging for the player.
            if (isInCombat(player)) {
                combatHandler.replace(player, getRemainingTime(player), Settings.combatTagDuration);
            } else {
                combatHandler.put(player, Settings.combatTagDuration);

                combatScheduler.put(player, new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isInCombat(player)) {
                            if (getRemainingTime(player) > 1) {
                                combatHandler.replace(player, getRemainingTime(player), getRemainingTime(player) - 1);
                            } else {
                                remove(player);
                            }
                        }
                    }
                }.runTaskTimer(KitPvP.instance, 0L, 20L));
            }

            // Cancels the player's pending teleportation when taking damage for.
            if (Settings.combatTagCancelTeleport && playerData.getTeleportToSpawnTask() != null) {
                MessageUtil.messagePlayer(player, "&cTeleportation cancelled, you entered combat.");
                playerData.getTeleportToSpawnTask().cancel();
                playerData.setTeleportToSpawnTask(null);
            }
        }

        // Sets the last attackers.
        lastAttacker.put(target, attacker);
    }

    /**
     * Checks if a player is in combat.
     *
     * @param player The player to check.
     * @return Whether the player is in combat.
     */
    public static boolean isInCombat(Player player) {
        return combatHandler.containsKey(player);
    }

    /**
     * Gets the remaining time in combat for a player.
     *
     * @param player The player to check.
     * @return The remaining time in combat.
     */
    public static int getRemainingTime(Player player) {
        return isInCombat(player) ? combatHandler.get(player) : -1;
    }

    /**
     * Gets the last attacker of a player.
     *
     * @param player The player to check.
     * @return The last attacker.
     */
    public static Player getLastAttacker(Player player) {
        return lastAttacker.get(player);
    }

    /**
     * Removes a player from combat.
     *
     * @param player The player to remove.
     */
    public static void remove(Player player) {
        combatHandler.remove(player);

        if (combatScheduler.containsKey(player)) {
            combatScheduler.get(player).cancel();
            combatScheduler.remove(player);
        }

        lastAttacker.remove(player);
    }
}

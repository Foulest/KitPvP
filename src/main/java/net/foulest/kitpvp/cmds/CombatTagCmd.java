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
package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for checking your combat tag timer.
 *
 * @author Foulest
 */
public class CombatTagCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "combattag", aliases = {"combatlog", "ct", "combat", "combattime", "combattimer"},
            description = "Displays your current combat tag timer.",
            permission = "kitpvp.combattag", usage = "/combattag", inGameOnly = true)
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();

        // Prints the usage message.
        if (args.length() != 0) {
            MessageUtil.messagePlayer(player, "&cUsage: /combattag");
            return;
        }

        // Checks if the combat tag feature is enabled.
        if (!Settings.combatTagEnabled) {
            MessageUtil.messagePlayer(player, ConstantUtil.COMMAND_DISABLED);
            return;
        }

        if (CombatTag.isInCombat(player)) {
            int timeLeft = CombatTag.getRemainingTime(player);

            MessageUtil.messagePlayer(player, "&cYou are in combat for " + timeLeft
                    + " more " + (timeLeft == 1 ? "second" : "seconds") + ".");
        } else {
            MessageUtil.messagePlayer(player, "&aYou are not in combat.");
        }
    }
}

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

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.menus.KitSelector;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for selecting a kit or opening the Kit Selector.
 *
 * @author Foulest
 */
public class KitsCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "kit", aliases = {"kits", "kitselector"}, usage = "/kit [name]",
            description = "Selects a kit or opens the Kit Selector.", inGameOnly = true,
            permission = "kitpvp.kitselector")
    public void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        // Checks if the player is in spawn.
        if (!Regions.isInSafezone(player.getLocation())) {
            MessageUtil.messagePlayer(player, ConstantUtil.NOT_IN_SPAWN);
            return;
        }

        if (args.length() > 1) {
            MessageUtil.messagePlayer(player, "&cUsage: /kit [name]");
            return;
        }

        if (args.length() == 0) {
            new KitSelector(player);
            return;
        }

        Kit kit = KitManager.getKit(args.getArgs(0));

        if (kit == null) {
            MessageUtil.messagePlayer(player, "&cCould not find the kit you wanted; opening the Kit Selector.");
            new KitSelector(player);
            return;
        }

        kit.apply(player);
    }
}

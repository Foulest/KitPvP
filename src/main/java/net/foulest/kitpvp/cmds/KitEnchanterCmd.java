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

import net.foulest.kitpvp.menus.KitEnchanter;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.jetbrains.annotations.NotNull;

/**
 * Command for opening the Kit Enchanter.
 *
 * @author Foulest
 */
public class KitEnchanterCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "enchanter", aliases = "kitenchanter", description = "Opens the Kit Enchanter.", usage = "/enchanter", inGameOnly = true, permission = "kitpvp.kitenchanter")
    public void onCommand(@NotNull CommandArgs args) {
        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /enchanter");
            return;
        }

        new KitEnchanter(args.getPlayer());
    }
}

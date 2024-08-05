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

import lombok.NoArgsConstructor;
import net.foulest.kitpvp.menus.KitShop;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for opening the Kit Shop.
 *
 * @author Foulest
 * @project KitPvP
 */
@NoArgsConstructor
public class KitShopCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "kitshop", aliases = "shop", description = "Opens the Kit Shop.", usage = "/kitshop", inGameOnly = true, permission = "kitpvp.kitshop")
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();

        if (!Regions.isInSafezone(player.getLocation())) {
            MessageUtil.messagePlayer(player, ConstantUtil.NOT_IN_SPAWN);
            return;
        }

        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /kitshop");
            return;
        }

        new KitShop(args.getPlayer(), 0);
    }
}

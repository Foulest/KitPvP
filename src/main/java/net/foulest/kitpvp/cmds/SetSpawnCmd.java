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

import lombok.Data;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for setting the spawn point.
 *
 * @author Foulest
 */
@Data
public class SetSpawnCmd {

    @Command(name = "setspawn", usage = "/setspawn", description = "Sets the spawn point.",
            permission = "kitpvp.setspawn", inGameOnly = true)
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        Location location = player.getLocation();

        // Checks if the player is in a safezone.
        if (!Regions.isInSafezone(location)) {
            MessageUtil.messagePlayer(player, "&cYou cannot set spawn outside of a safezone.");
            return;
        }

        // Sets the spawn point to the player's location.
        Spawn.setLocation(location);
        MessageUtil.messagePlayer(player, "&aSpawn has been set.");
    }
}

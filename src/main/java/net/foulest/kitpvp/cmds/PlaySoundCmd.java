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
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Command for testing different sounds.
 *
 * @author Foulest
 */
@Data
public class PlaySoundCmd {

    @Command(name = "playsound", description = "Plays a specified or random sound.",
            permission = "kitpvp.playsound", usage = "/playsound [name]", inGameOnly = true)
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Checks if the player is null.
        if (player == null) {
            MessageUtil.messagePlayer(sender, ConstantUtil.IN_GAME_ONLY);
            return;
        }

        Location location = player.getLocation();

        // Prints the usage message.
        if (args.length() > 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /playsound [name]");
            return;
        }

        // Plays a random sound.
        if (args.length() == 0) {
            // Randomly get a sound from the list.
            Sound[] sounds = Sound.values();
            Sound sound = sounds[(int) (Math.random() * sounds.length)];
            String soundName = sound.name();

            player.playSound(location, sound, 1.0F, 1.0F);
            MessageUtil.messagePlayer(sender, "&aPlayed random sound: &e" + soundName);
            return;
        }

        // Plays a specified sound.
        String soundName = args.getArgs(0).toUpperCase(Locale.ROOT);
        Sound sound = Sound.valueOf(soundName);

        player.playSound(location, sound, 1.0F, 1.0F);
        MessageUtil.messagePlayer(sender, "&aPlayed specified sound: &e" + soundName);
    }
}

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

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Main command for KitPvP.
 *
 * @author Foulest
 */
@Getter
@Setter
public class KitPvPCmd {

    /**
     * Handles the command logic.
     *
     * @param args The command arguments.
     */
    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "kitpvp", description = "Main command for KitPvP.",
            permission = "kitpvp.main", usage = "/kitpvp")
    public void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();

        // No additional arguments, display help menu.
        if (args.length() == 0) {
            handleHelp(sender, args);
            return;
        }

        // Handle sub-commands.
        String subCommand = args.getArgs(0);

        if (subCommand.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("kitpvp.reload")
                    && !(sender instanceof ConsoleCommandSender)) {
                MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                return;
            }

            if (args.length() != 1) {
                MessageUtil.messagePlayer(sender, "&cUsage: /kitpvp reload");
                return;
            }

            Settings.loadSettings();
            MessageUtil.messagePlayer(sender, "&aReloaded the config files successfully.");
        } else {
            handleHelp(sender, args);
        }
    }

    /**
     * Handles the help command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private static void handleHelp(@NotNull CommandSender sender, CommandArgs args) {
        if (!sender.hasPermission("kitpvp.main")
                && !(sender instanceof ConsoleCommandSender)) {
            MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
            return;
        }

        // A list of available commands with their usages.
        List<String> commands = Collections.singletonList(
                "&f/kitpvp reload &7- Reloads the config."
        );

        int itemsPerPage = 4;
        int maxPages = (int) Math.ceil((double) commands.size() / itemsPerPage);
        int page = 1;

        if (args.length() > 1) {
            try {
                page = Integer.parseInt(args.getArgs(1));
            } catch (NumberFormatException ex) {
                MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
                return;
            }
        }

        if (page > maxPages || page < 1) {
            MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
            return;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(commands.size(), startIndex + itemsPerPage);

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&eKitPvP Help &7(Page " + page + "/" + maxPages + ")");

        for (int i = startIndex; i < endIndex; i++) {
            MessageUtil.messagePlayer(sender, commands.get(i));
        }

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&7Type &f/kitpvp help <page> &7for more commands.");
        MessageUtil.messagePlayer(sender, "");
    }
}

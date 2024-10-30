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
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

/**
 * Command for coloring leather armor with an RGB hex.
 * Useful for testing armor colors for new kits.
 *
 * @author Foulest
 */
@Data
public class ArmorColorCmd {

    @Command(name = "armorcolor", description = "Colors your chestplate with an RGB hex.",
            permission = "kitpvp.armorcolor", usage = "/armorcolor [hex]", inGameOnly = true)
    public static void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();
        Player player = args.getPlayer();

        // Prints the usage message.
        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /armorcolor [hex]");
            return;
        }

        String hex = args.getArgs(0);

        if (hex.length() != 6) {
            MessageUtil.messagePlayer(sender, "&cInvalid hex.");
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack invChestplate = inventory.getChestplate();

        if (invChestplate.getType() != Material.LEATHER_CHESTPLATE) {
            MessageUtil.messagePlayer(sender, "&cYou can't color that chestplate.");
            return;
        }

        ItemStack chestplate = new ItemBuilder(invChestplate)
                .color(Color.fromRGB(Integer.parseInt(hex, 16))).getItem();

        inventory.setChestplate(chestplate);
        player.updateInventory();

        MessageUtil.messagePlayer(sender, "&aColor 0x" + hex + " has been applied.");
    }
}

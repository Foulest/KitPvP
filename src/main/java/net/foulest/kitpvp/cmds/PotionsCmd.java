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
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Command for setting your healing item to Potions.
 *
 * @author Foulest
 * @project KitPvP
 */
@NoArgsConstructor
public class PotionsCmd {

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "potions", aliases = "pots", description = "Sets your healing item to Potions.",
            usage = "/potions", inGameOnly = true, permission = "kitpvp.potions")
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (!Regions.isInSafezone(player.getLocation())) {
            MessageUtil.messagePlayer(player, ConstantUtil.NOT_IN_SPAWN);
            return;
        }

        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /pots");
            return;
        }

        if (!playerData.isUsingSoup()) {
            MessageUtil.messagePlayer(args.getSender(), "&cYou are already using Potions.");
            return;
        }

        playerData.setUsingSoup(false);
        MessageUtil.messagePlayer(player, "&aYou are now using Potions.");

        if (playerData.getActiveKit() == null) {
            ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
            player.getInventory().setItem(6, healingItem);
            player.updateInventory();
        } else {
            playerData.getActiveKit().apply(player);
        }
    }
}

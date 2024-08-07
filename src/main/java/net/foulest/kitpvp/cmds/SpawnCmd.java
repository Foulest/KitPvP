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
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.BlockUtil;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Command for teleporting to spawn.
 *
 * @author Foulest
 * @project KitPvP
 */
@NoArgsConstructor
public class SpawnCmd {

    private static final int SECONDS_TO_WAIT = 5;

    @SuppressWarnings("MethodMayBeStatic")
    @Command(name = "spawn", description = "Teleports you to spawn.",
            usage = "/spawn", inGameOnly = true, permission = "kitpvp.spawn")
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Checks if the player is in combat.
        if (CombatTag.isInCombat(player)) {
            MessageUtil.messagePlayer(args.getPlayer(), ConstantUtil.COMBAT_TAGGED);
            return;
        }

        // Checks if the player is on the ground.
        if (!BlockUtil.isOnGroundOffset(player, 0.001)) {
            MessageUtil.messagePlayer(args.getPlayer(), ConstantUtil.NOT_ON_GROUND);
            return;
        }

        // Checks if the player is in spawn.
        if (Regions.isInSafezone(player.getLocation())) {
            Spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
            MessageUtil.messagePlayer(player, MessageUtil.colorize(ConstantUtil.TELEPORTED_TO_SPAWN));
            return;
        }

        // Checks if the player is already teleporting to spawn.
        if (playerData.getTeleportToSpawnTask() != null) {
            MessageUtil.messagePlayer(player, MessageUtil.colorize("&cYou are already teleporting to spawn."));
            return;
        }

        // Teleports the player to spawn.
        playerData.setTeleportToSpawnTask(new BukkitRunnable() {
            int counter;
            int remainingSeconds = SECONDS_TO_WAIT;

            @Override
            public void run() {
                ++counter;

                if (counter == SECONDS_TO_WAIT) {
                    Spawn.teleport(player);
                    player.getInventory().setHeldItemSlot(0);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);
                    MessageUtil.messagePlayer(player, MessageUtil.colorize(ConstantUtil.TELEPORTED_TO_SPAWN));
                    playerData.setTeleportToSpawnTask(null);
                    cancel();
                    return;
                }

                player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
                --remainingSeconds;
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&eTeleporting to spawn in &a"
                        + remainingSeconds + " seconds&e..."));
            }
        }.runTaskTimer(KitPvP.instance, 0L, 20L));
    }
}

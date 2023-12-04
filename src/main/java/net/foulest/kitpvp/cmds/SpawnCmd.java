package net.foulest.kitpvp.cmds;

import lombok.NonNull;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for teleporting to spawn.
 */
public class SpawnCmd {

    @Command(name = "spawn", description = "Teleports you to spawn.",
            usage = "/spawn", inGameOnly = true, permission = "kitpvp.spawn")
    public void onCommand(@NonNull CommandArgs args) {
        Player player = args.getPlayer();
        Entity entityPlayer = player.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (CombatLog.isInCombat(player)) {
            MessageUtil.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
            return;
        }

        if (!entityPlayer.isOnGround()) {
            MessageUtil.messagePlayer(args.getPlayer(), "&cYou need to be on the ground.");
            return;
        }

        if (Regions.isInSafezone(player.getLocation())) {
            Spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
            MessageUtil.messagePlayer(player, MessageUtil.colorize("&aTeleported to spawn."));
            return;
        }

        if (playerData.getTeleportingToSpawn() != null) {
            MessageUtil.messagePlayer(player, MessageUtil.colorize("&cYou are already teleporting to spawn."));
            return;
        }

        // Teleports the player to spawn.
        playerData.setTeleportingToSpawn(new BukkitRunnable() {
            int counter = 0;
            int remainingSeconds = 5;

            @Override
            public void run() {
                int counterLimit = 5;

                if (counter++ == counterLimit) {
                    Spawn.teleport(player);
                    player.getInventory().setHeldItemSlot(0);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);
                    MessageUtil.messagePlayer(player, MessageUtil.colorize("&aTeleported to spawn."));
                    playerData.setTeleportingToSpawn(null);
                    cancel();
                    return;
                }

                player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&eTeleporting to spawn in &a"
                        + remainingSeconds-- + " seconds&e..."));
            }
        }.runTaskTimer(KitPvP.instance, 0L, 20L));
    }
}

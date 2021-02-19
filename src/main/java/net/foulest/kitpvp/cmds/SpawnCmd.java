package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.Spawn;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class SpawnCmd {

    private static final Spawn SPAWN = Spawn.getInstance();
    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();

    @Command(name = "spawn", description = "Teleports you to spawn.", usage = "/spawn", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        Entity entityPlayer = player.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (COMBAT_LOG.isInCombat(player)) {
            MessageUtil.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
            return;
        }

        if (!entityPlayer.isOnGround()) {
            MessageUtil.messagePlayer(args.getPlayer(), "&cYou need to be on the ground.");
            return;
        }

        if (Regions.getInstance().isInSafezone(player)) {
            SPAWN.teleport(player);
            player.getInventory().setHeldItemSlot(0);
            MessageUtil.messagePlayer(player, MessageUtil.colorize("&aTeleported to spawn."));
            return;
        }

        if (playerData.isTeleportingToSpawn()) {
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
                    SPAWN.teleport(player);
                    player.getInventory().setHeldItemSlot(0);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);
                    MessageUtil.messagePlayer(player, MessageUtil.colorize("&aTeleported to spawn."));
                    playerData.setTeleportingToSpawn(null);
                    cancel();
                    return;
                }

                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&eTeleporting to spawn in &a"
                        + remainingSeconds-- + " seconds&e..."));
            }
        }.runTaskTimer(KITPVP, 0L, 20L));
    }
}

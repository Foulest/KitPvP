package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.Spawn;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnCmd {

    private final Spawn spawn = Spawn.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final CombatLog combatLog = CombatLog.getInstance();

    @Command(name = "spawn", description = "Teleports you to spawn.", usage = "/spawn", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        Entity entityPlayer = player.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (combatLog.isInCombat(player)) {
            MiscUtils.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
            return;
        }

        if (!entityPlayer.isOnGround()) {
            MiscUtils.messagePlayer(args.getPlayer(), "&cYou need to be on the ground.");
            return;
        }

        if (Regions.getInstance().isInSafezone(player)) {
            spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
            MiscUtils.messagePlayer(player, MiscUtils.colorize("&aTeleported to spawn."));
            return;
        }

        if (playerData.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(player, MiscUtils.colorize("&cYou are already teleporting to spawn."));
            return;
        }

        // Teleports the player to spawn.
        playerData.setTeleportingToSpawn(new BukkitRunnable() {
            int counter = 0;
            int remainingSeconds = 5;

            public void run() {
                if (counter++ == 5) {
                    spawn.teleport(player);
                    player.getInventory().setHeldItemSlot(0);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);
                    MiscUtils.messagePlayer(player, MiscUtils.colorize("&aTeleported to spawn."));
                    playerData.setTeleportingToSpawn(null);
                    cancel();
                    return;
                }

                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, MiscUtils.colorize("&eTeleporting to spawn in &a" + remainingSeconds-- + " seconds&e..."));
            }
        }.runTaskTimer(kitPvP, 0L, 20L));
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.utils.KitUser;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.Spawn;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnCmd {

    private final Spawn spawn = Spawn.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final CombatLog combatLog = CombatLog.getInstance();

    @Command(name = "spawn", description = "Teleports you to spawn.", usage = "/spawn", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        Location playerLoc = player.getLocation();
        KitUser kitUser = KitUser.getInstance(player);

        if (combatLog.isInCombat(player)) {
            MiscUtils.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
            return;
        }

        if (kitUser.isInSafezone()) {
            spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
            MiscUtils.messagePlayer(player, MiscUtils.colorize("&aTeleported to spawn."));
            return;
        }

        if (kitUser.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(player, MiscUtils.colorize("&cYou are already teleporting to spawn."));
            return;
        }

        kitUser.setTeleportingToSpawn(new BukkitRunnable() {
            final long teleportTime = System.currentTimeMillis();
            long pastValue = 6;

            public void run() {
                long secondsDiff = Math.round(((teleportTime - System.currentTimeMillis()) + 5500) / 1000.0D);
                // Cancels teleport if the player enters combat.
                if (combatLog.isInCombat(player)) {
                    MiscUtils.messagePlayer(player, MiscUtils.colorize("&cTeleportation cancelled, you entered combat."));
                    cancel();
                    kitUser.setTeleportingToSpawn(null);
                    return;
                }

                // Teleports the player to spawn.
                if (secondsDiff == 0) {
                    spawn.teleport(player);
                    player.getInventory().setHeldItemSlot(0);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);
                    MiscUtils.messagePlayer(player, MiscUtils.colorize("&aTeleported to spawn."));
                    cancel();
                    kitUser.setTeleportingToSpawn(null);
                    return;
                }

                // Sends a message and sound to the player.
                if (secondsDiff != pastValue) {
                    player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                    MiscUtils.messagePlayer(player, MiscUtils.colorize("&eTeleporting to spawn in &a" + secondsDiff + " seconds&e..."));
                }

                pastValue = secondsDiff;
            }
        }.runTaskTimer(kitPvP, 0L, 0L));
    }
}

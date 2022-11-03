package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Combat tag/log handling
 */
public class CombatLog {

    private static final Map<Player, BukkitTask> combatScheduler = new HashMap<>();
    private static final Map<Player, Integer> combatHandler = new HashMap<>();
    private static final Map<Player, Player> lastAttacker = new HashMap<>();

    public static void markForCombat(Player damager, Player receiver) {
        List<Player> players = new ArrayList<>(Arrays.asList(damager, receiver));

        for (Player player : players) {
            PlayerData playerData = PlayerData.getInstance(player);

            if (playerData == null) {
                player.kickPlayer("Disconnected");
                return;
            }

            // Handles combat tagging for the receiver.
            if (!isInCombat(player)) {
                combatHandler.put(player, 15);

                combatScheduler.put(player, new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isInCombat(player)) {
                            if (getRemainingTime(player) > 1) {
                                combatHandler.replace(player, getRemainingTime(player), getRemainingTime(player) - 1);
                            } else {
                                remove(player);
                            }
                        }
                    }
                }.runTaskTimer(KitPvP.instance, 0L, 20L));
            } else {
                combatHandler.replace(player, getRemainingTime(player), 15);
            }

            // Cancels the damager's pending teleportation when taking damage for.
            if (playerData.getTeleportingToSpawn() != null) {
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&cTeleportation cancelled, you entered combat."));
                playerData.getTeleportingToSpawn().cancel();
                playerData.setTeleportingToSpawn(null);
            }
        }

        // Sets the last attackers.
        lastAttacker.put(receiver, damager);
    }

    public static boolean isInCombat(Player player) {
        return combatHandler.containsKey(player);
    }

    public static int getRemainingTime(Player player) {
        return !isInCombat(player) ? -1 : combatHandler.get(player);
    }

    public static Player getLastAttacker(Player player) {
        return lastAttacker.get(player);
    }

    public static void remove(Player player) {
        combatHandler.remove(player);

        if (combatScheduler.containsKey(player)) {
            combatScheduler.get(player).cancel();
            combatScheduler.remove(player);
        }

        lastAttacker.remove(player);
    }
}

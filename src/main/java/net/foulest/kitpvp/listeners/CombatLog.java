package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.lunar.LunarClientAPI;
import net.foulest.kitpvp.utils.lunar.objects.LCCooldown;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CombatLog {

    private static final CombatLog instance = new CombatLog();
    private final Map<Player, BukkitTask> combatScheduler = new HashMap<>();
    private final Map<Player, Integer> combatHandler = new HashMap<>();
    private final Map<Player, Player> lastAttacker = new HashMap<>();
    private final LunarClientAPI lunarAPI = LunarClientAPI.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();

    public static CombatLog getInstance() {
        return instance;
    }

    public void markForCombat(Player damager, Player receiver) {
        PlayerData damagerData = PlayerData.getInstance(damager);
        PlayerData receiverData = PlayerData.getInstance(receiver);

        // Displays Lunar Client combat tag cooldowns.
        if (lunarAPI.isRunningLunarClient(damager)) {
            lunarAPI.sendCooldown(damager, new LCCooldown("Combat Tag", 15L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }
        if (lunarAPI.isRunningLunarClient(receiver)) {
            lunarAPI.sendCooldown(receiver, new LCCooldown("Combat Tag", 15L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }

        // Handles combat tagging for the damager.
        if (!isInCombat(damager)) {
            combatHandler.put(damager, 15);

            combatScheduler.put(damager, new BukkitRunnable() {
                public void run() {
                    if (isInCombat(damager)) {
                        if (getRemainingTime(damager) > 1) {
                            combatHandler.replace(damager, getRemainingTime(damager), getRemainingTime(damager) - 1);
                        } else {
                            remove(damager);
                        }
                    }
                }
            }.runTaskTimer(kitPvP, 0L, 20L));
        } else {
            combatHandler.replace(damager, getRemainingTime(damager), 15);
        }

        // Cancels the damager's pending teleportation when taking damage for.
        if (damagerData.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(damager, MiscUtils.colorize("&cTeleportation cancelled, you entered combat."));
            damagerData.getTeleportingToSpawnTask().cancel();
            damagerData.setTeleportingToSpawn(null);
        }

        // Handles combat tagging for the receiver.
        if (!isInCombat(receiver)) {
            combatHandler.put(receiver, 15);

            combatScheduler.put(receiver, new BukkitRunnable() {
                public void run() {
                    if (isInCombat(receiver)) {
                        if (getRemainingTime(receiver) > 1) {
                            combatHandler.replace(receiver, getRemainingTime(receiver), getRemainingTime(receiver) - 1);
                        } else {
                            remove(receiver);
                        }
                    }
                }
            }.runTaskTimer(kitPvP, 0L, 20L));
        } else {
            combatHandler.replace(receiver, getRemainingTime(receiver), 15);
        }

        // Cancels the receiver's pending teleportation when moving.
        if (receiverData.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(receiver, MiscUtils.colorize("&cTeleportation cancelled, you entered combat."));
            receiverData.getTeleportingToSpawnTask().cancel();
            receiverData.setTeleportingToSpawn(null);
        }

        // Sets the last attackers.
        lastAttacker.put(receiver, damager);
    }

    public boolean isInCombat(Player player) {
        return combatHandler.containsKey(player);
    }

    public int getRemainingTime(Player player) {
        return !isInCombat(player) ? -1 : combatHandler.get(player);
    }

    public Player getLastAttacker(Player player) {
        return lastAttacker.get(player);
    }

    public void remove(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);

        // Clears Combat Tag data.
        combatHandler.remove(player);
        if (combatScheduler.containsKey(player)) {
            combatScheduler.get(player).cancel();
        }
        combatScheduler.remove(player);
        lastAttacker.remove(player);

        // Clears Lunar Client combat tag cooldowns.
        if (lunarAPI.isRunningLunarClient(player)) {
            lunarAPI.clearCooldown(player, new LCCooldown("Combat Tag", 0L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }
    }
}

package net.foulest.kitpvp.listeners;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCCooldown;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class CombatLog {

    private static final CombatLog INSTANCE = new CombatLog();
    private static final Map<Player, BukkitTask> COMBAT_SCHEDULER = new HashMap<>();
    private static final Map<Player, Integer> COMBAT_HANDLER = new HashMap<>();
    private static final Map<Player, Player> LAST_ATTACKER = new HashMap<>();
    private static final LunarClientAPI LUNAR_API = LunarClientAPI.getInstance();
    private static final KitPvP KITPVP = KitPvP.getInstance();

    public static CombatLog getInstance() {
        return INSTANCE;
    }

    public void markForCombat(Player damager, Player receiver) {
        PlayerData damagerData = PlayerData.getInstance(damager);
        PlayerData receiverData = PlayerData.getInstance(receiver);

        // Displays Lunar Client combat tag cooldowns.
        if (LUNAR_API.isRunningLunarClient(damager)) {
            LUNAR_API.sendCooldown(damager, new LCCooldown("Combat Tag", 15L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }
        if (LUNAR_API.isRunningLunarClient(receiver)) {
            LUNAR_API.sendCooldown(receiver, new LCCooldown("Combat Tag", 15L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }

        // Handles combat tagging for the damager.
        if (!isInCombat(damager)) {
            COMBAT_HANDLER.put(damager, 15);

            COMBAT_SCHEDULER.put(damager, new BukkitRunnable() {
                @Override
                public void run() {
                    if (isInCombat(damager)) {
                        if (getRemainingTime(damager) > 1) {
                            COMBAT_HANDLER.replace(damager, getRemainingTime(damager), getRemainingTime(damager) - 1);
                        } else {
                            remove(damager);
                        }
                    }
                }
            }.runTaskTimer(KITPVP, 0L, 20L));
        } else {
            COMBAT_HANDLER.replace(damager, getRemainingTime(damager), 15);
        }

        // Cancels the damager's pending teleportation when taking damage for.
        if (damagerData.isTeleportingToSpawn()) {
            MessageUtil.messagePlayer(damager, MessageUtil.colorize("&cTeleportation cancelled, you entered combat."));
            damagerData.getTeleportingToSpawnTask().cancel();
            damagerData.setTeleportingToSpawn(null);
        }

        // Handles combat tagging for the receiver.
        if (!isInCombat(receiver)) {
            COMBAT_HANDLER.put(receiver, 15);

            COMBAT_SCHEDULER.put(receiver, new BukkitRunnable() {
                @Override
                public void run() {
                    if (isInCombat(receiver)) {
                        if (getRemainingTime(receiver) > 1) {
                            COMBAT_HANDLER.replace(receiver, getRemainingTime(receiver), getRemainingTime(receiver) - 1);
                        } else {
                            remove(receiver);
                        }
                    }
                }
            }.runTaskTimer(KITPVP, 0L, 20L));
        } else {
            COMBAT_HANDLER.replace(receiver, getRemainingTime(receiver), 15);
        }

        // Cancels the receiver's pending teleportation when moving.
        if (receiverData.isTeleportingToSpawn()) {
            MessageUtil.messagePlayer(receiver, MessageUtil.colorize("&cTeleportation cancelled, you entered combat."));
            receiverData.getTeleportingToSpawnTask().cancel();
            receiverData.setTeleportingToSpawn(null);
        }

        // Sets the last attackers.
        LAST_ATTACKER.put(receiver, damager);
    }

    public boolean isInCombat(Player player) {
        return COMBAT_HANDLER.containsKey(player);
    }

    public int getRemainingTime(Player player) {
        return !isInCombat(player) ? -1 : COMBAT_HANDLER.get(player);
    }

    public Player getLastAttacker(Player player) {
        return LAST_ATTACKER.get(player);
    }

    public void remove(Player player) {
        COMBAT_HANDLER.remove(player);

        if (COMBAT_SCHEDULER.containsKey(player)) {
            COMBAT_SCHEDULER.get(player).cancel();
            COMBAT_SCHEDULER.remove(player);
        }

        LAST_ATTACKER.remove(player);

        // Clears Lunar Client combat tag cooldowns.
        if (LUNAR_API.isRunningLunarClient(player)) {
            LUNAR_API.clearCooldown(player, new LCCooldown("Combat Tag", 0L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }
    }
}

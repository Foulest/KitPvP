package net.foulest.kitpvp.listeners;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCCooldown;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.KitUser;
import net.foulest.kitpvp.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CombatLog {

    private static final CombatLog instance = new CombatLog();
    private final Map<Player, Integer> combatScheduler = new HashMap<>();
    private final Map<Player, Integer> combatHandler = new HashMap<>();
    private final Map<Player, Player> lastAttacker = new HashMap<>();
    private final LunarClientAPI lunarAPI = LunarClientAPI.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();

    public static CombatLog getInstance() {
        return instance;
    }

    public void markForCombat(Player damager, Player receiver) {
        KitUser damagerUser = KitUser.getInstance(damager);
        KitUser receiverUser = KitUser.getInstance(receiver);

        // Displays Lunar Client combat tag cooldowns.
        if (damagerUser.isOnLunar()) {
            lunarAPI.sendCooldown(damager, new LCCooldown("Combat Tag", 15L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }
        if (receiverUser.isOnLunar()) {
            lunarAPI.sendCooldown(receiver, new LCCooldown("Combat Tag", 15L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }

        // Handles combat tagging for the damager.
        if (!isInCombat(damager)) {
            combatHandler.put(damager, 15);
            combatScheduler.put(damager, Bukkit.getScheduler().scheduleSyncRepeatingTask(kitPvP, () -> {
                if (isInCombat(damager)) {
                    if (getRemainingTime(damager) > 1) {
                        combatHandler.replace(damager, getRemainingTime(damager), getRemainingTime(damager) - 1);
                    } else {
                        remove(damager);
                    }
                }
            }, 0L, 20L));
        } else {
            combatHandler.replace(damager, getRemainingTime(damager), 15);
        }

        // Cancels the damager's pending teleportation when taking damage for.
        if (damagerUser.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(damager, MiscUtils.colorize("&cTeleportation cancelled, you entered combat."));
            damagerUser.getTeleportingToSpawnTask().cancel();
            damagerUser.setTeleportingToSpawn(null);
        }

        // Handles combat tagging for the receiver.
        if (!isInCombat(receiver)) {
            combatHandler.put(receiver, 15);
            combatScheduler.put(receiver, Bukkit.getScheduler().scheduleSyncRepeatingTask(kitPvP, () -> {
                if (isInCombat(receiver)) {
                    if (getRemainingTime(receiver) > 1) {
                        combatHandler.replace(receiver, getRemainingTime(receiver), getRemainingTime(receiver) - 1);
                    } else {
                        remove(receiver);
                    }
                }
            }, 0L, 20L));
        } else {
            combatHandler.replace(receiver, getRemainingTime(receiver), 15);
        }

        // Cancels the receiver's pending teleportation when moving.
        if (receiverUser.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(receiver, MiscUtils.colorize("&cTeleportation cancelled, you entered combat."));
            receiverUser.getTeleportingToSpawnTask().cancel();
            receiverUser.setTeleportingToSpawn(null);
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
        KitUser user = KitUser.getInstance(player);

        combatHandler.remove(player);

        if (combatScheduler.containsKey(player)) {
            Bukkit.getScheduler().cancelTask(combatScheduler.get(player));
        }

        combatScheduler.remove(player);
        lastAttacker.remove(player);

        // Clears Lunar Client combat tag cooldowns.
        if (user.isOnLunar()) {
            lunarAPI.clearCooldown(player, new LCCooldown("Combat Tag", 0L, TimeUnit.SECONDS, Material.IRON_SWORD));
        }
    }
}

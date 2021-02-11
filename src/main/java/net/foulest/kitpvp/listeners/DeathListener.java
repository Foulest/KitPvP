package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.kits.Kit;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;

public class DeathListener implements Listener {

    private static final KitPvP kitPvP = KitPvP.getInstance();
    private static final KitListener kitListener = KitListener.getInstance();
    private static final Spawn spawn = Spawn.getInstance();
    private static final CombatLog combatLog = CombatLog.getInstance();

    public static void handleDeath(Player receiver, boolean onPlayerQuit) {
        CraftPlayer craftPlayer = (CraftPlayer) receiver;
        PacketPlayInClientCommand packet = new PacketPlayInClientCommand();
        PlayerData receiverData = PlayerData.getInstance(receiver);
        Kit currentKit = receiverData.getKit();
        Vector vec = new Vector();

        // Cancels deaths while in staff mode.
        if (receiver.hasMetadata("staffMode")) {
            return;
        }

        // Removes potential player created Wolves.
        if (receiverData.hasKit()) {
            if (currentKit.getName().equals("Tamer")) {
                for (Entity entity : Bukkit.getWorld(receiver.getWorld().getUID()).getEntities()) {
                    if (entity.getType() == EntityType.WOLF) {
                        Wolf wolf = (Wolf) entity;

                        if (wolf.getOwner() == receiver) {
                            wolf.remove();
                        }
                    }
                }
            }

            // Removes potential player created Iron Golems.
            if (currentKit.getName().equals("Summoner")) {
                for (Entity entity : Bukkit.getWorld(receiver.getWorld().getUID()).getEntities()) {
                    if (entity.isValid() && entity.getType() == EntityType.IRON_GOLEM) {
                        IronGolem ironGolem = (IronGolem) entity;

                        if (ironGolem.hasMetadata(receiver.getName())) {
                            ironGolem.remove();
                        }
                    }
                }
            }
        }

        // On-death blood splatter effect.
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

        // Sets the player's current kit and adds a death.
        receiverData.setPreviousKit(currentKit);
        receiverData.addDeath();

        // Runs specific code if the player is killed by another player.
        if (combatLog.getLastAttacker(receiver) != null) {
            Player damager = combatLog.getLastAttacker(receiver);
            PlayerData damagerData = PlayerData.getInstance(damager);

            // Adds a kill to the damager.
            damagerData.addKill();
            damagerData.addKillstreak();
            damager.playSound(damager.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damagerData.getKillstreak() >= 5 && damagerData.getKillstreak() % 5 == 0) {
                // Sends all online players a killstreak message in chat.
                MiscUtils.broadcastMessage("&a" + damager.getName() + " &eis on a &a" + damagerData.getKillstreak() + " &ekillstreak!");

                // Re-adds the damager's kit items.
                damager.getInventory().clear();
                damagerData.getKit().apply(damager);
                damager.updateInventory();
            }

            // Gives the damager coins and experience.
            int rewardAmount = 5 * (damagerData.getKillstreak() / 5);
            int coinsGiven = ConfigManager.get().getInt("kill.coins-bonus") + rewardAmount;
            int experienceGiven = ConfigManager.get().getInt("kill.experience-bonus") + rewardAmount;
            damagerData.addCoins(coinsGiven);
            damagerData.addExperience(experienceGiven);

            // Saves the damager's stats.
            damagerData.saveStats();

            // Prints kill messages to both the damager and receiver.
            MiscUtils.messagePlayer(receiver.getPlayer(), "&eYou were killed by &c" + damager.getName()
                    + " &eon &6" + String.format("%.01f", damager.getHealth()) + "❤&e.");
            MiscUtils.messagePlayer(damager, "&eYou killed &a" + receiver.getPlayer().getName()
                    + "&e for &a" + coinsGiven + " coins &eand &a" + experienceGiven + " exp&e.");
        } else {
            MiscUtils.messagePlayer(receiver, "&cYou killed yourself.");
        }

        // Clears cooldowns.
        receiverData.clearCooldowns();

        // Sends all online players a killstreak message in chat.
        if (receiverData.getKillstreak() >= 5) {
            MiscUtils.broadcastMessage("&a" + receiver.getPlayer().getName() + " &edied and lost their &a"
                    + receiverData.getKillstreak() + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        combatLog.remove(receiver);

        // Removes the player from a Vampire's drained effects list.
        kitListener.drainedEffects.remove(receiver.getUniqueId());

        // Prevents the player from reaching the respawn screen using NMS.
        if (!onPlayerQuit) {
            try {
                Field a = PacketPlayInClientCommand.class.getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
                (craftPlayer.getHandle()).playerConnection.a(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Removes knockback before teleporting the player to spawn.
            new BukkitRunnable() {
                public void run() {
                    receiver.setVelocity(vec);
                }
            }.runTaskLater(kitPvP, 1L);

            // Teleports the player to spawn.
            spawn.teleport(receiver);
            receiver.getInventory().setHeldItemSlot(0);
            receiver.playSound(receiver.getLocation(), Sound.FALL_BIG, 0.5f, 0.0f);
        }

        // Resets the player's killstreak.
        receiverData.resetKillStreak();

        // Saves the receiver's stats with the database.
        receiverData.saveStats();
    }
}

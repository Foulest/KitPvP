package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.ConfigManager;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Spawn;
import net.foulest.kitpvp.utils.kits.Kit;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class DeathListener implements Listener {

    private static final String FEATHER_FALLING_METADATA = "featherFalling";
    private static final String PROTECTION_METADATA = "protection";
    private static final String SHARPNESS_METADATA = "sharpness";
    private static final String POWER_METADATA = "power";
    private static final int MIN_KILLSTREAK = 5;
    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static final Spawn SPAWN = Spawn.getInstance();
    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();

    public static void handleDeath(Player receiver, boolean onPlayerQuit) {
        CraftPlayer craftPlayer = (CraftPlayer) receiver;
        PacketPlayInClientCommand packet = new PacketPlayInClientCommand();
        PlayerData receiverData = PlayerData.getInstance(receiver);
        Kit currentKit = receiverData.getKit();
        Vector vec = new Vector();

        // Cancels deaths while in flying.
        if (receiver.getAllowFlight()) {
            return;
        }

        // Removes potential player created Wolves.
        if (receiverData.hasKit()) {
            String tamerKitName = "Tamer";
            String summonerKitName = "Summoner";

            if (currentKit.getName().equals(tamerKitName)) {
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
            if (currentKit.getName().equals(summonerKitName)) {
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

        // Removes certain metadata from the player.
        receiver.removeMetadata(FEATHER_FALLING_METADATA, KITPVP);
        receiver.removeMetadata(PROTECTION_METADATA, KITPVP);
        receiver.removeMetadata(POWER_METADATA, KITPVP);
        receiver.removeMetadata(SHARPNESS_METADATA, KITPVP);

        // Runs specific code if the player is killed by another player.
        if (COMBAT_LOG.getLastAttacker(receiver) != null) {
            Player damager = COMBAT_LOG.getLastAttacker(receiver);
            PlayerData damagerData = PlayerData.getInstance(damager);

            // Adds a kill to the damager.
            damagerData.addKill();
            damagerData.addKillstreak();
            damager.playSound(damager.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damagerData.getKillstreak() >= MIN_KILLSTREAK && damagerData.getKillstreak() % MIN_KILLSTREAK == 0) {
                // Sends all online players a killstreak message in chat.
                MessageUtil.broadcastMessage("&a" + damager.getName() + " &eis on a &a" + damagerData.getKillstreak() + " &ekillstreak!");

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

            // Removes the player's potential bounty.
            if (receiverData.getBounty() > 0 && receiverData.getBenefactor() != damager.getUniqueId()) {
                MessageUtil.messagePlayer(Bukkit.getPlayer(receiverData.getBenefactor()), "");
                MessageUtil.messagePlayer(Bukkit.getPlayer(receiverData.getBenefactor()),
                        " &eYour &a$" + receiverData.getBounty() + " &ebounty on &a" + receiver.getName()
                                + " &ewas claimed by &a" + damager.getName() + "&e.");
                MessageUtil.messagePlayer(Bukkit.getPlayer(receiverData.getBenefactor()), "");

                MessageUtil.messagePlayer(damager, "");
                MessageUtil.messagePlayer(damager,
                        " &eYou claimed the &a$" + receiverData.getBounty() + " &ebounty on &a"
                                + receiver.getName() + " &e's head.");
                MessageUtil.messagePlayer(damager, "");

                receiverData.removeBenefactor();
                receiverData.removeBounty();
            }

            // Saves the damager's stats.
            damagerData.saveStats();

            // Prints kill messages to both the damager and receiver.
            MessageUtil.messagePlayer(receiver.getPlayer(), "&eYou were killed by &c" + damager.getName()
                    + " &eon &6" + String.format("%.01f", damager.getHealth()) + "❤&e.");
            MessageUtil.messagePlayer(damager, "&eYou killed &a" + receiver.getPlayer().getName()
                    + "&e for &a" + coinsGiven + " coins &eand &a" + experienceGiven + " exp&e.");
        } else {
            MessageUtil.messagePlayer(receiver, "&cYou killed yourself.");
        }

        // Clears cooldowns.
        receiverData.clearCooldowns();

        // Sends all online players a killstreak message in chat.
        if (receiverData.getKillstreak() >= MIN_KILLSTREAK) {
            MessageUtil.broadcastMessage("&a" + receiver.getPlayer().getName() + " &edied and lost their &a"
                    + receiverData.getKillstreak() + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        COMBAT_LOG.remove(receiver);

        // Removes the player from a Vampire's drained effects list.
        KitListener.DRAINED_EFFECTS.remove(receiver.getUniqueId());

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
                @Override
                public void run() {
                    receiver.setVelocity(vec);
                }
            }.runTaskLater(KITPVP, 1L);

            // Teleports the player to spawn.
            SPAWN.teleport(receiver);
            receiver.getInventory().setHeldItemSlot(0);
            receiver.playSound(receiver.getLocation(), Sound.FALL_BIG, 0.5f, 0.0f);
        }

        // Resets the player's killstreak.
        receiverData.resetKillStreak();

        // Saves the receiver's stats with the database.
        receiverData.saveStats();
    }
}

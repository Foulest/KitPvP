package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.kits.Summoner;
import net.foulest.kitpvp.kits.Tamer;
import net.foulest.kitpvp.koth.KOTH;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.NMSUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.kits.Kit;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Handles all deaths
 */
public class DeathListener implements Listener {

    public static void handleDeath(Player receiver, boolean onPlayerQuit) {
        PlayerData receiverData = PlayerData.getInstance(receiver);

        if (receiverData == null) {
            receiver.kickPlayer("Disconnected");
            return;
        }

        Kit currentKit = receiverData.getKit();
        Vector vec = new Vector();

        // Cancels deaths while in flying.
        if (receiver.getAllowFlight()) {
            return;
        }

        // Removes potential player created Wolves.
        if (receiverData.getKit() != null) {
            if (currentKit instanceof Tamer) {
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
            if (currentKit instanceof Summoner) {
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
        receiverData.setDeaths(receiverData.getDeaths() + 1);

        // Sets the player's current kit and adds a death.
        if (KOTH.getActiveKoth() != null) {
            KOTH.playersInKOTH.get(KOTH.getActiveKoth()).remove(receiver);

            if (KOTH.getActiveKoth().getCapper() == receiver) {
                KOTH.getActiveKoth().setCapper(null);
                KOTH.getActiveKoth().setTimeLeft(KOTH.getActiveKoth().getCapTime());
            }
        }

        // Runs specific code if the player is killed by another player.
        if (CombatLog.getLastAttacker(receiver) != null && CombatLog.getLastAttacker(receiver) != receiver) {
            Player damager = CombatLog.getLastAttacker(receiver);
            PlayerData damagerData = PlayerData.getInstance(damager);

            if (damagerData == null) {
                damager.kickPlayer("Disconnected");
                return;
            }

            // Adds a kill to the damager.
            damagerData.setKills(damagerData.getKills() + 1);
            damagerData.addKillstreak();
            damager.playSound(damager.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damagerData.getKillstreak() >= 5 && damagerData.getKillstreak() % 5 == 0) {
                // Sends all online players a killstreak message in chat.
                MessageUtil.broadcast("&6" + damager.getName() + " &eis on a &6" + damagerData.getKillstreak() + " &ekillstreak!");

                // Re-adds the damager's kit items.
                damager.getInventory().clear();
                damagerData.getKit().apply(damager);
                damager.updateInventory();
            }

            // Gives the damager coins and experience.
            int rewardAmount = 5 * (damagerData.getKillstreak() / 5);
            int coinsGiven = Settings.killCoinBonus + rewardAmount;
            int experienceGiven = Settings.killExpBonus + rewardAmount;
            damagerData.addCoins(coinsGiven);
            damagerData.addExperience(experienceGiven);

            // Removes the player's potential bounty.
            if (receiverData.getBounty() > 0 && Bukkit.getPlayer(receiverData.getBenefactor()) != damager) {
                if (Bukkit.getPlayer(receiverData.getBenefactor()) != null
                        && Bukkit.getPlayer(receiverData.getBenefactor()).isOnline()) {
                    Player benefactor = Bukkit.getPlayer(receiverData.getBenefactor());

                    benefactor.playSound(benefactor.getLocation(), Sound.DONKEY_IDLE, 1.0f, 1.0f);
                    MessageUtil.messagePlayer(benefactor, "&aYour $" + receiverData.getBounty()
                            + " bounty on " + receiver.getName() + " was claimed by " + damager.getName() + ".");
                }

                damager.playSound(damager.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);
                damager.playSound(damager.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                MessageUtil.messagePlayer(damager, "&eYou claimed the &a$" + receiverData.getBounty()
                        + " &ebounty on &a" + receiver.getName() + "&e's head.");
                damagerData.setCoins(damagerData.getCoins() + receiverData.getBounty());

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
        if (receiverData.getKillstreak() >= 5) {
            MessageUtil.broadcast("&a" + receiver.getPlayer().getName() + " &edied and lost their &a"
                    + receiverData.getKillstreak() + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        CombatLog.remove(receiver);

        // Removes the player from a Vampire's drained effects list.
        KitListener.drainedEffects.remove(receiver.getUniqueId());

        if (!onPlayerQuit) {
            // Sets the player's experience bar (fixes a rare bug).
            receiverData.calcLevel(false);

            // Plays a death sound at the player's death location.
            receiver.playSound(receiver.getLocation(), Sound.FALL_BIG, 0.5f, 0.0f);

            // Removes knockback before teleporting the player to spawn.
            receiver.setVelocity(vec);
            new BukkitRunnable() {
                @Override
                public void run() {
                    receiver.setVelocity(vec);
                }
            }.runTaskLater(KitPvP.instance, 1L);

            // Sends a respawn packet to the player.
            NMSUtil.getConnection(receiver).a(new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN));

            // Teleports the player to spawn.
            receiver.getInventory().setHeldItemSlot(0);
            Spawn.teleport(receiver);
            receiver.getInventory().setHeldItemSlot(0);
        }

        // Removes enchantments from the player.
        if (receiverData.isFeatherFallingEnchant() || receiverData.isThornsEnchant()
                || receiverData.isProtectionEnchant() || receiverData.isKnockbackEnchant()
                || receiverData.isSharpnessEnchant() || receiverData.isPunchEnchant()
                || receiverData.isPowerEnchant()) {
            MessageUtil.messagePlayer(receiver, "&cYour enchantments were removed on death.");
            receiverData.setFeatherFallingEnchant(false);
            receiverData.setThornsEnchant(false);
            receiverData.setProtectionEnchant(false);
            receiverData.setKnockbackEnchant(false);
            receiverData.setSharpnessEnchant(false);
            receiverData.setPunchEnchant(false);
            receiverData.setPowerEnchant(false);
        }

        // Resets the player's killstreak.
        receiverData.setKillstreak(0);

        // Saves the receiver's stats with the database.
        receiverData.saveStats();
    }
}

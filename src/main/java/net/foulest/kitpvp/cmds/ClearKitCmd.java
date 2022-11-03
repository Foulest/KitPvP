package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for setting bounties on players, and checking your bounty status.
 * Some part of this is locked behind a paywall (permissions based).
 */
public class ClearKitCmd {

    public static void clearKit(PlayerData playerData) {
        Player player = playerData.getPlayer();

        playerData.setPreviousKit(playerData.getKit());
        playerData.clearCooldowns();
        playerData.setKit(null);

        player.setHealth(20);
        player.getInventory().setHeldItemSlot(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        playerData.giveDefaultItems();

        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
    }

    @Command(name = "clearkit", description = "Clears your kit.", usage = "/clearkit", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        if (!(args.getSender() instanceof Player)) {
            MessageUtil.messagePlayer(args.getSender(), "Only players can execute this command.");
            return;
        }

        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        // Clearing your own kit.
        if (args.length() == 0) {
            if (CombatLog.isInCombat(args.getPlayer())) {
                MessageUtil.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
                return;
            }

            if (Regions.isInSafezone(player.getLocation())) {
                if (playerData.getKit() == null) {
                    MessageUtil.messagePlayer(player, "&cYou do not have a kit selected.");
                    return;
                }

                clearKit(playerData);
                MessageUtil.messagePlayer(player, "&aYour kit has been cleared.");
                return;
            }

            MessageUtil.messagePlayer(player, "&cYou need to be in spawn to clear your kit.");
            return;
        }

        // Clearing kits from other players.
        if (args.getPlayer().hasPermission("kitpvp.clearkit.others")) {
            Player target = Bukkit.getPlayer(args.getArgs(1));
            PlayerData targetData = PlayerData.getInstance(target);

            if (target == null) {
                MessageUtil.messagePlayer(player, "&cThat player is not online.");
                return;
            }

            if (targetData == null) {
                target.kickPlayer("Disconnected");
                return;
            }

            if (targetData.getKit() == null) {
                MessageUtil.messagePlayer(target, "&cYou do not have a kit selected.");
                return;
            }

            clearKit(targetData);
            MessageUtil.messagePlayer(target, "&aYour kit has been cleared by a staff member.");
            MessageUtil.messagePlayer(player, "&aYou cleared " + target.getName() + "'s kit.");
        }
    }
}

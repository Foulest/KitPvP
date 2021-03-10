package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class ClearKitCmd {

    private static final String CLEAR_KIT_PERMISSION = "kitpvp.clearkit.others";
    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();
    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static final Regions REGIONS = Regions.getInstance();

    @Command(name = "clearkit", description = "Clears your kit.", usage = "/clearkit", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        if (!(args.getSender() instanceof Player)) {
            MessageUtil.messagePlayer(args.getSender(), "Only players can execute this command.");
            return;
        }

        Player sender = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(sender);

        // Clearing your own kit.
        if (args.length() == 0) {
            if (COMBAT_LOG.isInCombat(args.getPlayer())) {
                MessageUtil.messagePlayer(args.getPlayer(), "&cYou may not use this command while in combat.");
                return;
            }

            if (REGIONS.isInSafezone(sender)) {
                if (playerData.getKit() == null) {
                    MessageUtil.messagePlayer(sender, "&cYou do not have a kit selected.");
                    return;
                }

                clearKit(playerData);
                MessageUtil.messagePlayer(sender, "&aYour kit has been cleared.");
                return;
            }

            MessageUtil.messagePlayer(sender, "&cYou need to be in spawn to clear your kit.");
            return;
        }

        // Clearing kits from other players.
        if (args.getPlayer().hasPermission(CLEAR_KIT_PERMISSION)) {
            Player target = Bukkit.getPlayer(args.getArgs(1));

            if (target == null) {
                MessageUtil.messagePlayer(sender, "&cThat player is not online.");
                return;
            }

            PlayerData targetData = PlayerData.getInstance(target);
            if (targetData.getKit() == null) {
                MessageUtil.messagePlayer(target, "&cYou do not have a kit selected.");
                return;
            }

            clearKit(targetData);
            MessageUtil.messagePlayer(target, "&aYour kit has been cleared by a staff member.");
            MessageUtil.messagePlayer(sender, "&aYou cleared " + target.getName() + "'s kit.");
        }
    }

    public void clearKit(PlayerData playerData) {
        Player player = playerData.getPlayer();

        playerData.setPreviousKit(playerData.getKit());
        playerData.clearCooldowns();
        playerData.setKit(null);

        player.setHealth(20);
        player.getInventory().setHeldItemSlot(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        KITPVP.giveDefaultItems(player);

        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
    }
}

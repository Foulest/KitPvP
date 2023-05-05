package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for sending coins to other players.
 */
public class PayCmd {

    @Command(name = "pay", description = "Send coins to another player.",
            usage = "/pay <player> <amount>", inGameOnly = true, permission = "kitpvp.pay")
    public void onCommand(CommandArgs args) {
        if (args.length() == 2) {
            Player target = Bukkit.getPlayer(args.getArgs(0));

            if (target == null) {
                MessageUtil.messagePlayer(args.getSender(), args.getArgs(0) + " is not online.");
                return;
            }

            if (!StringUtils.isNumeric(args.getArgs(1))) {
                MessageUtil.messagePlayer(args.getSender(), "&c'" + args.getArgs(1) + "' is not a valid amount.");
                return;
            }

            int amount = Integer.parseInt(args.getArgs(1));

            if (amount < 0) {
                MessageUtil.messagePlayer(args.getPlayer(), "&cThe amount must be positive.");
                return;
            }

            Player player = args.getPlayer();
            Player sender = (Player) args.getSender();
            PlayerData targetData = PlayerData.getInstance(player);
            PlayerData senderData = PlayerData.getInstance(sender);

            if (senderData.getCoins() - amount <= 0) {
                MessageUtil.messagePlayer(sender, "&cYou don't have enough coins.");
                return;
            }

            targetData.setCoins(targetData.getCoins() + amount);
            senderData.removeCoins(amount);
            targetData.saveStats();

            if ((args.getSender() instanceof Player) && player == args.getSender()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cYou can't pay yourself.");
                return;
            }

            MessageUtil.messagePlayer(player, "&a" + player.getName() + " sent you " + amount + " coins!");
            MessageUtil.messagePlayer(args.getSender(), "&aYou sent " + player.getName() + " " + amount + " coins!");
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&cUsage: /pay <player> <amount>");
    }
}

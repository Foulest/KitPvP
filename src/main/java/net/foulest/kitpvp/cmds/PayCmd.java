package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class PayCmd {

    private static final String NEGATIVE = "-";

    @Command(name = "pay", description = "Send coins to another player.", usage = "/pay <player> <amount>",
            inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player target = Bukkit.getPlayer(args.getArgs(0));

        if (args.length() == 2) {
            if (target == null) {
                MessageUtil.messagePlayer(args.getSender(), args.getArgs(0) + " is not online.");
                return;
            }

            String amount = args.getArgs(1);

            if (!StringUtils.isNumeric(amount)) {
                MessageUtil.messagePlayer(args.getSender(), "&c'" + amount + "' is not a valid amount.");
                return;
            }

            if (amount.contains(NEGATIVE)) {
                MessageUtil.messagePlayer(args.getPlayer(), "&cThe amount must be positive.");
                return;
            }

            Player player = args.getPlayer();
            Player sender = (Player) args.getSender();
            PlayerData targetData = PlayerData.getInstance(target);
            PlayerData senderData = PlayerData.getInstance(sender);
            int oldAmount = targetData.getCoins();
            int check = senderData.getCoins() - Integer.parseInt(amount);

            if (check <= 0) {
                MessageUtil.messagePlayer(sender, "&cYou don't have enough coins.");
                return;
            }

            targetData.setCoins((Integer.parseInt(amount) + oldAmount));
            senderData.removeCoins(Integer.parseInt(amount));
            targetData.saveStats();

            if ((args.getSender() instanceof Player) && target == args.getSender()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MessageUtil.messagePlayer(target, "&cYou can't pay yourself.");
                return;
            }

            MessageUtil.messagePlayer(target, "&a" + player.getName() + " sent you " + amount + " coins!");
            MessageUtil.messagePlayer(args.getSender(), "&aYou sent " + target.getName() + " " + amount + " coins!");
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&cUsage: /pay <player> <amount>");
    }
}

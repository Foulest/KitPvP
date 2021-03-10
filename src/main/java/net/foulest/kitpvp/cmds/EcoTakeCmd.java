package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class EcoTakeCmd {

    @Command(name = "ecotake", description = "Takes from the balance of a player.", usage = "/ecotake <player> <amount>",
            permission = "kitpvp.ecotake")
    public void onCommand(CommandArgs args) {
        if (args.length() != 2) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /ecotake <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));

        if (target == null) {
            MessageUtil.messagePlayer(args.getSender(), args.getArgs(0) + " is not online.");
            return;
        }

        if (!StringUtils.isNumeric(args.getArgs(1))) {
            MessageUtil.messagePlayer(args.getSender(), "&c'" + args.getArgs(1) + "' is not a valid amount.");
            return;
        }

        PlayerData targetData = PlayerData.getInstance(target);
        int amount = Integer.parseInt(args.getArgs(1));

        targetData.removeCoins(amount);
        targetData.saveStats();

        if (args.getSender() instanceof Player && target == args.getSender()) {
            MessageUtil.messagePlayer(target, "&aYou set your balance to " + targetData.getCoins()
                    + " coins. &7(-" + amount + ")");
            return;
        }

        MessageUtil.messagePlayer(target, "&aYour balance was set to " + targetData.getCoins()
                + " coins. &7(-" + amount + ")");
        MessageUtil.messagePlayer(args.getSender(), "&aYou set " + target.getName() + "'s balance to "
                + targetData.getCoins() + " coins. &7(-" + amount + ")");
    }
}

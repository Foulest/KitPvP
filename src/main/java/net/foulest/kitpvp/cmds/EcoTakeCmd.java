package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for taking coins away from players.
 */
public class EcoTakeCmd {

    @Command(name = "ecotake", description = "Takes from the balance of a player.",
            usage = "/ecotake <player> <amount>", permission = "kitpvp.ecotake")
    public void onCommand(CommandArgs args) {
        if (args.length() != 2) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /ecotake <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        PlayerData targetData = PlayerData.getInstance(target);

        if (target == null) {
            MessageUtil.messagePlayer(args.getSender(), args.getArgs(0) + " is not online.");
            return;
        }

        if (!StringUtils.isNumeric(args.getArgs(1))) {
            MessageUtil.messagePlayer(args.getSender(), "&c'" + args.getArgs(1) + "' is not a valid amount.");
            return;
        }

        int amount = Integer.parseInt(args.getArgs(1));
        targetData.removeCoins(amount);

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

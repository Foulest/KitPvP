package net.foulest.kitpvp.cmds;

import lombok.NonNull;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
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
 * Command for setting a player's coins.
 */
public class EcoSetCmd {

    @Command(name = "ecoset", description = "Sets the balance of a player.",
            usage = "/ecoset <player> <amount>", permission = "kitpvp.ecoset")
    public void onCommand(@NonNull CommandArgs args) {
        if (args.length() != 2) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /ecoset <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (!target.isOnline()) {
            MessageUtil.messagePlayer(args.getSender(), args.getArgs(0) + " is not online.");
            return;
        }

        if (!StringUtils.isNumeric(args.getArgs(1))) {
            MessageUtil.messagePlayer(args.getSender(), "&c'" + args.getArgs(1) + "' is not a valid amount.");
            return;
        }

        int amount = Integer.parseInt(args.getArgs(1));
        targetData.setCoins(amount);

        if ((args.getSender() instanceof Player) && target == args.getSender()) {
            MessageUtil.messagePlayer(target, "&aYou set your balance to " + targetData.getCoins() + " coins.");
            return;
        }

        MessageUtil.messagePlayer(target, "&aYour balance was set to " + targetData.getCoins() + " coins.");
        MessageUtil.messagePlayer(args.getSender(), "&aYou set " + target.getName() + "'s balance to " + targetData.getCoins() + " coins.");
    }
}

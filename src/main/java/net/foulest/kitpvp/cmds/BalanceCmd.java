package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class BalanceCmd {

    @Command(name = "balance", aliases = {"bal", "money", "coins"}, description = "Shows your current balance.",
            usage = "/balance", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        CommandSender sender = args.getSender();

        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&fCoins: &6" + PlayerData.getInstance(player).getCoins());
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));

        if (target == null) {
            MessageUtil.messagePlayer(sender, "&cPlayer not found.");
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&f" + target.getName() + "'s Coins: &6" + PlayerData.getInstance(target).getCoins());
    }
}

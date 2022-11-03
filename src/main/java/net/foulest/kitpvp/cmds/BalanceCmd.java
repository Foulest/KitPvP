package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for displaying your current balance.
 */
public class BalanceCmd {

    @Command(name = "balance", aliases = {"bal", "money", "coins"},
            description = "Shows your current balance.", usage = "/balance", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        CommandSender sender = args.getSender();

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&fCoins: &6" + playerData.getCoins());
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        PlayerData targetData = PlayerData.getInstance(target);

        if (target == null) {
            MessageUtil.messagePlayer(sender, "&cPlayer not found.");
            return;
        }

        if (targetData == null) {
            target.kickPlayer("Disconnected");
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&f" + target.getName() + "'s Coins: &6" + targetData.getCoins());
    }
}

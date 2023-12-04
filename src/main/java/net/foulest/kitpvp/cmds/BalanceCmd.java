package net.foulest.kitpvp.cmds;

import lombok.NonNull;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
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
            description = "Shows your current balance.",
            permission = "kitpvp.balance", usage = "/balance", inGameOnly = true)
    public void onCommand(@NonNull CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        CommandSender sender = args.getSender();

        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&fCoins: &6" + playerData.getCoins());
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (!target.isOnline()) {
            MessageUtil.messagePlayer(sender, "&cPlayer not found.");
            return;
        }

        MessageUtil.messagePlayer(args.getSender(), "&f" + target.getName() + "'s Coins: &6" + targetData.getCoins());
    }
}

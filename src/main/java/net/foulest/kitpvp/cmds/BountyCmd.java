package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for placing bounties on other players.
 *
 * @author Foulest
 * @project KitPvP
 */
public class BountyCmd {

    @Command(name = "bounty", aliases = {"bounties"},
            description = "Allows players to place bounties on each other.",
            permission = "kitpvp.bounties", usage = "/bounty [player]", inGameOnly = true)
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Player benefactor = Bukkit.getPlayer(playerData.getBenefactor());

        if (args.length() == 0) {
            MessageUtil.messagePlayer(player, "");

            if (playerData.getBounty() == 0 || playerData.getBenefactor() == null || !benefactor.isOnline()) {
                MessageUtil.messagePlayer(player, " &aYou currently don't have a");
                MessageUtil.messagePlayer(player, " &abounty on your head.");
            } else {
                MessageUtil.messagePlayer(player, " &cYou currently have a &e$" + playerData.getBounty() + " &cbounty");
                MessageUtil.messagePlayer(player, " &con your head set by &e" + benefactor.getName() + "&c.");
            }

            MessageUtil.messagePlayer(player, "");

            if (player.hasPermission("kitpvp.bounties.place")) {
                MessageUtil.messagePlayer(player, " &fYou can place one on another player");
                MessageUtil.messagePlayer(player, " &fusing &e/bounty set <player> <amount>&f.");
                MessageUtil.messagePlayer(player, "");
            }
            return;
        }

        if (args.length() != 3 || !(args.getArgs(0).equals("set"))) {
            MessageUtil.messagePlayer(player, "&cUsage: /bounty set <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(1));
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (target.equals(player)) {
            MessageUtil.messagePlayer(player, "&cYou can't set a bounty on yourself.");
            return;
        }

        if (targetData.getBounty() != 0 || targetData.getBenefactor() != null) {
            MessageUtil.messagePlayer(player, "&c" + target.getName() + " already has a bounty on their head.");
            return;
        }

        if (!StringUtils.isNumeric(args.getArgs(2))) {
            MessageUtil.messagePlayer(player, "&c'" + args.getArgs(3) + "' is not a valid amount.");
            return;
        }

        int amount = Integer.parseInt(args.getArgs(2));
        int minCoins = 50;

        if (amount < minCoins) {
            MessageUtil.messagePlayer(player, "&cThe amount needs to be at least 50 coins.");
            return;
        }

        if (playerData.getCoins() - amount < 0) {
            MessageUtil.messagePlayer(player, "&cYou don't have enough coins.");
            return;
        }

        MessageUtil.messagePlayer(player, "&aYou set a $" + amount + " bounty on " + target.getName() + "'s head.");

        MessageUtil.messagePlayer(target, "");
        MessageUtil.messagePlayer(target, " &c" + player.getName() + " &eset a &c$" + amount + " &ebounty on your head.");
        MessageUtil.messagePlayer(target, "");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(target) && !online.equals(player)) {
                MessageUtil.messagePlayer(online, "&c" + player.getName() + " &eset a &c$" + amount + " &ebounty on &c" + target.getName() + "&e's head.");
            }
        }

        targetData.addBounty(amount, player.getUniqueId());
        playerData.removeCoins(amount);
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
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
public class BountyCmd {

    private static final String SET_NAME = "set";
    private static final String BOUNTIES_PERMISSION = "kitpvp.bounties";

    @Command(name = "bounty", aliases = {"bounties"}, description = "Shows your current balance.",
            usage = "/bounty [player]", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
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

            if (player.hasPermission(BOUNTIES_PERMISSION)) {
                MessageUtil.messagePlayer(player, " &fYou can place one on another player");
                MessageUtil.messagePlayer(player, " &fusing &e/bounty set <player> <amount>&f.");
            } else {
                MessageUtil.messagePlayer(player, " &eOnly &6Premium &emembers can set bounties.");
                MessageUtil.messagePlayer(player, " &eStore: &6store.kitpvp.io");
            }

            MessageUtil.messagePlayer(player, "");
            return;
        }

        if (args.length() != 3 || !(SET_NAME).equals(args.getArgs(0))) {
            MessageUtil.messagePlayer(player, "&cUsage: /bounty set <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(1));
        PlayerData targetData = PlayerData.getInstance(target);

        if (target == null) {
            MessageUtil.messagePlayer(player, "&cPlayer not found.");
            return;
        }

        if (target == player) {
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

        MessageUtil.messagePlayer(player, "&eYou set a &a$" + amount + " &ebounty on &a" + target.getName() + "&e's head.");

        MessageUtil.messagePlayer(target, "");
        MessageUtil.messagePlayer(target, " &c" + player.getName() + " &eset a &c$" + amount + " &ebounty on your head.");
        MessageUtil.messagePlayer(target, "");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != target && online != player) {
                MessageUtil.messagePlayer(online, "&a" + player.getName() + " &eset a &a$" + amount + " &ebounty on &a" + target.getName() + "&e's head.");
            }
        }

        targetData.setBounty(amount);
        targetData.setBenefactor(player.getUniqueId());
        playerData.removeCoins(amount);

        playerData.saveStats();
        targetData.saveStats();
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.menus.KitShop;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class KitShopCmd {

    @Command(name = "kitshop", aliases = {"shop"}, description = "Opens the Kit Shop.", usage = "/kitshop",
            inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (!Regions.getInstance().isInSafezone(player)) {
            MessageUtil.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /kitshop");
            return;
        }

        new KitShop(args.getPlayer(), 0);
    }
}

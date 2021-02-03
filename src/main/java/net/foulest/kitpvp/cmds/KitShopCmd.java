package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.kits.KitShop;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class KitShopCmd {

    @Command(name = "kitshop", aliases = {"shop"}, description = "Opens the Kit Shop.", usage = "/kitshop",
            inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (!Regions.getInstance().isInSafezone(player)) {
            MiscUtils.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() != 0) {
            MiscUtils.messagePlayer(args.getSender(), "&cUsage: /kitshop");
            return;
        }

        new KitShop(args.getPlayer(), 0);
    }
}

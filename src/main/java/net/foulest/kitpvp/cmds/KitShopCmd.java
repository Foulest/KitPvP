package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.menus.KitShop;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for opening the Kit Shop.
 *
 * @author Foulest
 * @project KitPvP
 */
public class KitShopCmd {

    @Command(name = "kitshop", aliases = {"shop"}, description = "Opens the Kit Shop.",
            usage = "/kitshop", inGameOnly = true, permission = "kitpvp.kitshop")
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();

        if (!Regions.isInSafezone(player.getLocation())) {
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

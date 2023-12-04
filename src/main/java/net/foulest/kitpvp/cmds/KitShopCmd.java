package net.foulest.kitpvp.cmds;

import lombok.NonNull;
import net.foulest.kitpvp.menus.KitShop;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for opening the Kit Shop menu.
 */
public class KitShopCmd {

    @Command(name = "kitshop", aliases = {"shop"}, description = "Opens the Kit Shop.",
            usage = "/kitshop", inGameOnly = true, permission = "kitpvp.kitshop")
    public void onCommand(@NonNull CommandArgs args) {
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

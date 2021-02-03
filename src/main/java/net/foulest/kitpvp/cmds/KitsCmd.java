package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.KitSelector;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.entity.Player;

public class KitsCmd {

    private final KitManager kitManager = KitManager.getInstance();

    @Command(name = "kits", aliases = "kit", usage = "/kits", description = "Shows available kits.", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (!Regions.getInstance().isInSafezone(player)) {
            MiscUtils.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() > 1) {
            MiscUtils.messagePlayer(player, "&cUsage: /kit [name]");
            return;
        }

        if (args.length() == 0) {
            new KitSelector(player);
            return;
        }

        if (kitManager.valueOf(args.getArgs(0)) == null) {
            MiscUtils.messagePlayer(player, "&cCould not find the kit you wanted; opening the Kit Selector.");
            new KitSelector(player);
            return;
        }

        kitManager.valueOf(args.getArgs(0)).apply(player);
    }
}

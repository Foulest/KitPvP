package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.StaffMode;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class StaffCmd {

    private final StaffMode staffMode = StaffMode.getInstance();

    @Command(name = "staff", description = "Toggle staff mode.", usage = "/staff", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        if (!(args.getSender() instanceof Player)) {
            MiscUtils.messagePlayer(args.getSender(), "Only players can execute this command.");
            return;
        }

        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (args.length() > 0) {
            MiscUtils.messagePlayer(player, "&cUsage: /staff");
            return;
        }

        staffMode.toggleStaffMode(player, !playerData.isInStaffMode(), false);
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.Spawn;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class SetSpawnCmd {

    private final Spawn spawn = Spawn.getInstance();

    @Command(name = "setspawn", usage = "/setspawn", description = "Sets the spawn point.",
            permission = "kitpvp.setspawn", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        spawn.setLocation(player.getLocation());
        MiscUtils.messagePlayer(player, "&aSpawn has been set.");
    }
}

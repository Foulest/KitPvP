package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.Spawn;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class SetSpawnCmd {

    private static final Spawn SPAWN = Spawn.getInstance();

    @Command(name = "setspawn", usage = "/setspawn", description = "Sets the spawn point.",
            permission = "kitpvp.setspawn", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        SPAWN.setLocation(player.getLocation());
        MessageUtil.messagePlayer(player, "&aSpawn has been set.");
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PotionsCmd {

    @Command(name = "potions", aliases = {"pots"}, description = "Sets your healing item to Potions.",
            usage = "/potions", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (!Regions.getInstance().isInSafezone(player)) {
            MiscUtils.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() != 0) {
            MiscUtils.messagePlayer(args.getSender(), "&cUsage: /pots");
            return;
        }

        if (!playerData.isUsingSoup()) {
            MiscUtils.messagePlayer(args.getSender(), "&cYou are already using Potions.");
            return;
        }

        playerData.setUsingSoup(false);
        playerData.saveStats();
        MiscUtils.messagePlayer(player, "&aYou are now using Potions.");

        if (!playerData.hasKit()) {
            ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUse Soup &7(Right Click)").build();
            player.getInventory().setItem(6, healingItem);
        } else {
            playerData.getKit().apply(player);
        }
    }
}

package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PotionsCmd {

    @Command(name = "potions", aliases = {"potion", "pot", "pots"}, description = "Sets your healing item to soup.",
            usage = "/pots", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        if (args.length() != 0) {
            MiscUtils.messagePlayer(args.getSender(), "&cUsage: /pots");
            return;
        }

        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (!playerData.isUsingSoup()) {
            MiscUtils.messagePlayer(args.getSender(), "&cYou are already using Potions.");
            return;
        }

        playerData.setUsingSoup(false);
        playerData.saveStats();
        MiscUtils.messagePlayer(player, "&aYou are now using Potions.");

        ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUse Soup &7(Right Click)").build();
        player.getInventory().setItem(6, healingItem);
    }
}

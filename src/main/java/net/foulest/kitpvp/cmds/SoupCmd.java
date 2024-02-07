package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Command for setting your healing item to Soup.
 *
 * @author Foulest
 * @project KitPvP
 */
public class SoupCmd {

    @Command(name = "soup", description = "Sets your healing item to Soup.",
            usage = "/soup", inGameOnly = true, permission = "kitpvp.soup")
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Checks if the player is in spawn.
        if (!Regions.isInSafezone(player.getLocation())) {
            MessageUtil.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /soup");
            return;
        }

        // Checks if the player is already using soup.
        if (playerData.isUsingSoup()) {
            MessageUtil.messagePlayer(args.getSender(), "&cYou are already using Soup.");
            return;
        }

        playerData.setUsingSoup(true);
        MessageUtil.messagePlayer(player, "&aYou are now using Soup.");

        if (playerData.getActiveKit() == null) {
            ItemStack healingItem = new ItemBuilder(Material.POTION).hideInfo().durability(16421).name("&aUsing Potions &7(Right Click)").getItem();
            player.getInventory().setItem(6, healingItem);
        } else {
            playerData.getActiveKit().apply(player);
        }
    }
}

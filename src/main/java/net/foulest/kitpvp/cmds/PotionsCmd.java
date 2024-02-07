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
import org.jetbrains.annotations.NotNull;

/**
 * Command for setting your healing item to Potions.
 *
 * @author Foulest
 * @project KitPvP
 */
public class PotionsCmd {

    @Command(name = "potions", aliases = {"pots"}, description = "Sets your healing item to Potions.",
            usage = "/potions", inGameOnly = true, permission = "kitpvp.potions")
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (!Regions.isInSafezone(player.getLocation())) {
            MessageUtil.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /pots");
            return;
        }

        if (!playerData.isUsingSoup()) {
            MessageUtil.messagePlayer(args.getSender(), "&cYou are already using Potions.");
            return;
        }

        playerData.setUsingSoup(false);
        MessageUtil.messagePlayer(player, "&aYou are now using Potions.");

        if (playerData.getActiveKit() == null) {
            ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
            player.getInventory().setItem(6, healingItem);
        } else {
            playerData.getActiveKit().apply(player);
        }
    }
}

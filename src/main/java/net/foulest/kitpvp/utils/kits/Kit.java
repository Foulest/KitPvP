package net.foulest.kitpvp.utils.kits;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public interface Kit {

    String getName();

    int getId();

    ItemStack getDisplayItem();

    List<ItemStack> getItems();

    ItemStack[] getArmor();

    PotionEffect[] getPotionEffects();

    String getDescription();

    double getAttack();

    double getDefense();

    int getCost();

    default void apply(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);

        // Checks if the player owns the kit they're trying to equip.
        if (!playerData.ownsKit(this)) {
            MiscUtils.messagePlayer(player, "&cYou do not own the " + getName() + " kit.");
            return;
        }

        // Sets the player's kit data.
        playerData.setKit(this);

        // Clears the player's inventory and armor.
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        // Clears the player's potion effects.
        for (PotionEffect effects : player.getActivePotionEffects()) {
            player.removePotionEffect(effects.getType());
        }

        // Sets the player's potion effects.
        if (getPotionEffects() != null) {
            for (PotionEffect effect : getPotionEffects()) {
                if (effect == null) {
                    break;
                }

                player.addPotionEffect(effect);
            }
        }

        // Sets the player's soup.
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            player.getInventory().addItem(new ItemBuilder(Material.MUSHROOM_SOUP).name("&fMushroom Stew").build());
        }

        // Sets the player's kit items.
        for (int i = 0; i < getItems().size(); ++i) {
            player.getInventory().setItem(i, getItems().get(i));
        }

        // Sets the player's armor.
        player.getInventory().setHelmet(getArmor()[0]);
        player.getInventory().setChestplate(getArmor()[1]);
        player.getInventory().setLeggings(getArmor()[2]);
        player.getInventory().setBoots(getArmor()[3]);
    }
}

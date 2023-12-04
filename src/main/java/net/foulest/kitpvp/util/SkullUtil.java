package net.foulest.kitpvp.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author deanveloper, Foulest
 * @project KitPvP
 * <p>
 * <a href="https://github.com/deanveloper/SkullCreator">...</a>
 */
public final class SkullUtil {

    private static Field blockProfileField;
    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    public static ItemStack createSkull() {
        try {
            return new ItemStack(Material.valueOf("PLAYER_HEAD"));
        } catch (IllegalArgumentException var1) {
            return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }
    }

    @Deprecated
    public static ItemStack itemFromName(@NonNull String name) {
        return itemWithName(createSkull(), name);
    }

    public static ItemStack itemFromUuid(@NonNull UUID id) {
        return itemWithUuid(createSkull(), id);
    }

    public static ItemStack itemFromUrl(@NonNull String url) {
        return itemWithUrl(createSkull(), url);
    }

    public static ItemStack itemFromBase64(@NonNull String base64) {
        return itemWithBase64(createSkull(), base64);
    }

    @Deprecated
    public static ItemStack itemWithName(@NonNull ItemStack item, @NonNull String name) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(name);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack itemWithUuid(@NonNull ItemStack item, @NonNull UUID id) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(Bukkit.getOfflinePlayer(id).getName());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack itemWithUrl(@NonNull ItemStack item, @NonNull String url) {
        return itemWithBase64(item, urlToBase64(url));
    }

    public static ItemStack itemWithBase64(@NonNull ItemStack item, @NonNull String base64) {
        if (item.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            mutateItemMeta(meta, base64);
            item.setItemMeta(meta);
            return item;
        } else {
            throw new IllegalArgumentException("Cannot set base64 on item. ItemMeta is not an instance of SkullMeta.");
        }
    }

    @Deprecated
    public static void blockWithName(@NonNull Block block, @NonNull String name) {
        Skull state = (Skull) block.getState();
        state.setOwner(name);
        state.update(false, false);
    }

    public static void blockWithUuid(@NonNull Block block, @NonNull UUID id) {
        setToSkull(block);
        Skull state = (Skull) block.getState();
        state.setOwner(Bukkit.getOfflinePlayer(id).getName());
        state.update(false, false);
    }

    public static void blockWithUrl(@NonNull Block block, @NonNull String url) {
        blockWithBase64(block, urlToBase64(url));
    }

    public static void blockWithBase64(@NonNull Block block, @NonNull String base64) {
        setToSkull(block);
        BlockState state = block.getState();

        if (state instanceof Skull) {
            Skull skull = (Skull) state;
            mutateBlockState(skull, base64);
            skull.update(false, false);
        } else {
            throw new IllegalArgumentException("Cannot set base64 on block. Block state is not an instance of Skull.");
        }
    }

    private static void setToSkull(@NonNull Block block) {
        try {
            block.setType(Material.valueOf("PLAYER_HEAD"), false);
        } catch (IllegalArgumentException var3) {
            block.setType(Material.valueOf("SKULL"), false);
            Skull state = (Skull) block.getState();
            state.setSkullType(SkullType.PLAYER);
            state.update(false, false);
        }
    }

    private static String urlToBase64(@NonNull String url) {
        URI actualUrl;

        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }

        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(toEncode.getBytes());
    }

    private static GameProfile makeProfile(@NonNull String b64) {
        UUID id = new UUID(b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode());
        GameProfile profile = new GameProfile(id, "aaaaa");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }

    private static void mutateBlockState(@NonNull Skull block, @NonNull String b64) {
        try {
            if (blockProfileField == null) {
                blockProfileField = block.getClass().getDeclaredField("profile");
                blockProfileField.setAccessible(true);
            }

            blockProfileField.set(block, makeProfile(b64));
        } catch (ReflectiveOperationException ex) {
            MessageUtil.log(Level.WARNING, "Failed to set block texture!");
            ex.printStackTrace();
        }
    }

    private static void mutateItemMeta(@NonNull SkullMeta meta, @NonNull String b64) {
        try {
            if (metaSetProfileMethod == null) {
                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                metaSetProfileMethod.setAccessible(true);
            }

            metaSetProfileMethod.invoke(meta, makeProfile(b64));

        } catch (ReflectiveOperationException ignored) {
            try {
                if (metaProfileField == null) {
                    metaProfileField = meta.getClass().getDeclaredField("profile");
                    metaProfileField.setAccessible(true);
                }

                metaProfileField.set(meta, makeProfile(b64));

            } catch (ReflectiveOperationException ex) {
                MessageUtil.log(Level.WARNING, "Failed to set item texture!");
                ex.printStackTrace();
            }
        }
    }
}

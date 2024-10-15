/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.util.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Custom SkullBuilder class that allows for easy skull creation.
 *
 * @author deanveloper
 * @see <a href="https://github.com/deanveloper/SkullCreator">SkullCreator GitHub</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SkullBuilder {

    private static Field blockProfileField;
    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    /**
     * Creates a default skull ItemStack.
     *
     * @return The default skull ItemStack.
     */
    @Contract(" -> new")
    private static @NotNull ItemStack createSkull() {
        try {
            return new ItemStack(Material.valueOf("PLAYER_HEAD"));
        } catch (IllegalArgumentException var1) {
            return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }
    }

    /**
     * Creates an ItemStack with the specified name.
     *
     * @param name The name of the player for the skull.
     * @return The ItemStack with the specified name.
     * @deprecated Use {@link #itemWithUuid(ItemStack, UUID)} instead.
     */
    @Deprecated
    public static @NotNull ItemStack itemFromName(String name) {
        return itemWithName(createSkull(), name);
    }

    /**
     * Creates an ItemStack with the specified UUID.
     *
     * @param id The UUID of the player for the skull.
     * @return The ItemStack with the specified UUID.
     */
    public static @NotNull ItemStack itemFromUuid(UUID id) {
        return itemWithUuid(createSkull(), id);
    }

    /**
     * Creates an ItemStack with the specified URL.
     *
     * @param url The URL of the player's skin for the skull.
     * @return The ItemStack with the specified URL.
     */
    public static @NotNull ItemStack itemFromUrl(String url) {
        return itemWithUrl(createSkull(), url);
    }

    /**
     * Creates an ItemStack with the specified Base64-encoded texture.
     *
     * @param base64 The Base64-encoded texture.
     * @return The ItemStack with the specified texture.
     */
    public static @NotNull ItemStack itemFromBase64(String base64) {
        return itemWithBase64(createSkull(), base64);
    }

    /**
     * Sets the owner name for an ItemStack.
     *
     * @param item The ItemStack to set the owner for.
     * @param name The name of the owner.
     * @return The ItemStack with the owner name set.
     * @deprecated Use {@link #itemWithUuid(ItemStack, UUID)} instead.
     */
    @Contract("_, _ -> param1")
    @Deprecated
    public static @NotNull ItemStack itemWithName(@NotNull ItemStack item, String name) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Sets the owner UUID for an ItemStack.
     *
     * @param item The ItemStack to set the owner UUID for.
     * @param id   The UUID of the owner.
     * @return The ItemStack with the owner UUID set.
     */
    @Contract("_, _ -> param1")
    private static @NotNull ItemStack itemWithUuid(@NotNull ItemStack item, UUID id) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(Bukkit.getOfflinePlayer(id).getName());
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Sets the owner URL for an ItemStack.
     *
     * @param item The ItemStack to set the owner URL for.
     * @param url  The URL of the owner's skin.
     * @return The ItemStack with the owner URL set.
     */
    @Contract("_, _ -> param1")
    private static @NotNull ItemStack itemWithUrl(ItemStack item, String url) {
        return itemWithBase64(item, urlToBase64(url));
    }

    /**
     * Sets the owner texture for an ItemStack using Base64 encoding.
     *
     * @param item   The ItemStack to set the owner texture for.
     * @param base64 The Base64-encoded texture.
     * @return The ItemStack with the owner texture set.
     */
    @Contract("_, _ -> param1")
    private static @NotNull ItemStack itemWithBase64(@NotNull ItemStack item, String base64) {
        if (item.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            mutateItemMeta(meta, base64);
            item.setItemMeta(meta);
            return item;
        } else {
            throw new IllegalArgumentException("Cannot set base64 on item. ItemMeta is not an instance of SkullMeta.");
        }
    }

    /**
     * Sets the owner name for a skull block.
     *
     * @param block The skull block to set the owner for.
     * @param name  The name of the owner.
     * @deprecated Use {@link #blockWithUuid(Block, UUID)} instead.
     */
    @Deprecated
    public static void blockWithName(@NotNull Block block, String name) {
        Skull state = (Skull) block.getState();
        state.setOwner(name);
        state.update(false, false);
    }

    /**
     * Sets the owner UUID for a skull block.
     *
     * @param block The skull block to set the owner UUID for.
     * @param id    The UUID of the owner.
     */
    private static void blockWithUuid(Block block, UUID id) {
        setToSkull(block);
        Skull state = (Skull) block.getState();
        state.setOwner(Bukkit.getOfflinePlayer(id).getName());
        state.update(false, false);
    }

    /**
     * Sets the owner URL for a skull block.
     *
     * @param block The skull block to set the owner URL for.
     * @param url   The URL of the owner's skin.
     */
    public static void blockWithUrl(Block block, String url) {
        blockWithBase64(block, urlToBase64(url));
    }

    /**
     * Sets the owner texture for a skull block using Base64 encoding.
     *
     * @param block  The skull block to set the owner texture for.
     * @param base64 The Base64-encoded texture.
     */
    private static void blockWithBase64(Block block, String base64) {
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

    /**
     * Sets the block type to a skull block.
     *
     * @param block The block to set to a skull block.
     */
    private static void setToSkull(@NotNull Block block) {
        try {
            block.setType(Material.valueOf("PLAYER_HEAD"), false);
        } catch (IllegalArgumentException var3) {
            block.setType(Material.valueOf("SKULL"), false);
            Skull state = (Skull) block.getState();
            state.setSkullType(SkullType.PLAYER);
            state.update(false, false);
        }
    }

    /**
     * Converts a URL to a Base64-encoded texture string.
     *
     * @param url The URL of the player's skin.
     * @return The Base64-encoded texture string.
     */
    private static String urlToBase64(String url) {
        URI actualUrl;

        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }

        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a GameProfile with the specified Base64-encoded texture.
     *
     * @param b64 The Base64-encoded texture.
     * @return The GameProfile with the specified texture.
     */
    private static @NotNull GameProfile makeProfile(@NotNull String b64) {
        UUID id = new UUID(b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode());
        GameProfile profile = new GameProfile(id, "aaaaa");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }

    /**
     * Mutates the block state of a skull block to set the texture.
     *
     * @param block The skull block to mutate.
     * @param b64   The Base64-encoded texture.
     */
    private static void mutateBlockState(Skull block, String b64) {
        try {
            if (blockProfileField == null) {
                blockProfileField = block.getClass().getDeclaredField("profile");
                blockProfileField.setAccessible(true);
            }

            blockProfileField.set(block, makeProfile(b64));
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Mutates the item meta of a skull ItemStack to set the texture.
     *
     * @param meta The item meta to mutate.
     * @param b64  The Base64-encoded texture.
     */
    private static void mutateItemMeta(SkullMeta meta, String b64) {
        try {
            if (metaProfileField == null) {
                metaProfileField = meta.getClass().getDeclaredField("profile");
                metaProfileField.setAccessible(true);
            }

            metaProfileField.set(meta, makeProfile(b64));
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }
}

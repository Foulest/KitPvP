package net.foulest.kitpvp.util;

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.util.data.ConcurrentStream;
import net.foulest.kitpvp.util.raytrace.BoundingBox;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class BlockUtil {

    public static boolean isPlayerInUnloadedChunk(@NotNull Player player) {
        return !player.getLocation().getWorld().isChunkLoaded(player.getLocation().getBlockX() >> 4,
                player.getLocation().getBlockZ() >> 4);
    }

    @Contract("_, _ -> new")
    public static @NotNull ConcurrentStream<Block> getCollidingBlocks(Player player, @NotNull BoundingBox boundingBox) {
        return new ConcurrentStream<>(boundingBox.getCollidingBlocks(player), false);
    }

    private static boolean collidesWithSolid(Player player, BoundingBox boundingBox) {
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType().isSolid()
                || block.getType() == Material.WATER_LILY
                || block.getType() == Material.FLOWER_POT
                || block.getType() == Material.CARPET
                || block.getType() == Material.SNOW
                || block.getType() == Material.SKULL);
    }

    public static boolean isOnGroundOffset(Player player, double offset) {
        if (isPlayerInUnloadedChunk(player)) {
            return true;
        }

        BoundingBox boundingBox = new BoundingBox(player).expand(0.0, 0.0, 0.0)
                .expandMin(0.0, offset, 0.0)
                .expandMax(0.0, -1.0, 0.0);
        return collidesWithSolid(player, boundingBox);
    }
}

package net.foulest.kitpvp.utils.lunar.objects;

import net.foulest.kitpvp.utils.lunar.LunarClientAPI;
import org.bukkit.Location;

import java.beans.ConstructorProperties;

public class LCWaypoint {

    private final String name;
    private final int x;
    private final int y;
    private final int z;
    private final String world;
    private final int color;
    private final boolean forced;
    private final boolean visible;

    public LCWaypoint(String name, Location location, int color, boolean forced, boolean visible) {
        this(name, location.getBlockX(), location.getBlockY(), location.getBlockZ(), LunarClientAPI.getInstance().getWorldIdentifier(location.getWorld()), color, forced, visible);
    }

    public LCWaypoint(String name, Location location, int color, boolean forced) {
        this(name, location.getBlockX(), location.getBlockY(), location.getBlockZ(), LunarClientAPI.getInstance().getWorldIdentifier(location.getWorld()), color, forced, true);
    }

    @ConstructorProperties({"name", "x", "y", "z", "world", "color", "forced", "visible"})
    public LCWaypoint(String name, int x, int y, int z, String world, int color, boolean forced, boolean visible) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.color = color;
        this.forced = forced;
        this.visible = visible;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof LCWaypoint)) {
            return false;
        }

        LCWaypoint other = (LCWaypoint) o;
        Object this$name = getName();
        Object other$name = other.getName();

        Label_0055:
        {
            if (this$name == null) {
                if (other$name == null) {
                    break Label_0055;
                }
            } else if (this$name.equals(other$name)) {
                break Label_0055;
            }

            return false;
        }

        if (getX() != other.getX()) {
            return false;
        }

        if (getY() != other.getY()) {
            return false;
        }

        if (getZ() != other.getZ()) {
            return false;
        }

        Object this$world = getWorld();
        Object other$world = other.getWorld();

        if (this$world == null) {
            if (other$world == null) {
                return getColor() == other.getColor() && isForced() == other.isForced() && isVisible() == other.isVisible();
            }
        } else if (this$world.equals(other$world)) {
            return getColor() == other.getColor() && isForced() == other.isForced() && isVisible() == other.isVisible();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int prime = 59;
        int result = 1;

        Object objName = getName();
        result = result * prime + ((objName == null) ? 43 : objName.hashCode());
        result = result * prime + getX();
        result = result * prime + getY();
        result = result * prime + getZ();

        Object objWorld = getWorld();
        result = result * prime + ((objWorld == null) ? 43 : objWorld.hashCode());
        result = result * prime + getColor();
        result = result * prime + (isForced() ? 79 : 97);
        result = result * prime + (isVisible() ? 79 : 97);

        return result;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public int getColor() {
        return color;
    }

    public boolean isForced() {
        return forced;
    }

    public boolean isVisible() {
        return visible;
    }
}

package net.foulest.kitpvp.utils.lunar.objects;

import com.google.common.base.Preconditions;
import org.bukkit.Material;

import java.util.concurrent.TimeUnit;

public final class LCCooldown {

    private final String message;
    private final long durationMs;
    private final Material icon;

    public LCCooldown(String message, long unitCount, TimeUnit unit, Material icon) {
        this.message = (String) Preconditions.checkNotNull((Object) message, "message");
        this.durationMs = unit.toMillis(unitCount);
        this.icon = (Material) Preconditions.checkNotNull((Object) icon, "icon");
    }

    public String getMessage() {
        return message;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Material getIcon() {
        return icon;
    }
}

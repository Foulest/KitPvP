package net.foulest.kitpvp.cooldown;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.foulest.kitpvp.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Cooldown {

    public Player player;
    public Kit kit;
    public Material itemType;
    @EqualsAndHashCode.Exclude
    public long duration;
}

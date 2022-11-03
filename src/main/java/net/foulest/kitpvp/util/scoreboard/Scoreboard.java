package net.foulest.kitpvp.util.scoreboard;

import dev.jcsoftware.jscoreboards.JGlobalMethodBasedScoreboard;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Scoreboard {

    @Getter
    private String name;
    @Getter
    private JGlobalMethodBasedScoreboard contents;
}

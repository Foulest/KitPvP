package net.foulest.kitpvp.utils.lunar.nethandler.obj;

public enum ServerRule {

    MINIMAP_STATUS("minimapStatus", String.class),
    SERVER_HANDLES_WAYPOINTS("serverHandlesWaypoints", Boolean.class),
    COMPETITIVE_GAME("competitiveGame", Boolean.class),
    SHADERS_DISABLED("shadersDisabled", Boolean.class),
    LEGACY_ENCHANTING("legacyEnchanting", Boolean.class),
    VOICE_ENABLED("voiceEnabled", Boolean.class);

    private final String id;
    private final Class type;

    ServerRule(String id, Class type) {
        this.id = id;
        this.type = type;
    }

    public static ServerRule getRule(String id) {
        for (ServerRule existing : values()) {
            if (existing.id.equals(id)) {
                return existing;
            }
        }

        return null;
    }

    public String getId() {
        return id;
    }

    public Class getType() {
        return type;
    }
}

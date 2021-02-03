package net.foulest.kitpvp.utils.lunar.nethandler.obj;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModSettings {

    public static final Gson GSON = new Gson();
    private final Map<String, ModSetting> modSettingsMap;

    public ModSettings() {
        modSettingsMap = new HashMap<>();
    }

    public ModSettings addModSetting(String modId, ModSetting setting) {
        modSettingsMap.put(modId, setting);
        return this;
    }

    public ModSetting getModSetting(String modId) {
        return modSettingsMap.get(modId);
    }

    public Map<String, ModSetting> getModSettings() {
        return modSettingsMap;
    }

    public static class ModSetting {
        private boolean enabled;
        private Map<String, Object> properties;

        public ModSetting() {
        }

        public ModSetting(boolean enabled, Map<String, Object> properties) {
            this.enabled = enabled;
            this.properties = properties;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return "ModSetting{enabled=" + enabled + ", properties=" + properties + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ModSetting that = (ModSetting) o;
            return enabled == that.enabled && Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enabled, properties);
        }
    }
}

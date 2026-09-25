package com.cheat.modules;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name;
    public final ModuleCategory category;
    private boolean enabled;
    private int bind;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, ModuleCategory category) {
        this(name, category, false);
    }

    public Module(String name, ModuleCategory category, boolean defaultEnabled) {
        this.name = name;
        this.category = category;
        this.enabled = defaultEnabled;
    }

    public String getName() {
        return name;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabledRaw(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public Module slider(String id, String label, double min, double max, double step, double defaultValue) {
        settings.add(new SliderSetting(id, label, min, max, step, defaultValue));
        return this;
    }

    public Module bool(String id, String label, boolean defaultValue) {
        settings.add(new BoolSetting(id, label, defaultValue));
        return this;
    }

    public boolean hasSetting(String id) {
        for (Setting s : settings) {
            if (s.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Setting getSetting(String id) {
        for (Setting s : settings) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException(name + " has no setting '" + id + "'");
    }

    public SliderSetting slider(String id) {
        return (SliderSetting) getSetting(id);
    }

    public BoolSetting bool(String id) {
        return (BoolSetting) getSetting(id);
    }

    public static abstract class Setting {
        public final String id;
        public final String label;

        Setting(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static class SliderSetting extends Setting {
        public double value;
        public final double min;
        public final double max;
        public final double step;

        SliderSetting(String id, String label, double min, double max, double step, double value) {
            super(id, label);
            this.min = min;
            this.max = max;
            this.step = step;
            this.value = value;
        }
    }

    public static class BoolSetting extends Setting {
        public boolean value;

        BoolSetting(String id, String label, boolean value) {
            super(id, label);
            this.value = value;
        }
    }
}
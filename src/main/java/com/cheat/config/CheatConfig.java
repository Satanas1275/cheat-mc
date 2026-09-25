package com.cheat.config;

import com.cheat.modules.Module;
import com.cheat.modules.ModuleRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheatConfig {
    public static int menuKey = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private static final String MOD_NAME = "cheat.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private CheatConfig() {
    }

    public static void load() {
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(MOD_NAME);
            if (!Files.exists(path)) {
                save();
                return;
            }
            try (Reader reader = Files.newBufferedReader(path)) {
                Data data = GSON.fromJson(reader, Data.class);
                if (data == null) {
                    return;
                }
                if (data.menuKey > 0) {
                    menuKey = data.menuKey;
                }
                if (data.modules != null) {
                    for (ModuleState st : data.modules) {
                        Module m = ModuleRegistry.find(st.name);
                        if (m == null) {
                            continue;
                        }
                        m.setEnabledRaw(st.enabled);
                        if (st.bind >= 0) {
                            m.setBind(st.bind);
                        }
                        if (st.settings != null) {
                            for (Map.Entry<String, Double> e : st.settings.entrySet()) {
                                if (!m.hasSetting(e.getKey())) {
                                    continue;
                                }
                                Module.Setting s = m.getSetting(e.getKey());
                                if (s instanceof Module.SliderSetting slider) {
                                    slider.value = e.getValue();
                                    slider.value = Math.max(slider.min, Math.min(slider.max, slider.value));
                                } else if (s instanceof Module.BoolSetting bool) {
                                    bool.value = e.getValue() > 0.5;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(MOD_NAME);
            Files.createDirectories(path.getParent());
            Data data = new Data();
            data.menuKey = menuKey;
            data.modules = new ArrayList<>();
            for (Module m : ModuleRegistry.all()) {
                ModuleState st = new ModuleState();
                st.name = m.getName();
                st.enabled = m.isEnabled();
                st.bind = m.getBind();
                st.settings = new HashMap<>();
                for (Module.Setting s : m.getSettings()) {
                    if (s instanceof Module.SliderSetting slider) {
                        st.settings.put(s.id, slider.value);
                    } else if (s instanceof Module.BoolSetting bool) {
                        st.settings.put(s.id, bool.value ? 1.0 : 0.0);
                    }
                }
                data.modules.add(st);
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static class Data {
        public int menuKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        public List<ModuleState> modules = new ArrayList<>();
    }

    public static class ModuleState {
        public String name = "";
        public boolean enabled = false;
        public int bind = 0;
        public Map<String, Double> settings = new HashMap<>();
    }
}
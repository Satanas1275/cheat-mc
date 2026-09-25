package com.cheat.modules;

import com.cheat.client.WallHackGlow;
import com.cheat.config.CheatConfig;

public final class ModuleController {
    private ModuleController() {
    }

    public static void setEnabled(Module module, boolean enabled) {
        module.setEnabledRaw(enabled);
        if (module == ModuleRegistry.WALL_HACK && !enabled) {
            WallHackGlow.clear();
        }
        CheatConfig.save();
    }

    public static void toggle(Module module) {
        setEnabled(module, !module.isEnabled());
    }
}
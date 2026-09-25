package com.cheat.input;

import com.cheat.config.CheatConfig;
import com.cheat.modules.Module;
import com.cheat.modules.ModuleRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheatKeys {
    private static boolean menuDown;
    private static boolean menuPressed;
    private static final Map<Module, Boolean> prevDown = new HashMap<>();
    private static final List<Module> pressed = new ArrayList<>();

    private CheatKeys() {
    }

    public static void tick() {
        pressed.clear();
        boolean menu = isDown(CheatConfig.menuKey);
        menuPressed = menu && !menuDown;
        menuDown = menu;
        for (Module m : ModuleRegistry.all()) {
            boolean down = m.getBind() > 0 && isDown(m.getBind());
            Boolean prev = prevDown.get(m);
            boolean was = prev != null && prev;
            if (down && !was) {
                pressed.add(m);
            }
            prevDown.put(m, down);
        }
    }

    public static boolean wasMenuPressed() {
        return menuPressed;
    }

    public static List<Module> getPressedModules() {
        return pressed;
    }

    private static boolean isDown(int key) {
        if (key <= 0) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        long handle = mc.getWindow().getWindow();
        return handle != 0L && InputConstants.isKeyDown(handle, key);
    }
}
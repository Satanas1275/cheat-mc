package com.cheat.modules;

import java.util.ArrayList;
import java.util.List;

public final class ModuleRegistry {
    public static final Module FLY = new Module("Fly", ModuleCategory.MOVEMENT)
            .slider("speed", "Vitesse", 0.1, 4.0, 0.05, 1.0);

    public static final Module AUTO_MLG = new Module("AutoMLG", ModuleCategory.MOVEMENT)
            .slider("height", "Déclenchement", 2.0, 64.0, 1.0, 10.0);

    public static final Module NO_FALL = new Module("No Fall", ModuleCategory.MOVEMENT, true)
            .slider("hover", "Hauteur au sol", 0.05, 0.5, 0.05, 0.1);

    public static final Module NO_HUNGER = new Module("Anti Hunger", ModuleCategory.PLAYER);

    public static final Module FREECAM = new Module("FreeCam", ModuleCategory.RENDER)
            .slider("speed", "Vitesse", 0.1, 3.0, 0.05, 0.9);

    public static final Module AIM_ASSIST = new Module("Aim Assist", ModuleCategory.COMBAT)
            .slider("range", "Portée", 1.0, 8.0, 0.1, 4.5)
            .slider("fov", "FOV", 5.0, 90.0, 1.0, 30.0)
            .slider("speed", "Vitesse", 0.05, 1.0, 0.05, 0.35)
            .bool("hold", "Tenir la souris requis", true);

    public static final Module REACH = new Module("Reach", ModuleCategory.COMBAT)
            .slider("range", "Extension", 0.0, 8.0, 0.1, 3.0);

    public static final Module CRITICALS = new Module("Criticals", ModuleCategory.COMBAT);

    public static final Module TRACER = new Module("Tracers", ModuleCategory.COMBAT)
            .slider("range", "Portée", 10.0, 256.0, 1.0, 64.0)
            .bool("players", "Joueurs", true)
            .bool("monsters", "Mobs hostiles", true)
            .bool("animals", "Animaux", true);

    public static final Module XRAY = new Module("X-Ray", ModuleCategory.RENDER)
            .slider("range", "Portée", 8.0, 64.0, 1.0, 32.0)
            .bool("ores", "Minerais", true)
            .bool("spawner", "Spawners", true)
            .bool("containers", "Coffres", false);

    public static final Module FULLBRIGHT = new Module("Fullbright", ModuleCategory.RENDER);

    public static final Module WALL_HACK = new Module("WallHack", ModuleCategory.RENDER)
            .slider("range", "Portée", 10.0, 128.0, 1.0, 64.0);

    private static final List<Module> ALL = List.of(
            AIM_ASSIST, REACH, CRITICALS, TRACER,
            FLY, AUTO_MLG, NO_FALL,
            NO_HUNGER,
            FREECAM, XRAY, FULLBRIGHT, WALL_HACK
    );

    private ModuleRegistry() {
    }

    public static List<Module> all() {
        return ALL;
    }

    public static List<Module> byCategory(ModuleCategory category) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) {
            if (m.getCategory() == category) {
                out.add(m);
            }
        }
        return out;
    }

    public static Module find(String name) {
        for (Module m : ALL) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
}
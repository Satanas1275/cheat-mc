package com.cheat.modules;

import java.util.ArrayList;
import java.util.List;

public final class ModuleRegistry {
    private static final List<String> DEFAULT_XRAY_BLOCKS = List.of(
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
            "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
            "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore",
            "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
            "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
            "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
            "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
            "minecraft:ancient_debris", "minecraft:nether_quartz_ore",
            "minecraft:spawner", "minecraft:chest", "minecraft:trapped_chest",
            "minecraft:ender_chest", "minecraft:barrel"
    );

    private static final List<String> DEFAULT_CONTAINER_BLOCKS = List.of(
            "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest",
            "minecraft:barrel", "minecraft:crafting_table", "minecraft:furnace",
            "minecraft:blast_furnace", "minecraft:smoker", "minecraft:hopper",
            "minecraft:shulker_box"
    );

    public static final Module FLY = new Module("Fly", ModuleCategory.MOVEMENT)
            .slider("speed", "Vitesse", 0.1, 4.0, 0.05, 1.0);

    public static final Module BOAT_FLY = new Module("Boat Fly", ModuleCategory.MOVEMENT)
            .slider("speed", "Vitesse", 0.1, 3.0, 0.05, 0.8)
            .slider("descent", "Vitesse de descente", 0.1, 3.0, 0.05, 0.15);

    public static final Module AUTO_MLG = new Module("AutoMLG", ModuleCategory.MOVEMENT)
            .slider("height", "Déclenchement", 2.0, 64.0, 1.0, 10.0);

    public static final Module NO_FALL = new Module("No Fall", ModuleCategory.MOVEMENT, true)
            .slider("hover", "Hauteur au sol", 0.05, 0.5, 0.05, 0.1);

    public static final Module NO_HUNGER = new Module("Anti Hunger", ModuleCategory.PLAYER);

    public static final Module AIR_PLACE = new Module("AirPlace", ModuleCategory.WORLD)
            .slider("range", "Distance", 1.0, 6.0, 0.05, 5.0);

    public static final Module THROUGH_WALLS = new Module("Through Walls", ModuleCategory.WORLD)
            .slider("range", "Portée", 1.0, 6.0, 0.05, 4.5)
            .bool("chest", "Coffre", true)
            .bool("trapped_chest", "Coffre piégé", true)
            .bool("ender_chest", "End Chest", true)
            .bool("barrel", "Tonneau", true)
            .bool("crafting_table", "Table de craft", true)
            .bool("furnace", "Four", true)
            .bool("blast_furnace", "Four à charbon", true)
            .bool("smoker", "Fumoir", true)
            .bool("hopper", "Hopper", true)
            .bool("shulker_box", "Shulker", true)
            .bool("advanced", "Mode avancé", false)
            .blockList("blocks", "Liste de blocs", DEFAULT_CONTAINER_BLOCKS);

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
            .bool("containers", "Coffres", false)
            .bool("advanced", "Mode avancé", false)
            .blockList("blocks", "Liste de blocs", DEFAULT_XRAY_BLOCKS);

    public static final Module BLOCK_ESP = new Module("Block ESP", ModuleCategory.RENDER)
            .slider("range", "Portée", 1.0, 64.0, 1.0, 24.0)
            .bool("chest", "Coffres", true)
            .bool("trapped_chest", "Coffres piégés", true)
            .bool("ender_chest", "End Chests", true)
            .bool("barrel", "Tonneaux", true)
            .bool("crafting_table", "Tables de craft", true)
            .bool("furnace", "Fours", true)
            .bool("blast_furnace", "Fours à charbon", true)
            .bool("smoker", "Fumoirs", true)
            .bool("hopper", "Hoppers", true)
            .bool("shulker_box", "Shulkers", true)
            .bool("advanced", "Mode avancé", false)
            .blockList("blocks", "Liste de blocs", DEFAULT_CONTAINER_BLOCKS);

    public static final Module FULLBRIGHT = new Module("Fullbright", ModuleCategory.RENDER);

    public static final Module WALL_HACK = new Module("WallHack", ModuleCategory.RENDER)
            .slider("range", "Portée", 10.0, 128.0, 1.0, 64.0)
            .bool("players", "Joueurs", true)
            .bool("monsters", "Mobs hostiles", true)
            .bool("animals", "Animaux", true);

    private static final List<Module> ALL = List.of(
            AIM_ASSIST, REACH, CRITICALS, TRACER,
            FLY, BOAT_FLY, AUTO_MLG, NO_FALL,
            NO_HUNGER, AIR_PLACE, THROUGH_WALLS,
            FREECAM, XRAY, BLOCK_ESP, FULLBRIGHT, WALL_HACK
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
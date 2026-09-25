package com.cheat.client;

import com.cheat.modules.ModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public final class WallHackGlow {
    private static final Set<Entity> glowed = new HashSet<>();

    private WallHackGlow() {
    }

    public static void clear() {
        for (Entity e : glowed) {
            e.setGlowingTag(false);
        }
        glowed.clear();
    }

    public static void tick(Minecraft mc, Player player) {
        if (!ModuleRegistry.WALL_HACK.isEnabled()) {
            clear();
            return;
        }
        clear();
        if (mc.level == null || player == null) {
            return;
        }
        double range = ModuleRegistry.WALL_HACK.slider("range").value;
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
                t -> t != player && t.isAlive() && !t.isInvisible())) {
            e.setGlowingTag(true);
            glowed.add(e);
        }
    }
}
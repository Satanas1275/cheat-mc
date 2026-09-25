package com.cheat.client;

import com.cheat.modules.ModuleRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class FreeCam {
    private static RemoteCameraEntity cam;
    private static Vec3 base = Vec3.ZERO;
    private static Vec3 offset = Vec3.ZERO;
    private static boolean active;
    private static CameraType prevCam;

    private FreeCam() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void sync(Minecraft mc, LocalPlayer player) {
        boolean want = ModuleRegistry.FREECAM.isEnabled();
        if (want && !active) {
            enter(mc, player);
        } else if (!want && active) {
            exit(mc);
        }
    }

    private static void enter(Minecraft mc, LocalPlayer player) {
        base = player.getEyePosition(1.0F);
        offset = Vec3.ZERO;
        if (cam == null) {
            cam = new RemoteCameraEntity(player.level);
        }
        cam.snapTo(base);
        copyRotation(player);
        prevCam = mc.options.getCameraType();
        mc.setCameraEntity(cam);
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        active = true;
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void exit(Minecraft mc) {
        if (mc.getCameraEntity() == cam) {
            mc.setCameraEntity(mc.player);
        }
        if (prevCam != null) {
            mc.options.setCameraType(prevCam);
        }
        active = false;
    }

    public static void tick(Minecraft mc, LocalPlayer player) {
        if (!active || cam == null) {
            return;
        }
        copyRotation(player);
        double yaw = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        double x = 0, y = 0, z = 0;
        if (mc.screen == null) {
            if (mc.options.keyUp.isDown()) {
                x += forward.x;
                z += forward.z;
            }
            if (mc.options.keyDown.isDown()) {
                x -= forward.x;
                z -= forward.z;
            }
            if (mc.options.keyRight.isDown()) {
                x += right.x;
                z += right.z;
            }
            if (mc.options.keyLeft.isDown()) {
                x -= right.x;
                z -= right.z;
            }
            if (mc.options.keyJump.isDown()) {
                y += 1;
            }
            if (mc.options.keyShift.isDown()) {
                y -= 1;
            }
        }
        double hor = Math.sqrt(x * x + z * z);
        if (hor > 0.0001) {
            x /= hor;
            z /= hor;
        }
        double speed = ModuleRegistry.FREECAM.slider("speed").value * 0.35;
        offset = offset.add(x * speed, y * speed, z * speed);
        cam.snapTo(base.add(offset));
    }

    private static void copyRotation(Player player) {
        cam.setYRot(player.getYRot());
        cam.setXRot(player.getXRot());
        cam.yRotO = player.getYRot();
        cam.xRotO = player.getXRot();
    }
}
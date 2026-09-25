package com.cheat.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RemoteCameraEntity extends Entity {
    public RemoteCameraEntity(Level level) {
        super(EntityType.PLAYER, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    public void snapTo(Vec3 pos) {
        this.setPos(pos.x, pos.y, pos.z);
        this.setOldPosAndRot();
    }
}
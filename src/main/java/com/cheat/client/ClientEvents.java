package com.cheat.client;

import com.cheat.CheatMod;
import com.cheat.config.CheatConfig;
import com.cheat.input.CheatKeys;
import com.cheat.modules.Module;
import com.cheat.modules.ModuleController;
import com.cheat.modules.ModuleRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CheatMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {

    static {
        CheatConfig.load();
    }

    private static final int CAT_ORES = 0;
    private static final int CAT_SPAWNER = 1;
    private static final int CAT_CONTAINERS = 2;

    private static final UUID REACH_UUID = new UUID(60425229727728100L, 1L);
    private static final UUID ATTACK_UUID = new UUID(60425229727728100L, 2L);

    private static boolean hadFly;
    private static boolean needRestore;
    private static boolean savedMayfly;
    private static boolean savedFlying;
    private static float savedFlySpeed;

    private static Double savedGamma;

    private static final List<BlockPos> xrayCache = new ArrayList<>();
    private static int xrayTicks;
    private static final Map<Block, int[]> xrayTargets = new HashMap<>();

    private ClientEvents() {
    }

    private static void buildXrayTargets() {
        if (!xrayTargets.isEmpty()) {
            return;
        }
        String[][] ores = {
                {"diamond_ore", "29D6E0"}, {"deepslate_diamond_ore", "29D6E0"},
                {"emerald_ore", "3CE64E"}, {"deepslate_emerald_ore", "3CE64E"},
                {"gold_ore", "FFD84A"}, {"deepslate_gold_ore", "FFD84A"}, {"nether_gold_ore", "FFD84A"},
                {"iron_ore", "D8AF93"}, {"deepslate_iron_ore", "D8AF93"},
                {"copper_ore", "D97B5B"}, {"deepslate_copper_ore", "D97B5B"},
                {"coal_ore", "838383"}, {"deepslate_coal_ore", "838383"},
                {"redstone_ore", "E14D4D"}, {"deepslate_redstone_ore", "E14D4D"},
                {"lapis_ore", "4C6FD6"}, {"deepslate_lapis_ore", "4C6FD6"},
                {"ancient_debris", "5A392A"}, {"nether_quartz_ore", "C9C9C9"}
        };
        for (String[] e : ores) {
            putXray(e[0], e[1], CAT_ORES);
        }
        putXray("spawner", "E14DE1", CAT_SPAWNER);
        String[][] containers = {
                {"chest", "C68B4E"}, {"trapped_chest", "B07A3E"},
                {"ender_chest", "3A6B8B"}, {"barrel", "8B5A2B"}
        };
        for (String[] e : containers) {
            putXray(e[0], e[1], CAT_CONTAINERS);
        }
    }

    private static void putXray(String id, String hex, int cat) {
        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft", id));
        if (b != null) {
            xrayTargets.put(b, new int[]{(int) Long.parseLong(hex, 16), cat});
        }
    }

    private static Player local() {
        return Minecraft.getInstance().player;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        CheatKeys.tick();
        for (Module m : CheatKeys.getPressedModules()) {
            ModuleController.setEnabled(m, !m.isEnabled());
        }
        if (CheatKeys.wasMenuPressed() && mc.screen == null) {
            mc.setScreen(new CheatScreen());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) {
            return;
        }
        if (event.side == LogicalSide.SERVER && event.player instanceof ServerPlayer sp) {
            applyReach(sp);
            if (ModuleRegistry.CRITICALS.isEnabled()) {
                sp.fallDistance = Math.max(sp.fallDistance, 0.5f);
                sp.setOnGround(false);
            }
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.player != mc.player || !(event.player instanceof LocalPlayer player)) {
            return;
        }

        applyReach(player);

        Module fb = ModuleRegistry.FULLBRIGHT;
        if (fb.isEnabled()) {
            if (savedGamma == null) {
                savedGamma = (Double) mc.options.gamma().get();
            }
            mc.options.gamma().set(1.0D);
        } else if (savedGamma != null) {
            mc.options.gamma().set(savedGamma);
            savedGamma = null;
        }

        FreeCam.sync(mc, player);
        if (FreeCam.isActive()) {
            player.input.forwardImpulse = 0;
            player.input.leftImpulse = 0;
            player.input.jumping = false;
            player.input.shiftKeyDown = false;
            player.setDeltaMovement(Vec3.ZERO);
            FreeCam.tick(mc, player);
        }

        if (ModuleRegistry.FLY.isEnabled()) {
            if (!hadFly) {
                savedMayfly = player.getAbilities().mayfly;
                savedFlying = player.getAbilities().flying;
                savedFlySpeed = player.getAbilities().getFlyingSpeed();
                hadFly = true;
                needRestore = true;
            }
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(0.05f * (float) ModuleRegistry.FLY.slider("speed").value);
        } else if (needRestore) {
            player.getAbilities().mayfly = savedMayfly;
            player.getAbilities().flying = savedFlying;
            player.getAbilities().setFlyingSpeed(savedFlySpeed);
            hadFly = false;
            needRestore = false;
        }

        noFallTick(mc, player);

        if (ModuleRegistry.NO_HUNGER.isEnabled()) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20f);
        }

        aimAssistTick(mc, player);
        WallHackGlow.tick(mc, player);
        xrayScanTick(mc, player);
        autoMlgTick(player);
    }

    private static void applyReach(Player player) {
        double ext = ModuleRegistry.REACH.isEnabled() ? ModuleRegistry.REACH.slider("range").value : 0.0;
        setAttr(player, ForgeMod.REACH_DISTANCE.get(), REACH_UUID, ext);
        setAttr(player, ForgeMod.ATTACK_RANGE.get(), ATTACK_UUID, ext);
    }

    private static void setAttr(Player player, Attribute attr, UUID uuid, double value) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) {
            return;
        }
        if (value > 0) {
            AttributeModifier existing = inst.getModifier(uuid);
            if (existing == null) {
                inst.addPermanentModifier(new AttributeModifier(uuid, "cheat_reach", value, AttributeModifier.Operation.ADDITION));
            } else if (existing.getAmount() != value) {
                inst.removeModifier(uuid);
                inst.addPermanentModifier(new AttributeModifier(uuid, "cheat_reach", value, AttributeModifier.Operation.ADDITION));
            }
        } else if (inst.getModifier(uuid) != null) {
            inst.removeModifier(uuid);
        }
    }

    private static void noFallTick(Minecraft mc, LocalPlayer player) {
        Module m = ModuleRegistry.NO_FALL;
        if (!m.isEnabled()) {
            return;
        }
        player.fallDistance = 0;
        player.setOnGround(true);
        double hover = m.slider("hover").value;
        Vec3 mot = player.getDeltaMovement();
        if (mot.y < 0) {
            BlockPos feet = player.blockPosition();
            BlockPos below = null;
            for (int dy = 1; dy <= 6; dy++) {
                BlockPos p = feet.below(dy);
                if (player.level.getBlockState(p).isSolidRender(player.level, p)) {
                    below = p;
                    break;
                }
            }
            if (below != null) {
                double groundTop = below.getY() + 1.0;
                double feetY = player.getY();
                if (feetY <= groundTop + hover + 0.05) {
                    player.setPos(player.getX(), groundTop + hover, player.getZ());
                    player.setDeltaMovement(mot.x, 0, mot.z);
                    player.setOnGround(true);
                }
            }
        }
    }

    private static void aimAssistTick(Minecraft mc, LocalPlayer player) {
        if (mc.screen != null || player.isSpectator()) {
            return;
        }
        Module m = ModuleRegistry.AIM_ASSIST;
        if (!m.isEnabled()) {
            return;
        }
        double range = m.slider("range").value;
        double fov = Math.toRadians(m.slider("fov").value);
        double speed = m.slider("speed").value;
        if (m.bool("hold").value && !mc.options.keyAttack.isDown()) {
            return;
        }
        Level level = player.level;
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        LivingEntity best = null;
        double bestAngle = fov;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
                t -> t != player && t.isAlive() && !t.isSpectator() && !t.isInvisible())) {
            Vec3 to = e.getEyePosition(1.0F).subtract(eye);
            double dist = to.length();
            if (dist > range || dist < 0.01) {
                continue;
            }
            double angle = Math.acos(Mth.clamp(look.dot(to.normalize()), -1.0, 1.0));
            if (angle < bestAngle) {
                bestAngle = angle;
                best = e;
            }
        }
        if (best == null) {
            return;
        }
        Vec3 target = best.getEyePosition(1.0F);
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 0.0001) {
            return;
        }
        float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float desiredPitch = (float) Math.toDegrees(Math.atan2(-dy, horiz));
        float yawDelta = Mth.wrapDegrees(desiredYaw - player.getYRot());
        float pitchDelta = desiredPitch - player.getXRot();
        player.setYRot(player.getYRot() + yawDelta * (float) speed);
        player.setXRot(player.getXRot() + pitchDelta * (float) speed);
    }

    private static boolean tracerVisible(LivingEntity e) {
        Module m = ModuleRegistry.TRACER;
        boolean isPlayer = e instanceof Player;
        boolean isHostile = !isPlayer && e.getType().getCategory() == MobCategory.MONSTER;
        if (isPlayer) {
            return m.bool("players").value;
        }
        if (isHostile) {
            return m.bool("monsters").value;
        }
        return m.bool("animals").value;
    }

    private static void xrayScanTick(Minecraft mc, LocalPlayer player) {
        if (!ModuleRegistry.XRAY.isEnabled()) {
            xrayCache.clear();
            return;
        }
        if (++xrayTicks < 10) {
            return;
        }
        xrayTicks = 0;
        buildXrayTargets();
        xrayCache.clear();
        Module xray = ModuleRegistry.XRAY;
        boolean wantOres = xray.bool("ores").value;
        boolean wantSpawner = xray.bool("spawner").value;
        boolean wantContainers = xray.bool("containers").value;
        double range = xray.slider("range").value;
        int centerX = Mth.floor(player.getX());
        int centerY = Mth.floor(player.getY());
        int centerZ = Mth.floor(player.getZ());
        int r = (int) Math.ceil(range);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -16; dy <= 16; dy++) {
                    BlockPos pos = new BlockPos(centerX + dx, centerY + dy, centerZ + dz);
                    Block block = player.level.getBlockState(pos).getBlock();
                    int[] info = xrayTargets.get(block);
                    if (info == null) {
                        continue;
                    }
                    boolean take = switch (info[1]) {
                        case CAT_ORES -> wantOres;
                        case CAT_SPAWNER -> wantSpawner;
                        default -> wantContainers;
                    };
                    if (take) {
                        xrayCache.add(pos);
                    }
                }
            }
        }
    }

    private static int lastMlgTick = -1000;

    private static void autoMlgTick(LocalPlayer player) {
        if (!ModuleRegistry.AUTO_MLG.isEnabled()) {
            return;
        }
        if (player.tickCount - lastMlgTick < 10) {
            return;
        }
        if (player.isInWater() || player.isInLava() || player.getAbilities().flying) {
            return;
        }
        if (player.fallDistance < ModuleRegistry.AUTO_MLG.slider("height").value) {
            return;
        }
        if (player.getDeltaMovement().y >= -0.3) {
            return;
        }
        BlockPos hitPos = player.blockPosition().below();
        BlockState bs = player.level.getBlockState(hitPos);
        if (bs.getFluidState().is(FluidTags.WATER) || bs.getFluidState().is(FluidTags.LAVA)) {
            return;
        }
        Inventory inv = player.getInventory();
        int slot = -1;
        for (int i = 0; i < inv.items.size(); i++) {
            if (inv.getItem(i).is(Items.WATER_BUCKET)) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            return;
        }
        int prev = inv.selected;
        if (slot >= 9) {
            ItemStack tmp = inv.getItem(9);
            inv.setItem(9, inv.getItem(slot));
            inv.setItem(slot, tmp);
            slot = 9;
        }
        inv.selected = slot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
        BlockHitResult hit = new BlockHitResult(new Vec3(hitPos.getX() + 0.5, hitPos.getY(), hitPos.getZ() + 0.5), Direction.UP, hitPos, false);
        player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        inv.selected = prev;
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(prev));
        }
        lastMlgTick = player.tickCount;
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (ModuleRegistry.NO_FALL.isEnabled() || ModuleRegistry.CRITICALS.isEnabled() || FreeCam.isActive()) {
            Player local = local();
            if (local != null && event.getEntity() instanceof Player p && p.getUUID().equals(local.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (FreeCam.isActive()) {
            Player local = local();
            if (local != null && event.getEntity() instanceof Player p && p.getUUID().equals(local.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelLastEvent event) {
        if (!ModuleRegistry.TRACER.isEnabled() && !ModuleRegistry.XRAY.isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Matrix4f model = event.getPoseStack().last().pose();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.5f);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        if (ModuleRegistry.TRACER.isEnabled()) {
            double range = ModuleRegistry.TRACER.slider("range").value;
            Vec3 start = mc.player.getPosition(event.getPartialTick()).add(0, mc.player.getBbHeight() / 2.0, 0).subtract(cam);
            for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(range),
                    t -> t != mc.player && t.isAlive() && !t.isInvisible())) {
                if (!tracerVisible(e)) {
                    continue;
                }
                Vec3 end = e.getEyePosition(event.getPartialTick()).subtract(cam);
                float r, g, b;
                if (e instanceof Player) {
                    r = 1.0f;
                    g = 0.2f;
                    b = 0.2f;
                } else if (e instanceof Monster) {
                    r = 1.0f;
                    g = 0.7f;
                    b = 0.1f;
                } else {
                    r = 0.3f;
                    g = 1.0f;
                    b = 0.3f;
                }
                buf.vertex(model, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, 1.0f).endVertex();
                buf.vertex(model, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, 1.0f).endVertex();
            }
        }

        if (ModuleRegistry.XRAY.isEnabled()) {
            for (BlockPos p : xrayCache) {
                int[] info = xrayTargets.get(mc.level.getBlockState(p).getBlock());
                int color = info != null ? info[0] : 0xFF00FF;
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                renderBox(model, buf, p, cam, r, g, b);
            }
        }

        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static void renderBox(Matrix4f model, BufferBuilder buf, BlockPos p, Vec3 cam, float r, float g, float b) {
        float x = (float) (p.getX() - cam.x);
        float y = (float) (p.getY() - cam.y);
        float z = (float) (p.getZ() - cam.z);
        float[][] edges = {
                {x, y, z, x + 1, y, z}, {x + 1, y, z, x + 1, y, z + 1}, {x + 1, y, z + 1, x, y, z + 1}, {x, y, z + 1, x, y, z},
                {x, y + 1, z, x + 1, y + 1, z}, {x + 1, y + 1, z, x + 1, y + 1, z + 1}, {x + 1, y + 1, z + 1, x, y + 1, z + 1}, {x, y + 1, z + 1, x, y + 1, z},
                {x, y, z, x, y + 1, z}, {x + 1, y, z, x + 1, y + 1, z}, {x + 1, y, z + 1, x + 1, y + 1, z + 1}, {x, y, z + 1, x, y + 1, z + 1}
        };
        for (float[] e : edges) {
            buf.vertex(model, e[0], e[1], e[2]).color(r, g, b, 1.0f).endVertex();
            buf.vertex(model, e[3], e[4], e[5]).color(r, g, b, 1.0f).endVertex();
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (FreeCam.isActive()) {
            event.setCanceled(true);
        }
    }
}
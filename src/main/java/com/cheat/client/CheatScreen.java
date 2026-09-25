package com.cheat.client;

import com.cheat.config.CheatConfig;
import com.cheat.modules.Module;
import com.cheat.modules.ModuleCategory;
import com.cheat.modules.ModuleController;
import com.cheat.modules.ModuleRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CheatScreen extends Screen {
    private static final int ACCENT = 0xFF18A0FB;

    private ModuleCategory activeCategory = ModuleCategory.MOVEMENT;
    private Module selected;
    private boolean capturingBind;
    private boolean capturingMenu;
    private Module.SliderSetting draggingSlider;

    public CheatScreen() {
        super(Component.literal("Cheat"));
    }

    private int panelX() {
        return 40;
    }

    private int panelY() {
        return 40;
    }

    private int panelW() {
        return this.width - 80;
    }

    private int panelH() {
        return this.height - 80;
    }

    private int tabY() {
        return panelY() + 30;
    }

    private int tabH() {
        return 22;
    }

    private int listX() {
        return panelX() + 12;
    }

    private int listY() {
        return tabY() + tabH() + 12;
    }

    private int listW() {
        return 200;
    }

    private int rowH() {
        return 22;
    }

    private int winX() {
        return Math.min(listX() + listW() + 30, panelX() + panelW() - winW() - 12);
    }

    private int winY() {
        return listY();
    }

    private int winW() {
        return 240;
    }

    private int winH() {
        return 34 + (2 + selected.getSettings().size()) * 22;
    }

    private int rowsY(int i) {
        return winY() + 34 + i * 22;
    }

    @Override
    protected void init() {
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        fill(pose, 0, 0, this.width, this.height, 0x66000000);

        int px = panelX(), py = panelY(), pw = panelW(), ph = panelH();
        fill(pose, px, py, px + pw, py + ph, 0xD017171D);
        fill(pose, px, py, px + pw, py + 2, ACCENT);
        fill(pose, px, py, px + 1, py + ph, 0xFF25252B);
        fill(pose, px + pw - 1, py, px + pw, py + ph, 0xFF25252B);
        fill(pose, px, py + ph - 1, px + pw, py + ph, 0xFF25252B);

        this.font.drawShadow(pose, Component.literal("Cheat"), px + 14, py + 8, 0xFFFFFFFF);
        this.font.drawShadow(pose, Component.literal("1.19.2"), px + 14 + this.font.width("Cheat") + 10, py + 8, 0xFF7A7A7A);

        int tabs = 0;
        for (Tab tab : tabs()) {
            tabs++;
            if (activeCategory == tab.category) {
                fill(pose, tab.rect.x, tab.rect.y, tab.rect.x + tab.rect.w, tab.rect.y + tab.rect.h, 0xFF2C2C34);
                fill(pose, tab.rect.x, tab.rect.y + tab.rect.h - 2, tab.rect.x + tab.rect.w, tab.rect.y + tab.rect.h, ACCENT);
                drawCenteredString(pose, this.font, Component.literal(tab.category.label), tab.rect.x + tab.rect.w / 2, tab.rect.y + 6, 0xFFFFFFFF);
            } else {
                boolean hover = tab.rect.contains(mouseX, mouseY);
                if (hover) {
                    fill(pose, tab.rect.x, tab.rect.y, tab.rect.x + tab.rect.w, tab.rect.y + tab.rect.h, 0x14FFFFFF);
                }
                drawCenteredString(pose, this.font, Component.literal(tab.category.label), tab.rect.x + tab.rect.w / 2, tab.rect.y + 6, 0xFF8E8E8E);
            }
        }

        List<Module> mods = ModuleRegistry.byCategory(activeCategory);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            Rect r = rowRect(i);
            boolean hover = r.contains(mouseX, mouseY);
            if (hover) {
                fill(pose, r.x, r.y, r.x + r.w, r.y + r.h, 0x16FFFFFF);
            }
            if (m == selected) {
                fill(pose, r.x, r.y, r.x + 2, r.y + r.h, ACCENT);
            }
            float nameColor = m.isEnabled() ? 0xFFE8E8E8 : 0xFF9A9A9A;
            this.font.drawShadow(pose, Component.literal(m.getName()), r.x + 10, r.y + 6, (int) nameColor);
            String status = m.isEnabled() ? "ON" : "OFF";
            int statusColor = m.isEnabled() ? 0xFF43D95E : 0xFFE14D4D;
            this.font.drawShadow(pose, Component.literal(status), r.x + r.w - this.font.width(status) - 8, r.y + 6, statusColor);
        }

        int footerY = listY() + mods.size() * rowH() + 8;
        Rect menuRect = new Rect(listX(), footerY, listW(), 18);
        boolean hoverMenu = menuRect.contains(mouseX, mouseY);
        if (hoverMenu) {
            fill(pose, menuRect.x, menuRect.y, menuRect.x + menuRect.w, menuRect.y + menuRect.h, 0x14FFFFFF);
        }
        Component menuLabel = Component.literal("Touche menu: ").append(keyName(CheatConfig.menuKey));
        this.font.drawShadow(pose, menuLabel, menuRect.x + 8, menuRect.y + 5, 0xFF8E8E8E);

        if (selected != null) {
            renderWindow(pose, mouseX, mouseY);
        }
    }

    private void renderWindow(PoseStack pose, int mouseX, int mouseY) {
        int wx = winX(), wy = winY(), ww = winW(), wh = winH();
        fill(pose, wx, wy, wx + ww, wy + wh, 0xEE16161C);
        fill(pose, wx, wy, wx + ww, wy + 24, 0xFF20202A);
        fill(pose, wx, wy + 23, wx + ww, wy + 24, ACCENT);
        fill(pose, wx, wy, wx + 1, wy + wh, 0xFF2A2A32);
        fill(pose, wx + ww - 1, wy, wx + ww, wy + wh, 0xFF2A2A32);
        fill(pose, wx, wy + wh - 1, wx + ww, wy + wh, 0xFF2A2A32);

        this.font.drawShadow(pose, Component.literal(selected.getName()), wx + 10, wy + 6, 0xFF189AD5);
        Rect close = closeRect();
        boolean hoverClose = close.contains(mouseX, mouseY);
        fill(pose, close.x, close.y, close.x + close.w, close.y + close.h, hoverClose ? 0xFFE14D4D : 0xFF33333D);
        drawCenteredString(pose, this.font, Component.literal("X"), close.x + close.w / 2, close.y + 3, 0xFFFFFFFF);

        int i = 0;
        drawRowToggle(pose, i++, "Active", selected.isEnabled(), ACCENT, 0xFF43D95E, mouseX, mouseY);
        drawRowKey(pose, i++);

        for (Module.Setting s : selected.getSettings()) {
            if (s instanceof Module.SliderSetting slider) {
                drawRowSlider(pose, i, slider, mouseX);
            } else if (s instanceof Module.BoolSetting bool) {
                drawRowToggle(pose, i, bool.label, bool.value, ACCENT, 0xFF43D95E, mouseX, mouseY);
            }
            i++;
        }
    }

    private void drawRowToggle(PoseStack pose, int i, String label, boolean value, int onColor, int onColorText, int mouseX, int mouseY) {
        Rect r = rowRectInWindow(i);
        boolean hover = r.contains(mouseX, mouseY) || capturingBind;
        if (hover) {
            fill(pose, r.x, r.y, r.x + r.w, r.y + r.h, 0x10FFFFFF);
        }
        this.font.drawShadow(pose, Component.literal(label), r.x + 8, r.y + 5, 0xFFE8E8E8);
        int pillX = r.x + r.w - 34, pillY = r.y + (r.h - 12) / 2, pillW = 28, pillH = 12;
        fill(pose, pillX, pillY, pillX + pillW, pillY + pillH, value ? onColor : 0xFF3A3A44);
        fill(pose, value ? pillX + pillW - 12 : pillX, pillY, value ? pillX + pillW - 12 + 12 : pillX + 12, pillY + pillH, 0xFFDDDDDD);
    }

    private void drawRowKey(PoseStack pose, int i) {
        Rect r = rowRectInWindow(i);
        this.font.drawShadow(pose, Component.literal("Bind"), r.x + 8, r.y + 5, 0xFFE8E8E8);
        Component txt = capturingBind ? Component.literal("Appuie une touche") : keyName(selected.getBind());
        this.font.drawShadow(pose, txt, r.x + r.w - this.font.width(txt) - 8, r.y + 5, capturingBind ? 0xFF43D95E : 0xFF189AD5);
    }

    private void drawRowSlider(PoseStack pose, int i, Module.SliderSetting slider, int mouseX) {
        Rect r = rowRectInWindow(i);
        this.font.drawShadow(pose, Component.literal(slider.label), r.x + 8, r.y + 5, 0xFFE8E8E8);
        int trackX = r.x + 80;
        int trackW = r.w - 96;
        int trackY = r.y + (r.h - 4) / 2;
        double ratio = (slider.value - slider.min) / (slider.max - slider.min);
        fill(pose, trackX, trackY, trackX + trackW, trackY + 4, 0xFF33333D);
        fill(pose, trackX, trackY, trackX + (int) (trackW * ratio), trackY + 4, ACCENT);
        Component val = Component.literal(String.format("%.2f", slider.value));
        this.font.drawShadow(pose, val, trackX + trackW - this.font.width(val), r.y - 6, 0xFF7A7A7A);
    }

    private Tab[] tabs() {
        ModuleCategory[] cats = ModuleCategory.values();
        Tab[] tabs = new Tab[cats.length];
        int x = panelX() + 12;
        for (int i = 0; i < cats.length; i++) {
            int w = Math.max(this.font.width(cats[i].label) + 22, 78);
            tabs[i] = new Tab(cats[i], new Rect(x, tabY(), w, tabH()));
            x += w + 6;
        }
        return tabs;
    }

    private Rect rowRect(int i) {
        return new Rect(listX(), listY() + i * rowH(), listW(), rowH());
    }

    private Rect rowRectInWindow(int i) {
        return new Rect(winX() + 8, rowsY(i), winW() - 16, 20);
    }

    private Rect closeRect() {
        return new Rect(winX() + winW() - 18, winY() + 4, 14, 14);
    }

    private Component keyName(int code) {
        if (code <= 0) {
            return Component.literal("Aucune");
        }
        return InputConstants.getKey(code, -1).getDisplayName();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int x = (int) mouseX, y = (int) mouseY;
        if (capturingBind) {
            capturingBind = false;
        }
        if (capturingMenu) {
            capturingMenu = false;
        }

        if (selected != null) {
            if (closeRect().contains(x, y)) {
                selected = null;
                draggingSlider = null;
                return true;
            }
            int i = 0;
            if (rowRectInWindow(i++).contains(x, y)) {
                ModuleController.setEnabled(selected, !selected.isEnabled());
                return true;
            }
            if (rowRectInWindow(i++).contains(x, y)) {
                capturingBind = true;
                return true;
            }
            for (Module.Setting s : selected.getSettings()) {
                if (rowRectInWindow(i).contains(x, y)) {
                    if (s instanceof Module.SliderSetting slider) {
                        draggingSlider = slider;
                        updateSlider(slider, x);
                        CheatConfig.save();
                    } else if (s instanceof Module.BoolSetting bool) {
                        bool.value = !bool.value;
                        CheatConfig.save();
                    }
                    return true;
                }
                i++;
            }
        }

        for (Tab tab : tabs()) {
            if (tab.rect.contains(x, y)) {
                if (activeCategory != tab.category) {
                    activeCategory = tab.category;
                    selected = null;
                    draggingSlider = null;
                }
                return true;
            }
        }

        List<Module> mods = ModuleRegistry.byCategory(activeCategory);
        for (int i = 0; i < mods.size(); i++) {
            if (rowRect(i).contains(x, y)) {
                if (selected == mods.get(i)) {
                    selected = null;
                } else {
                    selected = mods.get(i);
                }
                capturingBind = false;
                return true;
            }
        }

        Rect menuRect = new Rect(listX(), listY() + mods.size() * rowH() + 8, listW(), 18);
        if (menuRect.contains(x, y)) {
            capturingMenu = true;
            return true;
        }

        if (selected != null) {
            selected = null;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingSlider != null) {
            updateSlider(draggingSlider, (int) mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSlider != null) {
            draggingSlider = null;
            CheatConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSlider(Module.SliderSetting slider, int x) {
        int trackX = winX() + 88;
        int trackW = winW() - 104;
        double ratio = (x - trackX) / (double) trackW;
        double value = slider.min + ratio * (slider.max - slider.min);
        value = Math.round(value / slider.step) * slider.step;
        slider.value = Mth.clamp(value, slider.min, slider.max);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturingBind || capturingMenu) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                capturingBind = false;
                capturingMenu = false;
                return true;
            }
            if (capturingMenu) {
                CheatConfig.menuKey = keyCode;
                CheatConfig.save();
            } else if (selected != null) {
                selected.setBind(keyCode);
                CheatConfig.save();
            }
            capturingBind = false;
            capturingMenu = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == CheatConfig.menuKey) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Tab(ModuleCategory category, Rect rect) {
    }

    private record Rect(int x, int y, int w, int h) {
        private boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }
}
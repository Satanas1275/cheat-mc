package com.cheat.client;

import com.cheat.config.CheatConfig;
import com.cheat.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BlockPickerScreen extends Screen {
    private final Screen parent;
    private final Module.BlockListSetting setting;
    private final List<Entry> blocks = new ArrayList<>();
    private String search = "";
    private int scroll;
    private List<Entry> filtered = List.of();
    private String filteredQuery;

    public BlockPickerScreen(Screen parent, Module.BlockListSetting setting) {
        super(Component.literal("Blocs"));
        this.parent = parent;
        this.setting = setting;
        for (Block block : ForgeRegistries.BLOCKS) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id == null || isAir(id)) {
                continue;
            }
            String text = id.toString();
            blocks.add(new Entry(text, (text + " " + block.getName().getString()).toLowerCase(Locale.ROOT)));
        }
        blocks.sort(Comparator.comparing(Entry::id));
    }

    private static boolean isAir(ResourceLocation id) {
        return id.getNamespace().equals("minecraft")
                && (id.getPath().equals("air") || id.getPath().equals("cave_air") || id.getPath().equals("void_air"));
    }

    private int panelX() {
        return 40;
    }

    private int panelY() {
        return 40;
    }

    private int panelW() {
        return width - 80;
    }

    private int panelH() {
        return height - 80;
    }

    private int rowH() {
        return 20;
    }

    private int listY() {
        return panelY() + 62;
    }

    private int visibleRows() {
        return Math.max(1, (panelH() - 72) / rowH());
    }

    private Rect searchRect() {
        return new Rect(panelX() + 10, panelY() + 32, panelW() - 100, 20);
    }

    private Rect clearRect() {
        return new Rect(panelX() + panelW() - 82, panelY() + 32, 72, 20);
    }

    private Rect closeRect() {
        return new Rect(panelX() + panelW() - 24, panelY() + 6, 16, 16);
    }

    private Rect rowRect(int i) {
        return new Rect(panelX() + 10, listY() + i * rowH(), panelW() - 20, rowH() - 1);
    }

    private List<Entry> filtered() {
        String query = search.toLowerCase(Locale.ROOT);
        if (filteredQuery != null && filteredQuery.equals(query)) {
            return filtered;
        }
        List<Entry> result = new ArrayList<>();
        for (Entry entry : blocks) {
            if (query.isEmpty() || entry.search.contains(query)) {
                result.add(entry);
            }
        }
        filtered = result;
        filteredQuery = query;
        return result;
    }

    private int maxScroll(List<Entry> list) {
        return Math.max(0, list.size() - visibleRows());
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        fill(pose, 0, 0, width, height, 0x99000000);
        int px = panelX();
        int py = panelY();
        int pw = panelW();
        int ph = panelH();
        fill(pose, px, py, px + pw, py + ph, 0xEE16161C);
        fill(pose, px, py, px + pw, py + 26, 0xFF20202A);
        fill(pose, px, py + 25, px + pw, py + 26, 0xFF18A0FB);

        this.font.drawShadow(pose, Component.literal("Liste de blocs"), px + 12, py + 8, 0xFF189AD5);
        String count = setting.values().size() + " sélectionnés";
        this.font.drawShadow(pose, Component.literal(count), px + pw - this.font.width(count) - 34, py + 8, 0xFF7A7A7A);

        Rect searchBox = searchRect();
        fill(pose, searchBox.x, searchBox.y, searchBox.x + searchBox.w, searchBox.y + searchBox.h, 0xFF101015);
        String searchText = search.isEmpty() ? "Rechercher un bloc..." : search;
        this.font.drawShadow(pose, Component.literal(searchText), searchBox.x + 6, searchBox.y + 6,
                search.isEmpty() ? 0xFF6A6A75 : 0xFFE8E8E8);

        Rect clear = clearRect();
        fill(pose, clear.x, clear.y, clear.x + clear.w, clear.y + clear.h, 0xFF30303A);
        drawCenteredString(pose, this.font, Component.literal("Aucun"), clear.x + clear.w / 2, clear.y + 6, 0xFFE8E8E8);

        Rect close = closeRect();
        fill(pose, close.x, close.y, close.x + close.w, close.y + close.h, 0xFF33333D);
        drawCenteredString(pose, this.font, Component.literal("X"), close.x + close.w / 2, close.y + 3, 0xFFFFFFFF);

        List<Entry> list = filtered();
        scroll = Mth.clamp(scroll, 0, maxScroll(list));
        int first = scroll;
        int last = Math.min(list.size(), first + visibleRows());
        for (int i = first; i < last; i++) {
            Rect row = rowRect(i - first);
            boolean hover = row.contains(mouseX, mouseY);
            Entry entry = list.get(i);
            boolean selected = setting.contains(entry.id);
            int color = selected ? 0xFF203040 : hover ? 0xFF262630 : 0xFF1B1B22;
            fill(pose, row.x, row.y, row.x + row.w, row.y + row.h, color);
            this.font.drawShadow(pose, Component.literal((selected ? "[x] " : "[ ] ") + entry.id),
                    row.x + 6, row.y + 5, selected ? 0xFF9CFF57 : 0xFFBDBDC7);
        }
        if (list.isEmpty()) {
            drawCenteredString(pose, this.font, Component.literal("Aucun bloc trouvé"),
                    px + pw / 2, listY() + 8, 0xFF7A7A7A);
        }

        String position = list.isEmpty() ? "0 / 0" : (first + 1) + " / " + list.size();
        this.font.drawShadow(pose, Component.literal(position), px + 12, py + ph - 18, 0xFF7A7A7A);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return true;
        }
        int x = (int) mouseX;
        int y = (int) mouseY;
        if (closeRect().contains(x, y)) {
            onClose();
            return true;
        }
        if (clearRect().contains(x, y)) {
            setting.clear();
            CheatConfig.save();
            return true;
        }
        List<Entry> list = filtered();
        scroll = Mth.clamp(scroll, 0, maxScroll(list));
        int first = scroll;
        int last = Math.min(list.size(), first + visibleRows());
        for (int i = first; i < last; i++) {
            if (rowRect(i - first).contains(x, y)) {
                setting.toggle(list.get(i).id);
                CheatConfig.save();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<Entry> list = filtered();
        scroll = Mth.clamp(scroll + (delta > 0 ? -3 : 3), 0, maxScroll(list));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            scroll = 0;
        }
        return true;
    }

    @Override
    public boolean charTyped(char code, int modifiers) {
        if (!Character.isISOControl(code) && search.length() < 96) {
            search += code;
            scroll = 0;
        }
        return true;
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Entry(String id, String search) {
    }

    private record Rect(int x, int y, int w, int h) {
        private boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }
}

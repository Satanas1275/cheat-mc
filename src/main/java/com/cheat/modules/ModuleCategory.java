package com.cheat.modules;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render");

    public final String label;

    ModuleCategory(String label) {
        this.label = label;
    }
}
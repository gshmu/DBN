package com.dci.intellij.dbn.execution;

public class NavigationInstruction {
    private boolean open;
    private boolean focus;
    private boolean scroll;

    public static final NavigationInstruction FOCUS = new NavigationInstruction(false, true, false);
    public static final NavigationInstruction SCROLL = new NavigationInstruction(false, false, true);
    public static final NavigationInstruction FOCUS_SCROLL = new NavigationInstruction(false, true, true);
    public static final NavigationInstruction OPEN = new NavigationInstruction(true, false, false);
    public static final NavigationInstruction OPEN_FOCUS_SCROLL = new NavigationInstruction(true, true, true);
    public static final NavigationInstruction OPEN_SCROLL = new NavigationInstruction(true, false, true);

    public NavigationInstruction(boolean open, boolean focus, boolean scroll) {
        this.open = open;
        this.focus = focus;
        this.scroll = scroll;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isFocus() {
        return focus;
    }

    public boolean isScroll() {
        return scroll;
    }
}

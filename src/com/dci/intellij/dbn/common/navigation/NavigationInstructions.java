package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class NavigationInstructions extends PropertyHolderImpl<NavigationInstruction> {

    public static final NavigationInstructions NONE = new NavigationInstructions();

    public static final NavigationInstructions FOCUS = new NavigationInstructions(false, true, false);
    public static final NavigationInstructions SCROLL = new NavigationInstructions(false, false, true);
    public static final NavigationInstructions FOCUS_SCROLL = new NavigationInstructions(false, true, true);
    public static final NavigationInstructions OPEN = new NavigationInstructions(true, false, false);
    public static final NavigationInstructions OPEN_FOCUS_SCROLL = new NavigationInstructions(true, true, true);
    public static final NavigationInstructions OPEN_SCROLL = new NavigationInstructions(true, false, true);

    private NavigationInstructions(NavigationInstruction ... instructions) {
        super(instructions);
    }

    private NavigationInstructions(boolean open, boolean focus, boolean scroll) {
        set(NavigationInstruction.OPEN, open);
        set(NavigationInstruction.FOCUS, focus);
        set(NavigationInstruction.SCROLL, scroll);
    }

    public static NavigationInstructions create(boolean open, boolean focus, boolean scroll) {
        return new NavigationInstructions(open, focus, scroll);
    }

    public static NavigationInstructions create(NavigationInstruction ... instructions) {
        return new NavigationInstructions(instructions);
    }

    public boolean isOpen() {
        return is(NavigationInstruction.OPEN);
    }

    public boolean isFocus() {
        return is(NavigationInstruction.FOCUS);
    }

    public boolean isScroll() {
        return is(NavigationInstruction.SCROLL);
    }

    @Override
    protected NavigationInstruction[] properties() {
        return NavigationInstruction.values();
    }
}

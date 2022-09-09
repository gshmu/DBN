package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class NavigationInstructions extends PropertyHolderBase.IntStore<NavigationInstruction> {

    public static final NavigationInstructions NONE = new NavigationInstructions();

    private NavigationInstructions(NavigationInstruction ... instructions) {
        super(instructions);
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

    public boolean isSelect() {
        return is(NavigationInstruction.SELECT);
    }

    public boolean isReset() {
        return is(NavigationInstruction.RESET);
    }

    public NavigationInstructions with(NavigationInstruction instruction) {
        return with(instruction, true);
    }

    public NavigationInstructions without(NavigationInstruction instruction) {
        return with(instruction, false);
    }

    public NavigationInstructions with(NavigationInstruction instruction, boolean value) {
        set(instruction, value);
        return this;
    }

    @Override
    protected NavigationInstruction[] properties() {
        return NavigationInstruction.VALUES;
    }
}

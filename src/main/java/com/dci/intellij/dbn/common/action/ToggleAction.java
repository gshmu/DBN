package com.dci.intellij.dbn.common.action;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class ToggleAction
        extends com.intellij.openapi.actionSystem.ToggleAction
        implements BackgroundUpdatedAction {

    public ToggleAction() {
    }

    public ToggleAction(@Nullable String text) {
        super(text);
    }

    public ToggleAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }
}

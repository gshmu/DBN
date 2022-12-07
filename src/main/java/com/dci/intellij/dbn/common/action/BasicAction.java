package com.dci.intellij.dbn.common.action;

import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BasicAction extends AnAction implements BackgroundUpdatedAction {

    public BasicAction() {
    }

    public BasicAction(@Nullable Icon icon) {
        super(icon);
    }

    public BasicAction(@Nullable String text) {
        super(text);
    }

    public BasicAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }
}

package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.openapi.util.NlsActions.ActionDescription;
import static com.intellij.openapi.util.NlsActions.ActionText;

public abstract class BasicAction extends AnAction implements BackgroundUpdatedAction {

    public BasicAction() {
    }

    public BasicAction(@Nullable Icon icon) {
        super(icon);
    }

    public BasicAction(@Nullable @ActionText String text) {
        super(text);
    }

    public BasicAction(@Nullable @ActionText String text, @Nullable @ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @NotNull
    @Compatibility
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    @Compatibility
    public boolean isUpdateInBackground() {
        return true;
    }
}

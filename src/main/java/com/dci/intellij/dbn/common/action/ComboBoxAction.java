package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import org.jetbrains.annotations.NotNull;

public abstract class ComboBoxAction
        extends com.intellij.openapi.actionSystem.ex.ComboBoxAction
        implements BackgroundUpdatedAction {


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

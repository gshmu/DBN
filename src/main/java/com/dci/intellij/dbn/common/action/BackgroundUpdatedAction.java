package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionUpdateThreadAware;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

@Compatibility
public interface BackgroundUpdatedAction extends UpdateInBackground, ActionUpdateThreadAware {

    @NotNull
    @Compatibility
    default ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Compatibility
    default boolean isUpdateInBackground() {
        return true;
    }

}

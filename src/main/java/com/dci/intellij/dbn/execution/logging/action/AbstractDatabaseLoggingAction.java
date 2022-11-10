package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareContextAction;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract class AbstractDatabaseLoggingAction extends DumbAwareContextAction<DatabaseLoggingResult> {
    AbstractDatabaseLoggingAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    protected DatabaseLoggingResult getTarget(@NotNull AnActionEvent e) {
        return e.getData(DataKeys.DATABASE_LOG_OUTPUT);
    }
}

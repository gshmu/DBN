package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiscellaneousConnectionAction extends AbstractConnectionAction {
    public MiscellaneousConnectionAction(@NotNull ConnectionHandler connectionHandler) {
        super("Dev Test", connectionHandler);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {

    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler connectionHandler) {
        presentation.setVisible(Diagnostics.isBulkActionsEnabled());
    }
}

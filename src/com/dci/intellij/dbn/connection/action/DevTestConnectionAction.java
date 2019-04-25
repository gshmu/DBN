package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DevTestConnectionAction extends AbstractConnectionAction{
    DevTestConnectionAction(@NotNull ConnectionHandler connectionHandler) {
        super("Dev Test", connectionHandler);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {

    }

    @Override
    protected void update(@NotNull AnActionEvent e, Project project, @NotNull ConnectionHandler connectionHandler) {
        e.getPresentation().setVisible(DatabaseNavigator.DEVELOPER);
    }
}

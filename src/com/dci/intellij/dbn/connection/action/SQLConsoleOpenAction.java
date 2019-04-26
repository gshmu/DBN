package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SQLConsoleOpenAction extends AbstractConnectionAction {
    SQLConsoleOpenAction(ConnectionHandler connectionHandler) {
        super("Open SQL Console", Icons.FILE_SQL_CONSOLE, connectionHandler);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(connectionHandler.getConsoleBundle().getDefaultConsole(), true);
    }
}

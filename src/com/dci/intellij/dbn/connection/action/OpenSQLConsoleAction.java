package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import org.jetbrains.annotations.NotNull;

public class OpenSQLConsoleAction extends AbstractConnectionAction {
    public OpenSQLConsoleAction(ConnectionHandler connectionHandler) {
        super("Open SQL Console", Icons.FILE_SQL_CONSOLE, connectionHandler);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(connectionHandler.getConsoleBundle().getDefaultConsole(), true);
    }
}

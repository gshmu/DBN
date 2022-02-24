package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConsoleCreateAction extends DumbAwareProjectAction {
    private final DBConsoleType consoleType;
    private final ConnectionHandlerRef connection;

    ConsoleCreateAction(ConnectionHandler connection, DBConsoleType consoleType) {
        super("New " + consoleType.getName() + "...");
        this.connection = connection.ref();
        this.consoleType = consoleType;

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseConsoleManager consoleManager = DatabaseConsoleManager.getInstance(project);
        consoleManager.showCreateConsoleDialog(connection.get(), consoleType);
    }
}
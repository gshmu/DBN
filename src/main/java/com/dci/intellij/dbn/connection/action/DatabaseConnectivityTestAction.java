package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseConnectivityTestAction extends AbstractConnectionAction {

    DatabaseConnectivityTestAction(ConnectionHandler connection) {
        super("Test connectivity", "Test connectivity of " + connection.getName(), null, connection);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        connection.getInstructions().setAllowAutoConnect(true);

        ConnectionAction.invoke("testing the connectivity", true, connection,
                (action) -> ConnectionManager.testConnection(connection, null, SessionId.MAIN, true, true));
    }
}

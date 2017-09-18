package com.dci.intellij.dbn.connection.info.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.intellij.openapi.project.Project;

public class ConnectionInfoDialog extends DBNDialog<ConnectionInfoForm> {
    public ConnectionInfoDialog(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Connection information", true);
        component = new ConnectionInfoForm(this, connectionHandler);
        getCancelAction().putValue(Action.NAME, "Close");
        setResizable(false);
        init();
    }

    public ConnectionInfoDialog(Project project, ConnectionInfo connectionInfo, String connectionName, EnvironmentType environmentType) {
        super(project, "Connection information", true);
        component = new ConnectionInfoForm(this, connectionInfo, connectionName, environmentType);
        getCancelAction().putValue(Action.NAME, "Close");
        setResizable(false);
        init();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getCancelAction()
        };
    }
}

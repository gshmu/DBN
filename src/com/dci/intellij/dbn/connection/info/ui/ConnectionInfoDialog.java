package com.dci.intellij.dbn.connection.info.ui;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectionInfoDialog extends DBNDialog<ConnectionInfoForm> {
    private ConnectionHandlerRef connectionHandlerRef;
    private ConnectionInfo connectionInfo;
    private String connectionName;
    private EnvironmentType environmentType;

    public ConnectionInfoDialog(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Connection information", true);
        connectionHandlerRef = connectionHandler.getRef();
        getCancelAction().putValue(Action.NAME, "Close");
        setResizable(false);
        init();
    }

    public ConnectionInfoDialog(Project project, ConnectionInfo connectionInfo, String connectionName, EnvironmentType environmentType) {
        super(project, "Connection information", true);
        this.connectionInfo = connectionInfo;
        this.connectionName = connectionName;
        this.environmentType = environmentType;
        getCancelAction().putValue(Action.NAME, "Close");
        setResizable(false);
        init();
    }

    @NotNull
    @Override
    protected ConnectionInfoForm createForm() {
        if (connectionHandlerRef != null) {
            ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
            return new ConnectionInfoForm(this, connectionHandler);
        } else {
            return new ConnectionInfoForm(this, connectionInfo, connectionName, environmentType);
        }
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getCancelAction()
        };
    }
}

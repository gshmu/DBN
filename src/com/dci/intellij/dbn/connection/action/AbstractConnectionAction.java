package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.action.DumbAwareContextAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractConnectionAction extends DumbAwareContextAction<ConnectionHandler> {
    private final ConnectionHandlerRef connection;

    public AbstractConnectionAction(String text, @NotNull ConnectionHandler connection) {
        this(text, null, connection);

    }
    public AbstractConnectionAction(String text, Icon icon, @NotNull ConnectionHandler connection) {
        this(text, null, icon, connection);
    }
    public AbstractConnectionAction(String text, String description, Icon icon, @NotNull ConnectionHandler connection) {
        super(text, description, icon);
        this.connection = connection.getRef();
    }

    public ConnectionId getConnectionId() {
        return connection.getConnectionId();
    }

    @Nullable
    public ConnectionHandler getConnection() {
        return ConnectionHandlerRef.get(connection);
    }

    @Override
    protected ConnectionHandler getTarget(@NotNull AnActionEvent e) {
        return connection.get();
    }

    @NotNull
    @Override
    protected Project getProject() {
        ConnectionHandler connection = this.connection.ensure();
        return connection.getProject();
    }
}


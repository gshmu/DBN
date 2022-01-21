package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.action.DumbAwareContextAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public abstract class AbstractConnectionAction extends DumbAwareContextAction<ConnectionHandler> {
    private final ConnectionHandlerRef connectionHandler;

    public AbstractConnectionAction(String text, @NotNull ConnectionHandler connectionHandler) {
        this(text, null, connectionHandler);

    }
    public AbstractConnectionAction(String text, Icon icon, @NotNull ConnectionHandler connectionHandler) {
        this(text, null, icon, connectionHandler);
    }
    public AbstractConnectionAction(String text, String description, Icon icon, @NotNull ConnectionHandler connectionHandler) {
        super(text, description, icon);
        this.connectionHandler = connectionHandler.getRef();
    }

    public ConnectionId getConnectionId() {
        return connectionHandler.getConnectionId();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandler);
    }


    @Override
    protected ConnectionHandler getTarget(@NotNull AnActionEvent e) {
        return connectionHandler.get();
    }

    @Nullable
    @Override
    protected Project getProject() {
        ConnectionHandler connectionHandler = this.connectionHandler.get();
        return connectionHandler == null ? null : connectionHandler.getProject();
    }
}


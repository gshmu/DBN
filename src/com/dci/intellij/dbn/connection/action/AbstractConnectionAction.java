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
    private final ConnectionHandlerRef connectionHandlerRef;

    public AbstractConnectionAction(String text, @NotNull ConnectionHandler connectionHandler) {
        this(text, null, connectionHandler);

    }
    public AbstractConnectionAction(String text, Icon icon, @NotNull ConnectionHandler connectionHandler) {
        this(text, null, icon, connectionHandler);
    }
    public AbstractConnectionAction(String text, String description, Icon icon, @NotNull ConnectionHandler connectionHandler) {
        super(text, description, icon);
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    protected ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }

    @Override
    protected ConnectionHandler getTarget(@NotNull AnActionEvent e) {
        return connectionHandlerRef.get();
    }

    @Nullable
    @Override
    protected Project getProject() {
        ConnectionHandler connectionHandler = connectionHandlerRef.get();
        return connectionHandler == null ? null : connectionHandler.getProject();
    }
}


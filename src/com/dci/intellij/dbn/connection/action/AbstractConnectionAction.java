package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class AbstractConnectionAction extends DumbAwareProjectAction {
    private ConnectionHandlerRef connectionHandlerRef;

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
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionHandler connectionHandler = connectionHandlerRef.get();
        if (Failsafe.check(connectionHandler)) {
            actionPerformed(e, project, connectionHandler);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionHandler connectionHandler = connectionHandlerRef.get();
        if (connectionHandler != null) {
            update(e, project, connectionHandler);
        }
    }

    protected void update(@NotNull AnActionEvent e, Project project, @NotNull ConnectionHandler connectionHandler){
    };

    protected abstract void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler);
}


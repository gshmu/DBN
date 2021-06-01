package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class ObjectsStatusRefreshAction extends DumbAwareAction {

    private final ConnectionHandlerRef connectionHandler;

    public ObjectsStatusRefreshAction(ConnectionHandler connectionHandler) {
        super("Refresh objects status");
        this.connectionHandler = connectionHandler.getRef();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        connectionHandler.ensure().getObjectBundle().refreshObjectsStatus(null);
    }
}

package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class ObjectsStatusRefreshAction extends DumbAwareAction {

    private ConnectionHandler connectionHandler;

    public ObjectsStatusRefreshAction(ConnectionHandler connectionHandler) {
        super("Refresh objects status");
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionHandler.getObjectBundle().refreshObjectsStatus(null);
    }
}

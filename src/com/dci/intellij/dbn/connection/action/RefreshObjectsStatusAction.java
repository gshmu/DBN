package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class RefreshObjectsStatusAction extends DumbAwareAction {

    private ConnectionHandlerRef connectionHandlerRef;

    public RefreshObjectsStatusAction(ConnectionHandler connectionHandler) {
        super("Refresh objects status", "", Icons.ACTION_REFRESH);
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionHandlerRef.get().getObjectBundle().refreshObjectsStatus(null);
    }
}

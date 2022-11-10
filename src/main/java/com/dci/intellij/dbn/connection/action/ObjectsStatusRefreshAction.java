package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class ObjectsStatusRefreshAction extends DumbAwareAction {

    private final ConnectionRef connection;

    public ObjectsStatusRefreshAction(ConnectionHandler connection) {
        super("Refresh objects status", "", Icons.ACTION_REFRESH);
        this.connection = connection.ref();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        connection.ensure().getObjectBundle().refreshObjectsStatus(null);
    }
}

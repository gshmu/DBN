package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import org.jetbrains.annotations.NotNull;

public class TabbedBrowserTreeModel extends BrowserTreeModel {
    public TabbedBrowserTreeModel(ConnectionHandler connection) {
        super(connection.getObjectBundle());
        ProjectEvents.subscribe(connection.getProject(), this, ConnectionHandlerStatusListener.TOPIC, connectionHandlerStatusListener());
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return getConnection() == node.getConnection();
    }

    public ConnectionHandler getConnection() {
        return getRoot().getConnection();
    }

    @NotNull
    private ConnectionHandlerStatusListener connectionHandlerStatusListener() {
        return (connectionId) -> {
            ConnectionHandler connection = getConnection();
            if (connection.getConnectionId() == connectionId) {
                notifyListeners(connection.getObjectBundle(), TreeEventType.NODES_CHANGED);
            }
        };
    }
}

package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;

public class TabbedBrowserTreeModel extends BrowserTreeModel {
    public TabbedBrowserTreeModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getObjectBundle());
        ProjectEvents.subscribe(connectionHandler.getProject(), this, ConnectionHandlerStatusListener.TOPIC, connectionHandlerStatusListener);
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return getConnectionHandler() == node.getConnection();
    }

    public ConnectionHandler getConnectionHandler() {
        return getRoot().getConnection();
    }

    private final ConnectionHandlerStatusListener connectionHandlerStatusListener = (connectionId) -> {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler.getConnectionId() == connectionId) {
            notifyListeners(connectionHandler.getObjectBundle(), TreeEventType.NODES_CHANGED);
        }
    };
}

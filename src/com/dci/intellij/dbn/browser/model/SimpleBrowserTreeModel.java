package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.intellij.openapi.project.Project;

public class SimpleBrowserTreeModel extends BrowserTreeModel {
    public SimpleBrowserTreeModel() {
        this(FailsafeUtil.DUMMY_PROJECT, null);
    }

    public SimpleBrowserTreeModel(Project project, ConnectionBundle connectionBundle) {
        super(new SimpleBrowserTreeRoot(project, connectionBundle));
        EventUtil.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, connectionHandlerStatusListener);
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return true;
    }

    private final ConnectionHandlerStatusListener connectionHandlerStatusListener = (connectionId, sessionId) -> {
        ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
        if (connectionHandler != null) {
            notifyListeners(connectionHandler.getObjectBundle(), TreeEventType.NODES_CHANGED);
        }
    };

    public void dispose() {
        super.dispose();
    }
}

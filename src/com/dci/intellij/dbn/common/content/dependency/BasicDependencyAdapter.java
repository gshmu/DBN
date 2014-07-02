package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.connection.ConnectionHandler;

public class BasicDependencyAdapter implements ContentDependencyAdapter {
    private ConnectionHandler connectionHandler;

    public BasicDependencyAdapter(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    protected boolean isConnectionValid() {
        return connectionHandler != null && connectionHandler.isValid();
    }

    public boolean shouldLoad() {
        // should not reload just like that
        return false;
    }

    public boolean shouldLoadIfDirty() {
        //should reload if connection is valid
        return isConnectionValid();
    }

    public boolean isDirty() {
        return false;
    }

    public void beforeLoad() {
        // nothing to do before load
    }

    public void afterLoad() {
        // nothing to do after load
    }

    public void beforeReload(DynamicContent dynamicContent) {

    }

    public void afterReload(DynamicContent dynamicContent) {

    }

    public boolean canLoadFast() {
        return false;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    public void dispose() {
        connectionHandler = null;
    }
}

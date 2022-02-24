package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.connection.ConnectionHandler;

public class BasicDependencyAdapter implements ContentDependencyAdapter {
    public static BasicDependencyAdapter INSTANCE = new BasicDependencyAdapter();

    @Override
    public boolean canConnect(ConnectionHandler connection) {
        return connection != null && connection.canConnect() && connection.isValid();
    }

    @Override
    public boolean canLoad(ConnectionHandler connection) {
        //should reload if connection is valid
        return canConnect(connection);
    }

    @Override
    public boolean areDependenciesDirty() {
        return false;
    }

    @Override
    public void refreshSources() {

    }

    @Override
    public boolean canLoadFast() {
        return false;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    @Override
    public void dispose() {
    }
}

package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.connection.ConnectionHandler;

public class VoidContentDependencyAdapter implements ContentDependencyAdapter{
    public static final VoidContentDependencyAdapter INSTANCE = new VoidContentDependencyAdapter();

    private VoidContentDependencyAdapter() {

    }

    @Override
    public boolean canConnect(ConnectionHandler connection) {
        return false;
    }

    @Override
    public boolean canLoad(ConnectionHandler connection) {
        return false;
    }

    @Override
    public boolean areDependenciesDirty() {
        return false;
    }

    @Override
    public void refreshSources() {

    }

    @Override
    public void beforeLoad(boolean force) {

    }

    @Override
    public void afterLoad() {

    }


    @Override
    public boolean canLoadFast() {
        return true;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    @Override
    public void dispose() {

    }
}

package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.connection.ConnectionHandler;

public class BasicDependencyAdapter implements ContentDependencyAdapter {
    public static BasicDependencyAdapter INSTANCE = new BasicDependencyAdapter();

    @Override
    public boolean canConnect(ConnectionHandler connectionHandler) {
        return connectionHandler != null && connectionHandler.canConnect() && connectionHandler.isValid();
    }

    @Override
    public boolean canLoad(ConnectionHandler connectionHandler) {
        //should reload if connection is valid
        return canConnect(connectionHandler);
    }

    @Override
    public void markSourcesDirty() {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void beforeLoad() {
        // nothing to do before load
    }

    @Override
    public void afterLoad() {
        // nothing to do after load
    }

    @Override
    public void beforeReload(DynamicContent dynamicContent) {

    }

    @Override
    public void afterReload(DynamicContent dynamicContent) {

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

package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.Disposable;

public class ConnectionLoadMonitor implements Disposable {
    private ConnectionHandler connectionHandler;

    public ConnectionLoadMonitor(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    private int activeLoaderCount = 0;

    public boolean isLoading() {
        return activeLoaderCount > 0;
    }

    public void incrementLoaderCount(){
        activeLoaderCount++;
    }

    public void decrementLoaderCount() {
        activeLoaderCount--;
        if(activeLoaderCount == 0 && connectionHandler != null && !connectionHandler.isDisposed() && !connectionHandler.isVirtual()) {
            EventUtil.notify(connectionHandler.getProject(), ConnectionLoadListener.TOPIC).contentsLoaded(connectionHandler);
        }
    }

    @Override
    public void dispose() {
        connectionHandler = null;
    }
}

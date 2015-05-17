package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Counter;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.Disposable;

public class ConnectionLoadMonitor implements Disposable {
    private ConnectionHandlerRef connectionHandlerRef;

    public ConnectionLoadMonitor(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    private Counter runningStatements = new Counter() {
        @Override
        public void onIncrement() {
            updateLastAccess();
        }
        @Override
        public void onDecrement() {
            updateLastAccess();
        }
    };
    private Counter runningMethods = new Counter() {
        @Override
        public void onIncrement() {
            updateLastAccess();
        }

        @Override
        public void onDecrement() {
            updateLastAccess();
        }
    };

    private Counter runningMetaLoaders = new Counter(){
        @Override
        public void onIncrement() {
            updateLastAccess();
        }

        @Override
        public void onDecrement() {
            updateLastAccess();
            ConnectionHandler connectionHandler = getConnectionHandler();
            if(getValue() == 0 && !connectionHandler.isDisposed() && !connectionHandler.isVirtual()) {
                EventUtil.notify(connectionHandler.getProject(), ConnectionLoadListener.TOPIC).contentsLoaded(connectionHandler);
            }
        }
    };

    private void updateLastAccess() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.getConnectionPool().keepAlive(false);
    }

    public Counter getRunningMetaLoaders() {
        return runningMetaLoaders;
    }

    public Counter getRunningStatements() {
        return runningStatements;
    }

    public Counter getRunningMethods() {
        return runningMethods;
    }

    public boolean isLoading() {
        return runningMetaLoaders.getValue() > 0;
    }

    @Override
    public void dispose() {}

    public boolean isIdle() {
        return runningStatements.getValue() == 0 && runningMethods.getValue() == 0;
    }
}

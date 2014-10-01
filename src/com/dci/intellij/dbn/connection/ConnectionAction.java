package com.dci.intellij.dbn.connection;

public abstract class ConnectionAction {
    private ConnectionProvider connectionProvider;
    private boolean[] assertions;

    public ConnectionAction(ConnectionProvider connectionProvider, boolean ... assertions) {
        this.connectionProvider = connectionProvider;
        this.assertions = assertions;
    }

    public void start() {
        if (assertions != null) {
            for (boolean assertion : assertions) {
                if (!assertion) return;
            }
        }

        if (connectionProvider != null) {
            ConnectionHandler connectionHandler = connectionProvider.getConnectionHandler();
            if (connectionHandler != null && !connectionHandler.isDisposed()) {
                boolean canConnect = ConnectionUtil.assertCanConnect(connectionHandler);
                if (canConnect) {
                    execute();
                }
            }
        }
    }

    protected abstract void execute();
}

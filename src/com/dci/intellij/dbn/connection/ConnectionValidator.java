package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.intellij.openapi.Disposable;

public class ConnectionValidator extends SimpleTimeoutCall<Boolean> implements Disposable{
    private ConnectionHandler connectionHandler;
    private Connection connection;

    public ConnectionValidator(ConnectionHandler connectionHandler) {
        super(3, TimeUnit.SECONDS, false);
        this.connectionHandler = connectionHandler;
    }

    public boolean isValid(Connection connection) {
        this.connection = connection;
        try{
            return start();
        } finally {
            this.connection = null;
        }
    }

    @Override
    public final Boolean call() {
        if (connectionHandler != null) {
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.isValid(connection);
        }
        return false;
    }

    @Override
    public void dispose() {
        connectionHandler = null;
        connection = null;
    }
}

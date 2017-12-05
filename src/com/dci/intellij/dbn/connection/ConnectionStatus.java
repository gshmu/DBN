package com.dci.intellij.dbn.connection;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.IntervalChecker;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;

public class ConnectionStatus {
    private ConnectionHandlerRef connectionHandlerRef;

    private AuthenticationError authenticationError;
    private String statusMessage;

    private IntervalChecker valid = new IntervalChecker(TimeUtil.THIRTY_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connectionHandler = connectionHandlerRef.get();
                connectionHandler.getMainConnection();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };

    private IntervalChecker connected = new IntervalChecker(TimeUtil.TEN_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connectionHandler = connectionHandlerRef.get();
                ConnectionPool connectionPool = connectionHandler.getConnectionPool();
                DBNConnection mainConnection = connectionPool.getMainConnection();
                if (mainConnection != null && !mainConnection.isClosed() && mainConnection.isValid()) {
                    return true;
                }

                List<DBNConnection> poolConnections = connectionPool.getPoolConnections();
                for (DBNConnection poolConnection : poolConnections) {
                    if (!poolConnection.isClosed() && poolConnection.isValid()) {
                        return true;
                    }
                }

            } catch (Exception e) {
                return false;
            }
            return false;
        }
    };


    ConnectionStatus(@NotNull ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public boolean isConnected() {
        return connected.check();
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public boolean isValid() {
        return valid.check();
    }

    public void setValid(boolean valid) {
        this.valid.set(valid);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public AuthenticationError getAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(AuthenticationError authenticationError) {
        this.authenticationError = authenticationError;
    }

}

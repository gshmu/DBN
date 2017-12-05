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

    private IntervalChecker valid = new IntervalChecker(true, TimeUtil.THIRTY_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connectionHandler = getConnectionHandler();
                connectionHandler.getMainConnection();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };

    private IntervalChecker connected = new IntervalChecker(false, TimeUtil.TEN_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connectionHandler = getConnectionHandler();
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

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    private boolean canConnect() {
        return getConnectionHandler().canConnect();
    }


    public void setValid(boolean valid) {
        this.valid.set(valid);
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public boolean isConnected() {
        return canConnect() ?
                connected.check() :
                connected.get();
    }

    public boolean isValid() {
        return canConnect() ?
                valid.check() :
                valid.get();
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

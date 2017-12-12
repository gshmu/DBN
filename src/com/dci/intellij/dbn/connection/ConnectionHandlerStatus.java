package com.dci.intellij.dbn.connection;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.LazyResourceStatus;
import com.intellij.openapi.project.Project;

public class ConnectionHandlerStatus {
    private ConnectionHandlerRef connectionHandlerRef;

    private AuthenticationError authenticationError;
    private Throwable connectionException;

    private LazyConnectionStatus active = new LazyConnectionStatus(true, TimeUtil.ONE_SECOND) {
        @Override
        protected boolean doCheck() {
            ConnectionHandler connectionHandler = getConnectionHandler();
            List<DBNConnection> connections = connectionHandler.getConnections();
            for (DBNConnection connection : connections) {
                if (connection.isActive()) {
                    return true;
                }
            }
            return false;
        }
    };

    private LazyConnectionStatus busy = new LazyConnectionStatus(true, TimeUtil.ONE_SECOND) {
        @Override
        protected boolean doCheck() {
            ConnectionHandler connectionHandler = getConnectionHandler();
            List<DBNConnection> connections = connectionHandler.getConnections();
            for (DBNConnection connection : connections) {
                if (connection.hasDataChanges()) {
                    return true;
                }
            }
            return false;
        }
    };

    private LazyConnectionStatus valid = new LazyConnectionStatus(true, TimeUtil.THIRTY_SECONDS) {
        @Override
        protected boolean doCheck() {
            DBNConnection poolConnection = null;
            ConnectionHandler connectionHandler = getConnectionHandler();
            try {
                boolean valid = get();
                ConnectionPool connectionPool = connectionHandler.getConnectionPool();
                if (isConnected() || !valid || connectionPool.wasNeverAccessed()) {
                    poolConnection = connectionPool.allocateConnection(true);
                }
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                connectionHandler.freePoolConnection(poolConnection);
            }
        }
    };

    private LazyConnectionStatus connected = new LazyConnectionStatus(false, TimeUtil.TEN_SECONDS) {
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

    ConnectionHandlerStatus(@NotNull ConnectionHandler connectionHandler) {
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
        return connectionException == null ? null : connectionException.getMessage();
    }

    public Throwable getConnectionException() {
        return connectionException;
    }

    public void setConnectionException(Throwable connectionException) {
        this.connectionException = connectionException;
    }

    public AuthenticationError getAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(AuthenticationError authenticationError) {
        this.authenticationError = authenticationError;
    }

    public boolean isBusy() {
        return busy.check();
    }

    public boolean isActive() {
        return active.check();
    }

    public LazyResourceStatus getActive() {
        return active;
    }

    public LazyResourceStatus getBusy() {
        return busy;
    }

    public LazyResourceStatus getValid() {
        return valid;
    }

    public LazyResourceStatus getConnected() {
        return connected;
    }

    private abstract class LazyConnectionStatus extends LazyResourceStatus {
        LazyConnectionStatus(boolean initialValue, long interval) {
            super(initialValue, interval);
        }

        @Override
        public final void statusChanged() {
            ConnectionHandler connectionHandler = connectionHandlerRef.get();
            Project project = connectionHandler.getProject();
            ConnectionHandlerStatusListener statusListener = EventUtil.notify(project, ConnectionHandlerStatusListener.TOPIC);
            statusListener.statusChanged(connectionHandler.getId());
        }
    }
}

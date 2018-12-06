package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IncrementalStatusAdapter;
import com.dci.intellij.dbn.connection.jdbc.LazyResourceStatus;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConnectionHandlerStatusHolder extends PropertyHolderImpl<ConnectionHandlerStatus> {
    private ConnectionHandlerRef connectionHandlerRef;

    private AuthenticationError authenticationError;
    private Throwable connectionException;

    @Override
    protected ConnectionHandlerStatus[] properties() {
        return ConnectionHandlerStatus.values();
    }

    private LazyConnectionStatus active = new LazyConnectionStatus(ConnectionHandlerStatus.ACTIVE, true, TimeUtil.ONE_SECOND) {
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

    private LazyConnectionStatus busy = new LazyConnectionStatus(ConnectionHandlerStatus.BUSY, true, TimeUtil.ONE_SECOND) {
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

    private LazyConnectionStatus valid = new LazyConnectionStatus(ConnectionHandlerStatus.VALID, true, TimeUtil.THIRTY_SECONDS) {
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

    private LazyConnectionStatus connected = new LazyConnectionStatus(ConnectionHandlerStatus.CONNECTED, false, TimeUtil.TEN_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connectionHandler = getConnectionHandler();
                List<DBNConnection> connections = connectionHandler.getConnections();
                for (DBNConnection connection : connections) {
                    if (connection != null && !connection.isActive() && !connection.isClosed() && connection.isValid()) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }
    };

    private IncrementalStatusAdapter<ConnectionHandlerStatusHolder, ConnectionHandlerStatus> loading =
            new IncrementalStatusAdapter<ConnectionHandlerStatusHolder, ConnectionHandlerStatus>(ConnectionHandlerStatus.LOADING, this) {
                @Override
                protected boolean setInner(ConnectionHandlerStatus status, boolean value) {
                    return ConnectionHandlerStatusHolder.super.set(status, value);
                }

                @Override
                protected void statusChanged() {
                    if (getResource().isNot(ConnectionHandlerStatus.LOADING)) {
                        ConnectionHandler connectionHandler = getConnectionHandler();
                        Project project = connectionHandler.getProject();
                        EventUtil.notify(project, ConnectionLoadListener.TOPIC).contentsLoaded(connectionHandler);
                    }
                }
            };

    ConnectionHandlerStatusHolder(@NotNull ConnectionHandler connectionHandler) {
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

    public IncrementalStatusAdapter<ConnectionHandlerStatusHolder, ConnectionHandlerStatus> getLoading() {
        return loading;
    }

    private abstract class LazyConnectionStatus extends LazyResourceStatus<ConnectionHandlerStatus> {
        LazyConnectionStatus(ConnectionHandlerStatus status, boolean initialValue, long interval) {
            super(ConnectionHandlerStatusHolder.this, status, initialValue, interval);
        }

        @Override
        public final void statusChanged(ConnectionHandlerStatus status) {
            ConnectionHandler connectionHandler = connectionHandlerRef.get();
            Project project = connectionHandler.getProject();
            ConnectionHandlerStatusListener statusListener = EventUtil.notify(project, ConnectionHandlerStatusListener.TOPIC);
            statusListener.statusChanged(connectionHandler.getId(), status);
        }
    }
}

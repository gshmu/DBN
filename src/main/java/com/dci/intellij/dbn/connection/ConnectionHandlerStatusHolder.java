package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.property.PropertyHolderBase;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IncrementalStatusAdapter;
import com.dci.intellij.dbn.connection.jdbc.LatentResourceStatus;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

@Getter
@Setter
public class ConnectionHandlerStatusHolder extends PropertyHolderBase.IntStore<ConnectionHandlerStatus> {
    private final ConnectionRef connection;

    private AuthenticationError authenticationError;
    private Throwable connectionException;

    @Override
    protected ConnectionHandlerStatus[] properties() {
        return ConnectionHandlerStatus.VALUES;
    }

    private final LatentConnectionStatus active = new LatentConnectionStatus(ConnectionHandlerStatus.ACTIVE, true, TimeUtil.Millis.ONE_SECOND) {
        @Override
        protected boolean doCheck() {
            ConnectionHandler connection = getConnection();
            List<DBNConnection> connections = connection.getConnections();
            for (DBNConnection conn : connections) {
                if (conn.isActive()) {
                    return true;
                }
            }
            return false;
        }
    };

    private final LatentConnectionStatus busy = new LatentConnectionStatus(ConnectionHandlerStatus.BUSY, true, TimeUtil.Millis.ONE_SECOND) {
        @Override
        protected boolean doCheck() {
            ConnectionHandler connection = getConnection();
            List<DBNConnection> connections = connection.getConnections();
            for (DBNConnection conn : connections) {
                if (conn.hasDataChanges()) {
                    return true;
                }
            }
            return false;
        }
    };

    private final LatentConnectionStatus valid = new LatentConnectionStatus(ConnectionHandlerStatus.VALID, true, TimeUtil.Millis.THIRTY_SECONDS) {
        @Override
        protected boolean doCheck() {
            DBNConnection poolConnection = null;
            ConnectionHandler connection = getConnection();
            try {
                boolean valid = get();
                ConnectionPool connectionPool = connection.getConnectionPool();
                if (isConnected() || !valid || connectionPool.wasNeverAccessed()) {
                    poolConnection = connectionPool.allocateConnection(true);
                }
                return true;
            } catch (Exception e) {
                conditionallyLog(e);
                return false;
            } finally {
                connection.freePoolConnection(poolConnection);
            }
        }
    };

    private final LatentConnectionStatus connected = new LatentConnectionStatus(ConnectionHandlerStatus.CONNECTED, false, TimeUtil.Millis.TEN_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connection = getConnection();
                List<DBNConnection> connections = connection.getConnections();
                for (DBNConnection conn : connections) {
                    if (conn != null && !conn.isActive() && !conn.isClosed() && conn.isValid()) {
                        return true;
                    }
                }
            } catch (Exception e) {
                conditionallyLog(e);
                return false;
            }
            return false;
        }
    };

    private final IncrementalStatusAdapter<ConnectionHandlerStatusHolder, ConnectionHandlerStatus> loading =
            new IncrementalStatusAdapter<ConnectionHandlerStatusHolder, ConnectionHandlerStatus>(this, ConnectionHandlerStatus.LOADING) {
                @Override
                protected boolean setInner(ConnectionHandlerStatus status, boolean value) {
                    return ConnectionHandlerStatusHolder.super.set(status, value);
                }

                @Override
                protected void statusChanged() {
                    if (true || getResource().isNot(ConnectionHandlerStatus.LOADING)) {
                        ConnectionHandler connection = Failsafe.nn(getConnection());
                        Project project = connection.getProject();
                        ProjectEvents.notify(project,
                                ConnectionLoadListener.TOPIC,
                                (listener) -> listener.contentsLoaded(connection));
                    }
                }
            };

    ConnectionHandlerStatusHolder(@NotNull ConnectionHandler connection) {
        this.connection = connection.ref();
    }

    @NotNull
    private ConnectionHandler getConnection() {
        return connection.ensure();
    }

    private boolean canConnect() {
        return getConnection().canConnect();
    }


    public void setValid(boolean valid) {
        this.valid.set(valid);
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public boolean isConnected() {
        if (isActive()) {
            return true;
        } else {
            return canConnect() ?
                    connected.check() :
                    connected.get();
        }
    }

    public boolean isValid() {
        if (isActive()) {
            return true;
        } else {
            return canConnect() ?
                    valid.check() :
                    valid.get();
        }
    }

    public String getStatusMessage() {
        return connectionException == null ? null : connectionException.getMessage();
    }

    public boolean isBusy() {
        return busy.check();
    }

    public boolean isActive() {
        return active.check();
    }

    public abstract class LatentConnectionStatus extends LatentResourceStatus<ConnectionHandlerStatus> {
        LatentConnectionStatus(ConnectionHandlerStatus status, boolean initialValue, long interval) {
            super(ConnectionHandlerStatusHolder.this, status, initialValue, interval);
        }

        @Override
        public final void statusChanged(ConnectionHandlerStatus status) {
            ConnectionHandler connection = getConnection();
            Project project = connection.getProject();
            ProjectEvents.notify(project,
                    ConnectionHandlerStatusListener.TOPIC,
                    (listener) -> listener.statusChanged(connection.getConnectionId()));
        }
    }
}

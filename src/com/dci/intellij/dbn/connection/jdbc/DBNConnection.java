package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusHolder;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.ACTIVE;
import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.RESERVED;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private String name;
    private ConnectionType type;
    private ConnectionId id;
    private SessionId sessionId;

    private long lastAccess = System.currentTimeMillis();
    private Set<DBNStatement> statements = new HashSet<DBNStatement>();
    private PendingTransactionBundle dataChanges;
    private SchemaId currentSchema;
    private ProjectRef projectRef;

    private IncrementalResourceStatusAdapter<DBNConnection> active =
            IncrementalResourceStatusAdapter.create(
                    DBNConnection.this,
                    ResourceStatus.ACTIVE,
                    (status, value) -> DBNConnection.super.set(status, value));

    private IncrementalResourceStatusAdapter<DBNConnection> reserved =
            IncrementalResourceStatusAdapter.create(
                    DBNConnection.this,
                    ResourceStatus.RESERVED,
                    (status, value) -> DBNConnection.super.set(status, value));

    private ResourceStatusAdapter<DBNConnection> valid =
            new ResourceStatusAdapterImpl<DBNConnection>(this,
                    ResourceStatus.VALID,
                    ResourceStatus.VALID_SETTING,
                    ResourceStatus.VALID_CHECKING,
                    TimeUtil.TEN_SECONDS,
                    Boolean.TRUE,
                    Boolean.FALSE) { // false is terminal status
                @Override
                protected void changeInner(boolean value) throws SQLException {
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return isActive() || inner.isValid(2);
                }
            };

    private ResourceStatusAdapter<DBNConnection> autoCommit =
            new ResourceStatusAdapterImpl<DBNConnection>(this,
                    ResourceStatus.AUTO_COMMIT,
                    ResourceStatus.AUTO_COMMIT_SETTING,
                    ResourceStatus.AUTO_COMMIT_CHECKING,
                    TimeUtil.TEN_SECONDS,
                    Boolean.FALSE,
                    null) { // no terminal status
                @Override
                protected void changeInner(boolean value) throws SQLException {
                    try {
                        try {
                            inner.setAutoCommit(value);
                        } catch (SQLException e) {
                            inner.setAutoCommit(value);
                        }
                    } catch (Throwable e){
                        LOGGER.warn("Unable to set auto-commit to " + value+". Maybe your database does not support transactions...", e);
                    }
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return inner.getAutoCommit();
                }
            };

    public DBNConnection(Project project, Connection connection, String name, ConnectionType type, ConnectionId id, SessionId sessionId) {
        super(connection);
        this.projectRef = ProjectRef.from(project);
        this.name = name;
        this.type = type;
        this.id = id;
        this.sessionId = sessionId;
    }

    @Override
    protected <S extends Statement> S wrap(S statement) {
        updateLastAccess();
        if (statement instanceof CallableStatement) {
            CallableStatement callableStatement = (CallableStatement) statement;
            statement = (S) new DBNCallableStatement(callableStatement, this);

        } else  if (statement instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            statement = (S) new DBNPreparedStatement(preparedStatement, this);

        } else {
            statement = (S) new DBNStatement<Statement>(statement, this);
        }

        if (isPoolConnection()) {
            for (DBNStatement currentStatement : statements) {
                if (TimeUtil.isOlderThan(currentStatement.getInitTimestamp(), TimeUtil.TEN_MINUTES)) {
                    LOGGER.error("Statement not released", new SQLException(currentStatement.traceable.getTrace()));
                }
            }
        }

        statements.add((DBNStatement) statement);
        return statement;
    }

    protected void release(DBNStatement statement) {
        statements.remove(statement);
        updateLastAccess();
    }

    @Override
    public boolean isClosedInner() throws SQLException {
        return inner.isClosed();
    }

    @Override
    public boolean isClosed() {
        // skip checking "closed" on active connections
        return !isActive() && super.isClosed();
    }

    @Override
    public void closeInner() throws SQLException {
        inner.close();
    }

    public ConnectionId getId() {
        return id;
    }

    public ConnectionType getType() {
        return type;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public boolean isPoolConnection() {
        return type == ConnectionType.POOL;
    }

    public boolean isDebugConnection() {
        return type == ConnectionType.DEBUG;
    }

    public boolean isDebuggerConnection() {
        return type == ConnectionType.DEBUGGER;
    }

    public boolean isMainConnection() {
        return type == ConnectionType.MAIN;
    }

    public boolean isTestConnection() {
        return type == ConnectionType.TEST;
    }

    @Override
    public void statusChanged(ResourceStatus status) {
        ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(id);
        if (Failsafe.check(connectionHandler)) {
            ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
            switch (status) {
                case CLOSED: connectionStatus.getConnected().markDirty(); break;
                case VALID: connectionStatus.getValid().markDirty(); break;
                case ACTIVE: connectionStatus.getActive().markDirty(); break;
            }
        }
    }

    public void updateLastAccess() {
        lastAccess = System.currentTimeMillis();
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }

    public static Connection getInner(Connection connection) {
        if (connection instanceof DBNConnection) {
            DBNConnection dbnConnection = (DBNConnection) connection;
            return dbnConnection.getInner();
        }
        return connection;
    }

    @NotNull
    public Project getProject() {
        return projectRef.ensure();
    }

    public String getName() {
        return name;
    }

    /********************************************************************
     *                        Transaction                               *
     ********************************************************************/
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit.set(autoCommit);
    }

    @Override
    public boolean getAutoCommit() {
        return autoCommit.get();
    }

    @Override
    public void commit() throws SQLException {
        updateLastAccess();

        super.commit();
        resetDataChanges();
        notifyStatusChange();
    }

    @Override
    public void rollback() throws SQLException {
        updateLastAccess();

        super.rollback();
        resetDataChanges();
        notifyStatusChange();
    }

    @Override
    public void close() throws SQLException {
        updateLastAccess();

        super.close();
        resetDataChanges();
        notifyStatusChange();
    }

    private void notifyStatusChange() {
        try {
            EventUtil.notify(getProject(),
                    ConnectionStatusListener.TOPIC,
                    (listener) -> listener.statusChanged(id, sessionId));
        } catch (ProcessCanceledException ignore) {}
    }

    public SchemaId getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(SchemaId currentSchema) {
        this.currentSchema = currentSchema;
    }

    /********************************************************************
     *                             Status                               *
     ********************************************************************/
    public boolean isIdle() {
        return !isActive() && !isReserved();
    }

    public boolean isReserved() {
        return is(RESERVED);
    }

    public boolean isActive() {
        return is(ACTIVE);
    }


    public boolean isValid() {
        return isValid(2);
    }

    @Override
    public boolean isValid(int timeout) {
        return valid.get();
    }

    @Override
    public boolean set(ResourceStatus status, boolean value) {
        boolean changed;
        if (status == ACTIVE) {
            changed = active.set(value);

        } else if (status == RESERVED) {
            if (value) {
                if (isActive()) {
                    LOGGER.warn("Reserving active connection");
                } else if (isReserved()) {
                    LOGGER.warn("Reserving already reserved connection");
                }
            }
            changed = reserved.set(value);
        } else {
            changed = super.set(status, value);
            if (changed) statusChanged(status);
        }


        return changed;
    }

    /********************************************************************
     *                             Data changes                         *
     ********************************************************************/
    public void notifyDataChanges(VirtualFile virtualFile) {
        if (!getAutoCommit()) {
            if (dataChanges == null) {
                dataChanges = new PendingTransactionBundle();
            }
            dataChanges.notifyChange(virtualFile);
        }
    }

    public void resetDataChanges() {
        dataChanges = null;
    }

    @Nullable
    public PendingTransactionBundle getDataChanges() {
        return dataChanges;
    }

    public boolean hasDataChanges() {
        return dataChanges != null && !dataChanges.isEmpty();
    }
}

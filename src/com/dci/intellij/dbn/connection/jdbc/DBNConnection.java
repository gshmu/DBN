package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusHolder;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionProperties;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.ACTIVE;
import static com.dci.intellij.dbn.connection.jdbc.ResourceStatus.RESERVED;

@Slf4j
@Getter
@Setter
public class DBNConnection extends DBNConnectionBase {
    private final ProjectRef project;
    private final String name;
    private final ConnectionType type;
    private final ConnectionId id;
    private final SessionId sessionId;
    private final ConnectionProperties properties;

    private long lastAccess = System.currentTimeMillis();
    private PendingTransactionBundle dataChanges;
    private SchemaId currentSchema;

    private final Set<DBNStatement> activeStatements = ConcurrentHashMap.newKeySet();
    private final Set<DBNResultSet> activeCursors = ConcurrentHashMap.newKeySet();
    private final Map<String, DBNPreparedStatement> cachedStatements = new ConcurrentHashMap<>();

    private final IncrementalResourceStatusAdapter<DBNConnection> active =
            IncrementalResourceStatusAdapter.create(
                    DBNConnection.this,
                    ResourceStatus.ACTIVE,
                    (status, value) -> DBNConnection.super.set(status, value));

    private final IncrementalResourceStatusAdapter<DBNConnection> reserved =
            IncrementalResourceStatusAdapter.create(
                    DBNConnection.this,
                    ResourceStatus.RESERVED,
                    (status, value) -> DBNConnection.super.set(status, value));

    private final ResourceStatusAdapter<DBNConnection> valid =
            new ResourceStatusAdapterImpl<DBNConnection>(this,
                    ResourceStatus.VALID,
                    ResourceStatus.CHANGING_VALID,
                    ResourceStatus.CHECKING_VALID,
                    TimeUtil.Millis.TEN_SECONDS,
                    Boolean.TRUE,
                    Boolean.FALSE) { // false is terminal status
                @Override
                protected void changeInner(boolean value) {
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return isActive() || inner.isValid(2);
                }
            };

    private final ResourceStatusAdapter<DBNConnection> autoCommit =
            new ResourceStatusAdapterImpl<DBNConnection>(this,
                    ResourceStatus.AUTO_COMMIT,
                    ResourceStatus.CHANGING_AUTO_COMMIT,
                    ResourceStatus.CHECKING_AUTO_COMMIT,
                    TimeUtil.Millis.TEN_SECONDS,
                    Boolean.FALSE,
                    null) { // no terminal status
                @Override
                protected void changeInner(boolean value) {
                    try {
                        try {
                            inner.setAutoCommit(value);
                        } catch (SQLException e) {
                            inner.setAutoCommit(value);
                        }
                    } catch (Throwable e){
                        log.warn("Unable to set auto-commit to " + value + ". Maybe your database does not support transactions...", e);
                    }
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return inner.getAutoCommit();
                }
            };

    private final ResourceStatusAdapter<DBNConnection> readOnly =
            new ResourceStatusAdapterImpl<DBNConnection>(this,
                    ResourceStatus.READ_ONLY,
                    ResourceStatus.CHANGING_READ_ONLY,
                    ResourceStatus.CHECKING_READ_ONLY,
                    TimeUtil.Millis.TEN_SECONDS,
                    Boolean.FALSE,
                    null) { // no terminal status
                @Override
                protected void changeInner(boolean value) {
                    try {
                        try {
                            inner.setReadOnly(value);
                        } catch (SQLException e) {
                            inner.setReadOnly(value);
                        }
                    } catch (Throwable e){
                        log.warn("Unable to set read-only status to " + value + ". Maybe your database does not support transactions...", e);
                    }
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return inner.isReadOnly();
                }
            };

    private DBNConnection(Project project, Connection connection, String name, ConnectionType type, ConnectionId id, SessionId sessionId) throws SQLException {
        super(connection);
        this.project = ProjectRef.of(project);
        this.name = name;
        this.type = type;
        this.id = id;
        this.sessionId = sessionId;
        this.properties = new ConnectionProperties(connection);
    }

    public static DBNConnection wrap(Project project, Connection connection, String name, ConnectionType type, ConnectionId id, SessionId sessionId) throws SQLException {
        return new DBNConnection(project, connection, name, type, id, sessionId);
    }


    public DBNPreparedStatement prepareStatementCached(String sql) {
        return cachedStatements.computeIfAbsent(sql, key -> {
            DBNPreparedStatement preparedStatement = prepareStatement(key);
            preparedStatement.setCached(true);
            preparedStatement.setFetchSize(10000);
            return preparedStatement;
        });
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

        activeStatements.add((DBNStatement) statement);
        return statement;
    }

    protected void park(DBNStatement statement) {
        activeStatements.remove(statement);
        updateLastAccess();
    }

    protected void release(DBNStatement statement) {
        park(statement);
        if (statement.isCached() && statement instanceof DBNPreparedStatement) {
            DBNPreparedStatement preparedStatement = (DBNPreparedStatement) statement;
            cachedStatements.values().removeIf(v -> v == preparedStatement);
        }

        updateLastAccess();
    }

    protected void release(DBNResultSet resultSet) {
        activeCursors.remove(resultSet);
        updateLastAccess();
    }

    public int getActiveStatementCount() {
        return activeStatements.size();
    }

    public int getCachedStatementCount() {
        return cachedStatements.size();
    }

    public int getActiveCursorCount() {
        return activeCursors.size();
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

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.Millis.ONE_MINUTE);
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
        return project.ensure();
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
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly.set(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.readOnly.get();
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
        try {
            super.close();
            updateLastAccess();
            List<DBNPreparedStatement> statements = new ArrayList<>(cachedStatements.values());
            cachedStatements.clear();
            ResourceUtil.close(statements);
        } finally {
            resetDataChanges();
            notifyStatusChange();
        }
    }

    private void notifyStatusChange() {
        try {
            ProjectEvents.notify(getProject(),
                    ConnectionStatusListener.TOPIC,
                    (listener) -> listener.statusChanged(id, sessionId));
        } catch (ProcessCanceledException ignore) {}
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
                    log.warn("Reserving active connection");
                } else if (isReserved()) {
                    log.warn("Reserving already reserved connection");
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
    public void notifyDataChanges(@NotNull VirtualFile virtualFile) {
        if (!getAutoCommit()) {
            if (dataChanges == null) {
                dataChanges = new PendingTransactionBundle();
            }
            dataChanges.notifyChange(virtualFile, this);
        }
    }

    public void resetDataChanges() {
        dataChanges = null;
    }

    public boolean hasDataChanges() {
        return dataChanges != null && !dataChanges.isEmpty() && !isClosed();
    }
}

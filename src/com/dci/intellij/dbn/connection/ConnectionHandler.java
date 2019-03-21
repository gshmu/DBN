package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Referenceable;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleBundle;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.execution.statement.StatementExecutionQueue;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface ConnectionHandler extends Disposable, EnvironmentTypeProvider, ConnectionProvider, Presentable, ConnectionIdProvider, Referenceable<ConnectionHandlerRef> {
    @NotNull
    Project getProject();
    DBNConnection getTestConnection() throws SQLException;

    @NotNull
    DBNConnection getMainConnection() throws SQLException;

    @NotNull
    DBNConnection getMainConnection(@Nullable SchemaId schemaId) throws SQLException;

    @NotNull
    DBNConnection getConnection(SessionId sessionId, @Nullable SchemaId schemaId) throws SQLException;

    @NotNull
    DBNConnection getDebugConnection(@Nullable SchemaId schemaId) throws SQLException;

    @NotNull
    DBNConnection getDebuggerConnection() throws SQLException;

    @NotNull
    DBNConnection getPoolConnection(boolean readonly) throws SQLException;

    @NotNull
    DBNConnection getPoolConnection(@Nullable SchemaId schemaId, boolean readonly) throws SQLException;

    void closeConnection(DBNConnection connection);

    void freePoolConnection(DBNConnection connection);

    ConnectionSettings getSettings();

    void setSettings(ConnectionSettings connectionSettings);

    @NotNull
    List<DBNConnection> getConnections(ConnectionType... connectionTypes);

    @NotNull
    ConnectionHandlerStatusHolder getConnectionStatus();

    @NotNull
    DatabaseConsoleBundle getConsoleBundle();

    @NotNull
    DatabaseSessionBundle getSessionBundle();

    @NotNull
    DBSessionBrowserVirtualFile getSessionBrowserFile();

    ConnectionInstructions getInstructions();

    void setTemporaryAuthenticationInfo(AuthenticationInfo temporaryAuthenticationInfo);

    @Nullable
    ConnectionInfo getConnectionInfo();

    @NotNull
    String getConnectionName(@Nullable DBNConnection connection);

    void setConnectionInfo(ConnectionInfo connectionInfo);

    @NotNull
    AuthenticationInfo getTemporaryAuthenticationInfo();

    boolean canConnect();

    boolean isAuthenticationProvided();

    boolean isDatabaseInitialized();

    @NotNull ConnectionBundle getConnectionBundle();
    @NotNull ConnectionPool getConnectionPool();
    DatabaseInterfaceProvider getInterfaceProvider();
    @NotNull DBObjectBundle getObjectBundle();
    @Nullable
    SchemaId getUserSchema();
    @Nullable
    SchemaId getDefaultSchema();
    @NotNull List<SchemaId> getSchemaIds();

    @Nullable
    DBSchema getSchema(SchemaId schema);

    boolean isValid();
    boolean isVirtual();
    boolean isAutoCommit();
    boolean isLoggingEnabled();
    boolean hasPendingTransactions(@NotNull DBNConnection connection);
    void setAutoCommit(boolean autoCommit);
    void setLoggingEnabled(boolean loggingEnabled);
    void disconnect() throws SQLException;

    ConnectionId getConnectionId();
    String getUserName();
    String getPresentableText();
    String getQualifiedName();

    @Nullable
    DBLanguageDialect resolveLanguageDialect(Language language);
    DBLanguageDialect getLanguageDialect(DBLanguage language);
    boolean isEnabled();

    DatabaseType getDatabaseType();
    double getDatabaseVersion();

    Filter<BrowserTreeNode> getObjectTypeFilter();

    boolean isConnected();

    boolean isConnected(SessionId sessionId);

    ConnectionHandlerRef getRef();

    DatabaseInfo getDatabaseInfo();
    AuthenticationInfo getAuthenticationInfo();

    @Deprecated
    boolean hasUncommittedChanges();

    @NotNull
    StatementExecutionQueue getExecutionQueue(SessionId sessionId);

    @NotNull
    PsiDirectory getPsiDirectory();

    static List<ConnectionId> ids(List<ConnectionHandler> connectionHandlers) {
        return CollectionUtil.map(connectionHandlers, (connectionHandler) -> connectionHandler.getConnectionId()) ;
    }
}

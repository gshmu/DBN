package com.dci.intellij.dbn.connection;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleBundle;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;

public interface ConnectionHandler extends Disposable, EnvironmentTypeProvider, ConnectionProvider, Presentable {
    @NotNull
    Project getProject();
    DBNConnection getTestConnection() throws SQLException;

    @NotNull
    DBNConnection getMainConnection() throws SQLException;

    @NotNull
    DBNConnection getMainConnection(@Nullable DBSchema schema) throws SQLException;

    @NotNull
    DBNConnection getPoolConnection(boolean readonly) throws SQLException;

    @NotNull
    DBNConnection getPoolConnection(@Nullable DBSchema schema, boolean readonly) throws SQLException;

    void freePoolConnection(DBNConnection connection);
    void dropPoolConnection(DBNConnection connection);
    ConnectionSettings getSettings();

    void setSettings(ConnectionSettings connectionSettings);

    @NotNull
    List<DBNConnection> getConnections(ConnectionType... connectionTypes);

    @NotNull
    ConnectionStatus getConnectionStatus();
    DatabaseConsoleBundle getConsoleBundle();
    DBSessionBrowserVirtualFile getSessionBrowserFile();
    ConnectionInstructions getInstructions();

    void setTemporaryAuthenticationInfo(AuthenticationInfo temporaryAuthenticationInfo);

    @Nullable
    ConnectionInfo getConnectionInfo();

    void setConnectionInfo(ConnectionInfo connectionInfo);

    @NotNull
    AuthenticationInfo getTemporaryAuthenticationInfo();

    boolean canConnect();

    boolean isAuthenticationProvided();

    boolean isDatabaseInitialized();

    @NotNull ConnectionBundle getConnectionBundle();
    @NotNull ConnectionPool getConnectionPool();
    @Deprecated
    ConnectionLoadMonitor getLoadMonitor();
    DatabaseInterfaceProvider getInterfaceProvider();
    @NotNull DBObjectBundle getObjectBundle();
    @Nullable DBSchema getUserSchema();
    @Nullable DBSchema getDefaultSchema();

    boolean isValid(boolean check);
    boolean isValid();
    boolean isVirtual();
    boolean isAutoCommit();
    boolean isLoggingEnabled();
    boolean hasPendingTransactions(@NotNull DBNConnection connection);
    void setAutoCommit(boolean autoCommit) throws SQLException;
    void setLoggingEnabled(boolean loggingEnabled);
    void disconnect() throws SQLException;

    String getId();
    String getUserName();
    String getPresentableText();
    String getQualifiedName();

    void commit() throws SQLException;
    void rollback() throws SQLException;
    void ping(boolean check);

    @Nullable
    DBLanguageDialect resolveLanguageDialect(Language language);
    DBLanguageDialect getLanguageDialect(DBLanguage language);
    boolean isActive();

    DatabaseType getDatabaseType();
    double getDatabaseVersion();

    Filter<BrowserTreeNode> getObjectTypeFilter();
    NavigationPsiCache getPsiCache();

    boolean isConnected();
    int getIdleMinutes();

    ConnectionHandlerRef getRef();

    DatabaseInfo getDatabaseInfo();
    AuthenticationInfo getAuthenticationInfo();
    Set<TransactionAction> getPendingActions();

    @Deprecated
    boolean hasUncommittedChanges();
}

package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DisposableLazyValue;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleBundle;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectBundleImpl;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class ConnectionHandlerImpl extends DisposableBase implements ConnectionHandler {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private ConnectionSettings connectionSettings;
    private ConnectionBundle connectionBundle;
    private ConnectionHandlerStatusHolder connectionStatus;
    private ConnectionPool connectionPool;
    private ConnectionLoadMonitor loadMonitor;
    private DatabaseInterfaceProvider interfaceProvider;
    private DatabaseConsoleBundle consoleBundle;
    private DatabaseSessionBundle sessionBundle;
    private DBSessionBrowserVirtualFile sessionBrowserFile;
    private ConnectionInstructions instructions = new ConnectionInstructions();

    private boolean enabled;
    private ConnectionHandlerRef ref;
    private AuthenticationInfo temporaryAuthenticationInfo = new AuthenticationInfo();
    private ConnectionInfo connectionInfo;
    private Cache metaDataCache = new Cache(TimeUtil.ONE_MINUTE);

    @Override
    public Set<TransactionAction> getPendingActions() {
        return pendingActions;
    }

    private Set<TransactionAction> pendingActions = new HashSet<TransactionAction>();


    private LazyValue<NavigationPsiCache> psiCache = new DisposableLazyValue<NavigationPsiCache>(this) {
        @Override
        protected NavigationPsiCache load() {
            return new NavigationPsiCache(ConnectionHandlerImpl.this);
        }
    };

    private LazyValue<DBObjectBundle> objectBundle = new DisposableLazyValue<DBObjectBundle>(this) {
        @Override
        protected DBObjectBundle load() {
            return new DBObjectBundleImpl(ConnectionHandlerImpl.this, connectionBundle);
        }
    };


    public ConnectionHandlerImpl(ConnectionBundle connectionBundle, ConnectionSettings connectionSettings) {
        this.connectionBundle = connectionBundle;
        this.connectionSettings = connectionSettings;
        this.enabled = connectionSettings.isActive();
        ref = new ConnectionHandlerRef(this);

        connectionStatus = new ConnectionHandlerStatusHolder(this);
        connectionPool = new ConnectionPool(this);
        consoleBundle = new DatabaseConsoleBundle(this);
        sessionBundle = new DatabaseSessionBundle(this);
        loadMonitor = new ConnectionLoadMonitor(this);

        Disposer.register(this, loadMonitor);
    }

    @NotNull
    @Override
    public List<DBNConnection> getConnections(ConnectionType... connectionTypes) {
        return getConnectionPool().getConnections(connectionTypes);
    }

    @Override
    public ConnectionInstructions getInstructions() {
        return instructions;
    }

    @Override
    public void setTemporaryAuthenticationInfo(AuthenticationInfo temporaryAuthenticationInfo) {
        this.temporaryAuthenticationInfo = temporaryAuthenticationInfo;
    }

    @Override
    @Nullable
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    @NotNull
    public AuthenticationInfo getTemporaryAuthenticationInfo() {
        if (temporaryAuthenticationInfo.isProvided()) {
            int passwordExpiryTime = getSettings().getDetailSettings().getPasswordExpiryTime() * 60000;
            long lastAccessTimestamp = getConnectionPool().getLastAccessTimestamp();
            if (temporaryAuthenticationInfo.isOlderThan(passwordExpiryTime) && TimeUtil.isOlderThan(lastAccessTimestamp, passwordExpiryTime)) {
                temporaryAuthenticationInfo = new AuthenticationInfo();
            }
        }
        return temporaryAuthenticationInfo;
    }

    @Override
    public boolean canConnect() {
        if (isDisposed()) {
            return false;
        }

        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        if (!databaseSettings.isDatabaseInitialized() && !instructions.isAllowAutoInit()) {
            return false;
        }

        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        if (detailSettings.isConnectAutomatically() || instructions.isAllowAutoConnect()) {
            return isAuthenticationProvided();
        }
        return false;
    }

    @Override
    public boolean isAuthenticationProvided() {
        return getAuthenticationInfo().isProvided() || getTemporaryAuthenticationInfo().isProvided();
    }

    @Override
    public boolean isDatabaseInitialized() {
        return getSettings().getDatabaseSettings().isDatabaseInitialized();
    }

    @NotNull
    public ConnectionBundle getConnectionBundle() {
        return FailsafeUtil.get(connectionBundle);
    }

    public ConnectionSettings getSettings() {
        return connectionSettings;
    }

    @Override
    public void setSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
        this.enabled = connectionSettings.isActive();
    }

    @NotNull
    public ConnectionHandlerStatusHolder getConnectionStatus() {
        return connectionStatus;
    }

    @NotNull
    @Override
    public DatabaseConsoleBundle getConsoleBundle() {
        return consoleBundle;
    }

    @NotNull
    @Override
    public DatabaseSessionBundle getSessionBundle() {
        return sessionBundle;
    }

    @Override
    public DBSessionBrowserVirtualFile getSessionBrowserFile() {
        if (sessionBrowserFile == null) {
            sessionBrowserFile = new DBSessionBrowserVirtualFile(this);
            Disposer.register(this, sessionBrowserFile);
        }
        return sessionBrowserFile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DatabaseType getDatabaseType() {
        return connectionSettings.getDatabaseSettings().getDatabaseType();
    }

    @Override
    public double getDatabaseVersion() {
        return connectionSettings.getDatabaseSettings().getDatabaseVersion();
    }

    public Filter<BrowserTreeNode> getObjectTypeFilter() {
        return connectionSettings.getFilterSettings().getObjectTypeFilterSettings().getElementFilter();
    }

    @Override
    public NavigationPsiCache getPsiCache() {
        return psiCache.get();
    }

    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        return connectionSettings.getDetailSettings().getEnvironmentType();
    }

    public void commit() {
        DBNConnection mainConnection = getConnectionPool().getMainConnection();
        ConnectionUtil.commit(mainConnection);
    }

    public void rollback() {
        DBNConnection mainConnection = getConnectionPool().getMainConnection();
        ConnectionUtil.rollback(mainConnection);
    }

    @Override
    public void ping(boolean check) {
        getConnectionPool().keepAlive(check);
    }

    @Override
    public boolean isConnected() {
        return connectionStatus.isConnected();
    }

    public String toString() {
        return getPresentableText();
    }

    @NotNull
    public Project getProject() {
        return getConnectionBundle().getProject();
    }

    @Override
    public int getIdleMinutes() {
        return connectionPool == null ? 0 : connectionPool.getIdleMinutes();
    }

    @Override
    public ConnectionHandlerRef getRef() {
        return ref;
    }

    @Override
    public DatabaseInfo getDatabaseInfo() {
        return getSettings().getDatabaseSettings().getDatabaseInfo();
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo() {
        return getSettings().getDatabaseSettings().getAuthenticationInfo();
    }

    public boolean isValid() {
        return connectionStatus.isValid();
    }

    public boolean isVirtual() {
        return false;
    }

    @Override
    public boolean isAutoCommit() {
        return connectionSettings.getPropertiesSettings().isEnableAutoCommit();
    }

    @Override
    public boolean isLoggingEnabled() {
        return connectionSettings.getDetailSettings().isEnableDatabaseLogging();
    }

    @Override
    public boolean hasPendingTransactions(@NotNull DBNConnection connection) {
        return getInterfaceProvider().getMetadataInterface().hasPendingTransactions(connection);
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        connectionSettings.getDetailSettings().setEnableDatabaseLogging(loggingEnabled);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        getConnectionPool().setAutoCommit(autoCommit);
        connectionSettings.getPropertiesSettings().setEnableAutoCommit(autoCommit);
    }

    public void disconnect() {
        // explicit disconnect (reset auto-connect data)
        temporaryAuthenticationInfo = new AuthenticationInfo();
        instructions.setAllowAutoConnect(false);
        connectionStatus.setConnected(false);
        getConnectionPool().closeConnections();
    }

    public ConnectionId getId() {
        return connectionSettings.getConnectionId();
    }

    public String getUserName() {
        return CommonUtil.nvl(connectionSettings.getDatabaseSettings().getAuthenticationInfo().getUser(), "");
    }

    public ConnectionLoadMonitor getLoadMonitor() {
        return loadMonitor;
    }

    @NotNull
    public DBObjectBundle getObjectBundle() {
        return objectBundle.get();
    }

    public DBSchema getUserSchema() {
        String userName = getUserName().toUpperCase();
        return getObjectBundle().getSchema(userName);
    }

    @Override
    public DBSchema getDefaultSchema() {
        DBSchema schema = getUserSchema();
        if (schema == null) {
            String databaseName = getSettings().getDatabaseSettings().getDatabaseInfo().getDatabase();
            if (StringUtil.isNotEmpty(databaseName)) {
                schema = getObjectBundle().getSchema(databaseName);
            }
            if (schema == null) {
                List<DBSchema> schemas = getObjectBundle().getSchemas();
                if (schemas.size() > 0) {
                    schema = schemas.get(0);
                }
            }
        }
        return schema;
    }

    @Override
    public DBNConnection getTestConnection() throws SQLException {
        assertCanConnect();
        return getConnectionPool().ensureTestConnection();
    }

    @NotNull
    public DBNConnection getMainConnection() throws SQLException {
        assertCanConnect();
        return getConnectionPool().ensureMainConnection();
    }

    @NotNull
    public DBNConnection getPoolConnection(boolean readonly) throws SQLException {
        assertCanConnect();
        return getConnectionPool().allocateConnection(readonly);
    }

    @NotNull
    public DBNConnection getMainConnection(@Nullable DBSchema schema) throws SQLException {
        DBNConnection connection = getMainConnection();
        if (schema != null && !schema.isPublicSchema() && DatabaseFeature.CURRENT_SCHEMA.isSupported(this)) {
            DatabaseMetadataInterface metadataInterface = getInterfaceProvider().getMetadataInterface();
            metadataInterface.setCurrentSchema(schema.getQuotedName(false), connection);
        }
        return connection;
    }

    @NotNull
    public DBNConnection getPoolConnection(@Nullable DBSchema schema, boolean readonly) throws SQLException {
        DBNConnection connection = getPoolConnection(readonly);
        //if (!schema.isPublicSchema()) {
        setCurrentSchema(connection, schema);

        //}
        return connection;
    }

    @NotNull
    public DBNConnection getConnection(@NotNull SessionId sessionId, @Nullable DBSchema schema) throws SQLException {
        DBNConnection connection =
                sessionId == SessionId.MAIN ? getMainConnection() :
                sessionId == SessionId.POOL ? getPoolConnection(false) :
                getConnectionPool().ensureSessionConnection(sessionId);
        return setCurrentSchema(connection, schema);
    }

    protected DBNConnection setCurrentSchema(DBNConnection connection, @Nullable DBSchema schema) throws SQLException {
        if (schema != null && DatabaseFeature.CURRENT_SCHEMA.isSupported(this)) {
            DatabaseMetadataInterface metadataInterface = getInterfaceProvider().getMetadataInterface();
            metadataInterface.setCurrentSchema(schema.getQuotedName(false), connection);
        }
        return connection;
    }


    private void assertCanConnect() throws SQLException {
        if (!canConnect()) {
            throw DatabaseInterface.DBN_NOT_CONNECTED_EXCEPTION;
        }
    }

    public void freePoolConnection(DBNConnection connection) {
        if (!isDisposed()) {
            getConnectionPool().releaseConnection(connection);
        }
    }

    @Override
    public void dropPoolConnection(DBNConnection connection) {
        getConnectionPool().dropConnection(connection);
    }

    @NotNull
    public ConnectionPool getConnectionPool() {
        return FailsafeUtil.get(connectionPool);
    }

    public DatabaseInterfaceProvider getInterfaceProvider() {
        if (!isValidInterfaceProvider()) {
            synchronized (this) {
                if (!isValidInterfaceProvider()) {
                    try {
                        interfaceProvider = DatabaseInterfaceProviderFactory.getInterfaceProvider(this);
                    } catch (SQLException e) {

                    }
                }
            }
        }
        if (interfaceProvider != null) {
            interfaceProvider.setProject(getProject());
            interfaceProvider.setMetaDataCache(metaDataCache);
        }

        // do not initialize
        return interfaceProvider == null ? DatabaseInterfaceProviderFactory.GENERIC_INTERFACE_PROVIDER : interfaceProvider;
    }

    boolean isValidInterfaceProvider() {
        return interfaceProvider != null && interfaceProvider.getDatabaseType() == getDatabaseType();
    }

    @Override
    public DBLanguageDialect resolveLanguageDialect(Language language) {
        if (language instanceof DBLanguageDialect) {
            return (DBLanguageDialect) language;
        } else if (language instanceof DBLanguage) {
            return getLanguageDialect((DBLanguage) language);
        }
        return null;
    }

    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        return getInterfaceProvider().getLanguageDialect(language);
    }

    public static Comparator<ConnectionHandler> getComparator(boolean asc) {
        return asc ? ASC_COMPARATOR : DESC_COMPARATOR;
    }

    public static final Comparator<ConnectionHandler> ASC_COMPARATOR = new Comparator<ConnectionHandler>() {
        public int compare(ConnectionHandler connection1, ConnectionHandler connection2) {
            return connection1.getPresentableText().toLowerCase().compareTo(connection2.getPresentableText().toLowerCase());
        }
    };

    public static final Comparator<ConnectionHandler> DESC_COMPARATOR = new Comparator<ConnectionHandler>() {
        public int compare(ConnectionHandler connection1, ConnectionHandler connection2) {
            return connection2.getPresentableText().toLowerCase().compareTo(connection1.getPresentableText().toLowerCase());
        }
    };

    /*********************************************************
     *                       TreeElement                     *
     *********************************************************/
    public String getQualifiedName() {
        return getPresentableText();
    }

    @NotNull
    public String getName() {
        return connectionSettings.getDatabaseSettings().getName();
    }

    public String getDescription() {
        return connectionSettings.getDatabaseSettings().getDescription();
    }

    public String getPresentableText(){
        return connectionSettings.getDatabaseSettings().getName();
    }

    public Icon getIcon(){
        if (connectionStatus.isConnected()) {
            return
                connectionStatus.isBusy() ? Icons.CONNECTION_BUSY :
                connectionStatus.isActive() ? Icons.CONNECTION_ACTIVE :
                    Icons.CONNECTION_CONNECTED;
        } else {
            return connectionStatus.isValid() ?
                    Icons.CONNECTION_INACTIVE :
                    Icons.CONNECTION_INVALID;
        }
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return this;
    }

    @Override
    @Deprecated
    public boolean hasUncommittedChanges() {
        List<DBNConnection> connections = getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
        for (DBNConnection connection : connections) {
            if (connection.hasDataChanges()) {
                return true;
            }
        }
        return false;
    }

    /*********************************************************
    *                      Disposable                       *
    *********************************************************/
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            connectionPool = null;
            connectionBundle = null;
            loadMonitor = null;
            sessionBrowserFile = null;
            psiCache = null;
        }
    }

    public void setConnectionConfig(final ConnectionSettings connectionSettings) {
        boolean refresh = this.connectionSettings.getDatabaseSettings().hashCode() != connectionSettings.getDatabaseSettings().hashCode();
        this.connectionSettings = connectionSettings;
        if (refresh) {
            connectionPool.closeConnections();

            final Project project = getProject();
            new BackgroundTask(getProject(), "Trying to connect to " + getName(), false) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    ConnectionManager.testConnection(ConnectionHandlerImpl.this, false, false);
                    //fixme check if the connection is pointing to a new database and reload if this is the case
                    //objectBundle.checkForDatabaseChange();

                    EventUtil.notify(project, BrowserTreeEventListener.TOPIC).nodeChanged(getObjectBundle(), TreeEventType.NODES_CHANGED);
                }
            }.start();
        }
    }
}

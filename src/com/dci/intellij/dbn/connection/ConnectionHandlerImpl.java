package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DisposableLazyValue;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleBundle;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectBundleImpl;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class ConnectionHandlerImpl implements ConnectionHandler {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private ConnectionSettings connectionSettings;
    private ConnectionBundle connectionBundle;
    private ConnectionStatus connectionStatus;
    private ConnectionPool connectionPool;
    private ConnectionLoadMonitor loadMonitor;
    private DatabaseInterfaceProvider interfaceProvider;
    private UncommittedChangeBundle changesBundle;
    private DatabaseConsoleBundle consoleBundle;
    private DBSessionBrowserVirtualFile sessionBrowserFile;
    private DatabaseLoggingResult logOutput;

    private boolean isActive;
    private boolean isDisposed;
    private boolean checkingIdleStatus;
    private boolean allowConnection;
    private long validityCheckTimestamp = 0;
    private ConnectionHandlerRef ref;
    private AuthenticationInfo temporaryAuthenticationInfo = new AuthenticationInfo();
    private ConnectionInfo connectionInfo;

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
        this.isActive = connectionSettings.isActive();
        ref = new ConnectionHandlerRef(this);

        connectionStatus = new ConnectionStatus();
        connectionPool = new ConnectionPool(this);
        consoleBundle = new DatabaseConsoleBundle(this);
        loadMonitor = new ConnectionLoadMonitor(this);

        Disposer.register(this, connectionPool);
        Disposer.register(this, consoleBundle);
        Disposer.register(this, loadMonitor);
    }

    @Override
    public boolean isAllowConnection() {
        return allowConnection;
    }

    @Override
    public void setAllowConnection(boolean allowConnection) {
        this.allowConnection = allowConnection;
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
        if (isDisposed) {
            return false;
        }

        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        if (allowConnection || detailSettings.isConnectAutomatically()) {
            if (isAuthenticationProvided()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAuthenticationProvided() {
        return getAuthenticationInfo().isProvided() || getTemporaryAuthenticationInfo().isProvided();
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
        this.isActive = connectionSettings.isActive();
    }

    @NotNull
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public DatabaseConsoleBundle getConsoleBundle() {
        return consoleBundle;
    }

    @Override
    public DBSessionBrowserVirtualFile getSessionBrowserFile() {
        if (sessionBrowserFile == null) {
            sessionBrowserFile = new DBSessionBrowserVirtualFile(this);
            Disposer.register(this, sessionBrowserFile);
        }
        return sessionBrowserFile;
    }

    public boolean isActive() {
        return isActive;
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

    public boolean hasUncommittedChanges() {
        return changesBundle != null && !changesBundle.isEmpty();
    }

    public void commit() throws SQLException {
        connectionPool.getStandaloneConnection(false).commit();
        changesBundle = null;
    }

    public void rollback() throws SQLException {
        connectionPool.getStandaloneConnection(false).rollback();
        changesBundle = null;
    }

    @Override
    public void ping(boolean check) {
        connectionPool.keepAlive(check);
    }

    public void notifyChanges(VirtualFile virtualFile) {
        if (!isAutoCommit()) {
            if (changesBundle == null) {
                changesBundle = new UncommittedChangeBundle();
            }
            changesBundle.notifyChange(virtualFile);
        }
    }

    @Override
    public void resetChanges() {
        changesBundle = null;
    }

    @Override
    public UncommittedChangeBundle getUncommittedChanges() {
        return changesBundle;
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

    public boolean isValid(boolean check) {
        if (check) {
            try {
                getStandaloneConnection();
            } catch (SQLException e) {
                return false;
            }
        }
        return isValid();
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
        if (getConnectionBundle().containsConnection(this)) {
            long currentTimestamp = System.currentTimeMillis();
            if (validityCheckTimestamp < currentTimestamp - 30000 && !ApplicationManager.getApplication().isDispatchThread()) {
                validityCheckTimestamp = currentTimestamp;
                try {
                    getStandaloneConnection();
                } catch (SQLException e) {
                    if (SettingsUtil.isDebugEnabled) {
                        LOGGER.warn("[DBN-INFO] Could not connect to database [" + getName() + "]: " + e.getMessage());
                    }
                }
            }
            return connectionStatus.isValid();
        }
        return false;
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

    public void setLoggingEnabled(boolean loggingEnabled) {
        connectionSettings.getDetailSettings().setEnableDatabaseLogging(loggingEnabled);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connectionPool.setAutoCommit(autoCommit);
        connectionSettings.getPropertiesSettings().setEnableAutoCommit(autoCommit);
    }

    public void disconnect() throws SQLException {
        try {
            connectionPool.closeConnections();
            changesBundle = null;
            temporaryAuthenticationInfo = new AuthenticationInfo();
        } finally {
            connectionStatus.setConnected(false);
        }
    }

    public String getId() {
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
        DBSchema defaultSchema = getObjectBundle().getSchema(userName);
        if (defaultSchema == null) {
            List<DBSchema> schemas = getObjectBundle().getSchemas();
            if (schemas.size() > 0) {
                return schemas.get(0);
            }
        }

        return defaultSchema;
    }

    public Connection getStandaloneConnection() throws SQLException {
        assertCanConnect();
        return connectionPool.getStandaloneConnection(true);
    }

    public Connection getPoolConnection() throws SQLException {
        assertCanConnect();
        return connectionPool.allocateConnection();
    }

    public Connection getStandaloneConnection(@Nullable DBSchema schema) throws SQLException {
        Connection connection = getStandaloneConnection();
        if (schema != null && !schema.isPublicSchema()) {
            DatabaseMetadataInterface metadataInterface = getInterfaceProvider().getMetadataInterface();
            metadataInterface.setCurrentSchema(schema.getQuotedName(false), connection);
        }
        return connection;
    }

    public Connection getPoolConnection(@Nullable DBSchema schema) throws SQLException {
        Connection connection = getPoolConnection();
        //if (!schema.isPublicSchema()) {
        if (schema != null) {
            DatabaseMetadataInterface metadataInterface = getInterfaceProvider().getMetadataInterface();
            metadataInterface.setCurrentSchema(schema.getQuotedName(false), connection);
        }

        //}
        return connection;
    }

    private void assertCanConnect() throws SQLException {
        if (!canConnect()) {
            throw DatabaseInterface.DBN_NOT_CONNECTED_EXCEPTION;
        }
    }

    public void freePoolConnection(Connection connection) {
        getConnectionPool().releaseConnection(connection);
    }

    @Override
    public void dropPoolConnection(Connection connection) {
        getConnectionPool().dropConnection(connection);
    }

    @NotNull
    public ConnectionPool getConnectionPool() {
        return FailsafeUtil.get(connectionPool);
    }

    public DatabaseInterfaceProvider getInterfaceProvider() {
        if (interfaceProvider == null || interfaceProvider.getDatabaseType() != getDatabaseType()) {
            synchronized (this) {
                if (interfaceProvider == null || interfaceProvider.getDatabaseType() != getDatabaseType()) {
                    try {
                        interfaceProvider = DatabaseInterfaceProviderFactory.getInterfaceProvider(this);
                    } catch (SQLException e) {

                    }
                }
            }
        }
        if (interfaceProvider != null) {
            interfaceProvider.setProject(getProject());
        }

        // do not initialize
        return interfaceProvider == null ? DatabaseInterfaceProviderFactory.GENERIC_INTERFACE_PROVIDER : interfaceProvider;
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
        return connectionStatus.isConnected() ? Icons.CONNECTION_ACTIVE : 
               connectionStatus.isValid() ? Icons.CONNECTION_INACTIVE :
                        Icons.CONNECTION_INVALID;
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return this;
    }

   /*********************************************************
    *                      Disposable                       *
    *********************************************************/
    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            connectionPool = null;
            connectionBundle = null;
            changesBundle = null;
            loadMonitor = null;
            sessionBrowserFile = null;
            psiCache = null;
        }
    }

    public boolean isDisposed() {
        return isDisposed || connectionBundle == null || connectionBundle.isDisposed();
    }

    public void setConnectionConfig(final ConnectionSettings connectionSettings) {
        boolean refresh = this.connectionSettings.getDatabaseSettings().hashCode() != connectionSettings.getDatabaseSettings().hashCode();
        this.connectionSettings = connectionSettings;
        if (refresh) {
            connectionPool.closeConnectionsSilently();

            final Project project = getProject();
            new BackgroundTask(getProject(), "Trying to connect to " + getName(), false) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                    connectionManager.testConnection(ConnectionHandlerImpl.this, false, false);
                    //fixme check if the connection is pointing to a new database and reload if this is the case
                    //objectBundle.checkForDatabaseChange();

                    EventUtil.notify(project, BrowserTreeChangeListener.TOPIC).nodeChanged(getObjectBundle(), TreeEventType.NODES_CHANGED);
                }
            }.start();
        }
    }
}

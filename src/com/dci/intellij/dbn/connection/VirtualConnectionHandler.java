package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleBundle;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBVirtualObjectBundle;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class VirtualConnectionHandler implements ConnectionHandler {
    public static final ConnectionStatus CONNECTION_STATUS = new ConnectionStatus();
    private String id;
    private String name;
    private DatabaseType databaseType;
    private double databaseVersion;
    private Project project;
    private DatabaseInterfaceProvider interfaceProvider;
    private Map<String, String> properties = new HashMap<String, String>();
    private NavigationPsiCache psiCache;
    private ConnectionHandlerRef ref;
    private DBObjectBundle objectBundle;
    private ConnectionInstructions instructions = new ConnectionInstructions();

    public VirtualConnectionHandler(String id, String name, DatabaseType databaseType, double databaseVersion, Project project){
        this.id = id;
        this.name = name;
        this.project = project;
        this.databaseType = databaseType;
        this.databaseVersion = databaseVersion;
        this.ref = new ConnectionHandlerRef(this);
        this.objectBundle = new DBVirtualObjectBundle(this);
    }

    public static ConnectionHandler getDefault(Project project) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        return connectionManager.getConnectionBundle().getVirtualConnection("virtual-oracle-connection");

    }

    @Override
    public ConnectionInstructions getInstructions() {
        return instructions;
    }

    public DatabaseType getDatabaseType() {return databaseType;}

    @Override
    public double getDatabaseVersion() {
        return databaseVersion;
    }

    public Filter<BrowserTreeNode> getObjectTypeFilter() {
        return null;
    }

    @Override
    public NavigationPsiCache getPsiCache() {
        if (psiCache == null) {
            psiCache = new NavigationPsiCache(this);
        }
        return psiCache;
    }

    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        return EnvironmentType.DEFAULT;
    }

    @Override
    @Nullable
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

    @NotNull
    public Project getProject() {return project;}

    public boolean isActive() {
        return true;
    }

    @Override public String getId() {return id;}
    @NotNull
    @Override public String getName() {return name;}
    @Override public String getPresentableText() {
        return name;
    }
    @Override public String getQualifiedName() {
        return name;
    }
    @Override public String getDescription() {return "Virtual database connection"; }
    @Override public Icon getIcon() { return Icons.CONNECTION_VIRTUAL; }
    @Override public boolean isVirtual() {return true;}
    @Override public boolean isAutoCommit() {return false;}

    @Override public boolean isLoggingEnabled() {return false;}
    @Override public void setAutoCommit(boolean autoCommit) throws SQLException {}
    @Override public void setLoggingEnabled(boolean loggingEnabled) {}

    @Override public UncommittedChangeBundle getDataChanges() {return null;}
    @Override public boolean isConnected() {return false;}
    @Override public boolean isDisposed() {
        return false;
    }

    public Map<String, String> getProperties() {return properties;}

    public DatabaseInterfaceProvider getInterfaceProvider() {
        if (interfaceProvider == null) {
            try {
                interfaceProvider = DatabaseInterfaceProviderFactory.getInterfaceProvider(this);
            } catch (SQLException e) {
                // do not initialize
                return DatabaseInterfaceProviderFactory.GENERIC_INTERFACE_PROVIDER;
            }
        }
        return interfaceProvider;
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return this;
    }

    @Override public String getUserName() {return "root";}

    @Override public DBNConnection getTestConnection() throws SQLException {return null;}
    @Override public DBNConnection getPoolConnection(boolean readonly) throws SQLException {return null;}
    @Override public DBNConnection getPoolConnection(@Nullable DBSchema schema, boolean readonly) throws SQLException {return null;}
    @Override public DBNConnection getMainConnection() throws SQLException {return null;}
    @Override public DBNConnection getMainConnection(@Nullable DBSchema schema) throws SQLException {return null;}
    @Override public void freePoolConnection(DBNConnection connection) {}
    @Override public void dropPoolConnection(DBNConnection connection) {}

    @Override public ConnectionSettings getSettings() {return null;}
    @Override public void setSettings(ConnectionSettings connectionSettings) {}
    @NotNull
    @Override public ConnectionStatus getConnectionStatus() {return CONNECTION_STATUS;}

    @Override public void setTemporaryAuthenticationInfo(AuthenticationInfo temporaryAuthenticationInfo) {}

    @Nullable
    @Override public ConnectionInfo getConnectionInfo() { return null;}

    @Override public void setConnectionInfo(ConnectionInfo connectionInfo) {}
    @Override public boolean canConnect() {
        return false;
    }

    @Override public boolean hasPendingTransactions(@NotNull DBNConnection connection) {return false;}

    @NotNull
    @Override
    public AuthenticationInfo getTemporaryAuthenticationInfo() {
        return new AuthenticationInfo();
    }

    @Override
    public boolean isAuthenticationProvided() {
        return true;
    }

    @Override
    public boolean isDatabaseInitialized() {return true;}

    @NotNull
    public ConnectionBundle getConnectionBundle() {return null;}
    @NotNull
    public ConnectionPool getConnectionPool() {return null;}

    @Override
    public ConnectionLoadMonitor getLoadMonitor() {
        return null;
    }

    @NotNull
    public DBObjectBundle getObjectBundle() {return objectBundle;}
    public DBSchema getUserSchema() {return null;}

    @Override
    public DBSchema getDefaultSchema() {
        return null;
    }

    @Override
    public DBSessionBrowserVirtualFile getSessionBrowserFile() {
        return null;
    }

    @Override
    public DatabaseConsoleBundle getConsoleBundle() {return null;}
    public boolean isValid(boolean check) {return true;}
    public boolean isValid() {return true;}
    public void disconnect() {}
    public void ping(boolean check) {}
    public int getIdleMinutes() {return 0;}

    @Override
    public ConnectionHandlerRef getRef() {
        return ref;
    }

    @Override
    public DatabaseInfo getDatabaseInfo() {
        return databaseType.getUrlPatterns()[0].getDefaultInfo();
    }

    @Override
    public Set<TransactionAction> getPendingActions() {
        return Collections.emptySet();
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo() {
        return null;
    }

    public ConnectionHandler clone() {return null;}
    public void notifyDataChanges(VirtualFile virtualFile) {}
    public void resetDataChanges() {}
    public boolean hasUncommittedChanges() {return false;}
    public void commit() throws SQLException {}
    public void rollback() throws SQLException {}
    public void dispose() {}

}

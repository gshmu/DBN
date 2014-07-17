package com.dci.intellij.dbn.connection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class VirtualConnectionHandler implements ConnectionHandler {
    private String id;
    private String name;
    private DatabaseType databaseType;
    private Project project;
    private DatabaseInterfaceProvider interfaceProvider;
    private Map<String, String> properties = new HashMap<String, String>();
    private NavigationPsiCache psiCache = new NavigationPsiCache(this);

    public VirtualConnectionHandler(String id, String name, DatabaseType databaseType, Project project){
        this.id = id;
        this.name = name;
        this.project = project;
        this.databaseType = databaseType;
    }

    public DatabaseType getDatabaseType() {return databaseType;}

    public Filter<BrowserTreeNode> getObjectFilter() {
        return null;
    }

    @Override
    public NavigationPsiCache getPsiCache() {
        return psiCache;
    }

    @Override
    public EnvironmentType getEnvironmentType() {
        return null;
    }

    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        return getInterfaceProvider().getLanguageDialect(language);
    }

    public Project getProject() {return project;}

    public boolean isActive() {
        return true;
    }

    public String getId() {return id;}
    public String getName() {return name;}
    public String getPresentableText() {return getName();}
    public String getQualifiedName() {return getName();}
    public String getDescription() {return "Virtual database connection"; }
    public Icon getIcon() { return Icons.CONNECTION_VIRTUAL; }
    public boolean isVirtual() {return true;}
    public boolean isAutoCommit() {return false;}
    public void setAutoCommit(boolean autoCommit) throws SQLException {}
    public UncommittedChangeBundle getUncommittedChanges() {return null;}
    public boolean isConnected() {return false;}

    @Override
    public boolean isDisposed() {
        return false;
    }

    public Map<String, String> getProperties() {return properties;}

    public DatabaseInterfaceProvider getInterfaceProvider() {
        if (interfaceProvider == null) {
            try {
                interfaceProvider = DatabaseInterfaceProviderFactory.createInterfaceProvider(this);
            } catch (SQLException e) {
                // do not initialize
                return DatabaseInterfaceProviderFactory.GENERIC_INTERFACE_PROVIDER;
            }
        }
        return interfaceProvider;
    }

    public String getUser() {return "root";}
    public String getUserName() {return "root";}

    public Module getModule() {return null;}
    public Connection getPoolConnection() throws SQLException {return null;}
    public Connection getPoolConnection(DBSchema schema) throws SQLException {return null;}
    public Connection getStandaloneConnection() throws SQLException {return null;}
    public Connection getStandaloneConnection(DBSchema schema) throws SQLException {return null;}
    public void freePoolConnection(Connection connection) {}

    public ConnectionSettings getSettings() {return null;}
    public ConnectionStatus getConnectionStatus() {return null;}
    public ConnectionBundle getConnectionBundle() {return null;}
    public ConnectionInfo getConnectionInfo() throws SQLException {return null;}
    public ConnectionPool getConnectionPool() {return null;}

    @Override
    public ConnectionLoadMonitor getLoadMonitor() {
        return null;
    }

    public DBObjectBundle getObjectBundle() {return null;}
    public DBSchema getUserSchema() {return null;}
    public SQLConsoleFile getSQLConsoleFile() {return null;}

    public boolean isValid(boolean check) {return true;}
    public boolean isValid() {return true;}
    public void disconnect() {}
    public void ping(boolean check) {}
    public int getIdleMinutes() {return 0;}

    public ConnectionHandler clone() {return null;}
    public void notifyChanges(VirtualFile virtualFile) {}
    public void resetChanges() {}
    public boolean hasUncommittedChanges() {return false;}
    public void commit() throws SQLException {}
    public void rollback() throws SQLException {}
    public void dispose() {}
}

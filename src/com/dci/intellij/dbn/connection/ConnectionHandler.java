package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionHandler extends Disposable{
    Project getProject();
    Module getModule();
    Connection getPoolConnection() throws SQLException;
    Connection getPoolConnection(DBSchema schema) throws SQLException;
    Connection getStandaloneConnection() throws SQLException;
    Connection getStandaloneConnection(DBSchema schema) throws SQLException;
    void freePoolConnection(Connection connection);
    ConnectionSettings getSettings();
    ConnectionStatus getConnectionStatus();
    ConnectionBundle getConnectionBundle();
    ConnectionInfo getConnectionInfo() throws SQLException;
    ConnectionPool getConnectionPool();
    ConnectionLoadMonitor getLoadMonitor();
    DatabaseInterfaceProvider getInterfaceProvider();
    DBObjectBundle getObjectBundle();
    DBSchema getUserSchema();
    SQLConsoleFile getSQLConsoleFile();

    boolean isValid(boolean check);
    boolean isValid();
    boolean isVirtual();
    boolean isAutoCommit();
    void setAutoCommit(boolean autoCommit) throws SQLException;
    void disconnect() throws SQLException;

    String getId();
    String getUserName();
    String getPresentableText();
    String getQualifiedName();
    String getName();
    String getDescription();
    Icon getIcon();

    void notifyChanges(VirtualFile virtualFile);
    void resetChanges();
    boolean hasUncommittedChanges();
    void commit() throws SQLException;
    void rollback() throws SQLException;
    void ping(boolean check);

    DBLanguageDialect getLanguageDialect(DBLanguage language);
    boolean isActive();

    DatabaseType getDatabaseType();

    Filter<BrowserTreeNode> getObjectFilter();
    NavigationPsiCache getPsiCache();

    EnvironmentType getEnvironmentType();
    UncommittedChangeBundle getUncommittedChanges();
    boolean isConnected();
    boolean isDisposed();
    int getIdleMinutes();
}

package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public interface ConnectionHandler extends Disposable{
    SQLException DBN_NOT_CONNECTED_EXCEPTION = new SQLException("DBN_NOT_CONNECTED_EXCEPTION");

    Project getProject();
    Module getModule();
    Connection getPoolConnection() throws SQLException;
    Connection getPoolConnection(DBSchema schema) throws SQLException;
    Connection getStandaloneConnection() throws SQLException;
    Connection getStandaloneConnection(DBSchema schema) throws SQLException;
    void freePoolConnection(Connection connection);
    ConnectionSettings getSettings();
    ConnectionStatus getConnectionStatus();

    boolean isAllowConnection();
    void setAllowConnection(boolean allowConnection);

    boolean canConnect();

    ConnectionBundle getConnectionBundle();
    ConnectionPool getConnectionPool();
    ConnectionLoadMonitor getLoadMonitor();
    DatabaseInterfaceProvider getInterfaceProvider();
    DBObjectBundle getObjectBundle();
    DBSchema getUserSchema();

    List<DBConsoleVirtualFile> getConsoles();
    Set<String> getConsoleNames();

    @NotNull
    DBConsoleVirtualFile getDefaultConsole();

    @Nullable
    DBConsoleVirtualFile getConsole(String name, boolean create);
    void removeConsole(String name);

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

    @Nullable
    DBLanguageDialect resolveLanguageDialect(Language language);
    DBLanguageDialect getLanguageDialect(DBLanguage language);
    boolean isActive();

    DatabaseType getDatabaseType();
    double getDatabaseVersion();

    Filter<BrowserTreeNode> getObjectTypeFilter();
    NavigationPsiCache getPsiCache();

    EnvironmentType getEnvironmentType();
    UncommittedChangeBundle getUncommittedChanges();
    boolean isConnected();
    boolean isDisposed();
    int getIdleMinutes();

    ConnectionHandlerRef getRef();
}

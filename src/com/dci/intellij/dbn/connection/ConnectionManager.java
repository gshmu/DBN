package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.option.InteractiveOptionBroker;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.common.util.InternalApi;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListener;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.info.ui.ConnectionInfoDialog;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.dci.intellij.dbn.connection.transaction.ui.IdleConnectionDialog;
import com.dci.intellij.dbn.connection.ui.ConnectionAuthenticationDialog;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import static com.dci.intellij.dbn.common.message.MessageCallback.when;
import static com.dci.intellij.dbn.common.util.Commons.list;
import static com.dci.intellij.dbn.common.util.Lists.isLast;
import static com.dci.intellij.dbn.common.util.Messages.*;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.actions;

@State(
    name = ConnectionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Slf4j
public class ConnectionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    public static final String COMPONENT_NAME = "DBNavigator.Project.ConnectionManager";

    private Timer idleConnectionCleaner;
    private final ConnectionBundle connectionBundle;
    private static ConnectionHandlerRef lastUsedConnection;

    public static ConnectionManager getInstance(@NotNull Project project) {
        return getComponent(project);
    }

    private static ConnectionManager getComponent(@NotNull Project project) {
        return Failsafe.getComponent(project, ConnectionManager.class);
    }

    private ConnectionManager(@NotNull Project project) {
        super(project);
        connectionBundle = new ConnectionBundle(project);

        ProjectEvents.subscribe(project, this,
                ConnectionConfigListener.TOPIC,
                ConnectionConfigListener.whenChanged(id -> refreshObjects(id)));

        Disposer.register(this, connectionBundle);
    }

    @Override
    public void initializeComponent() {
        super.initComponent();
        idleConnectionCleaner = new Timer("DBN - Idle Connection Cleaner");
        idleConnectionCleaner.schedule(new CloseIdleConnectionTask(), TimeUtil.Millis.ONE_MINUTE, TimeUtil.Millis.ONE_MINUTE);
    }

    @Override
    public void disposeInner() {
        SafeDisposer.dispose(idleConnectionCleaner);
        super.disposeInner();
    }

    @Nullable
    public static ConnectionHandler getLastUsedConnection() {
        return lastUsedConnection == null ? null : lastUsedConnection.get();
    }

    @Nullable
    public static ConnectionInfo getLastUsedConnectionInfo() {
        ConnectionHandler lastUsedConnection = getLastUsedConnection();
        return lastUsedConnection == null ? null : lastUsedConnection.getConnectionInfo();
    }

    static void setLastUsedConnection(@NotNull ConnectionHandler lastUsedConnection) {
        ConnectionManager.lastUsedConnection = lastUsedConnection.ref();
    }

    private void refreshObjects(ConnectionId connectionId) {
        ConnectionHandler connection = getConnection(connectionId);
        if (connection != null) {
            Project project = getProject();
            Progress.background(project, "Refreshing database objects", true, progress -> {
                connection.resetCompatibilityMonitor();
                List<TransactionAction> actions = actions(TransactionAction.DISCONNECT);

                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
                List<DBNConnection> connections = connection.getConnections();
                for (DBNConnection conn : connections) {
                    Progress.check(progress);
                    transactionManager.execute(connection, conn, actions, false, null);
                }
                connection.getObjectBundle().getObjectLists().refreshObjects();

            });
        }
    }

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public ConnectionBundle getConnectionBundle() {
        return connectionBundle;
    }

    public static void testConnection(ConnectionHandler connection, SchemaId schemaId, SessionId sessionId, boolean showSuccessMessage, boolean showErrorMessage) {
        Project project = connection.getProject();
        Progress.prompt(project, "Trying to connect to " + connection.getName(), true, progress -> {
            ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
            String connectionName = connection.getName();
            try {
                databaseSettings.validate();
                connection.getConnection(sessionId, schemaId);
                ConnectionHandlerStatusHolder connectionStatus = connection.getConnectionStatus();
                connectionStatus.setValid(true);
                connectionStatus.setConnected(true);
                if (showSuccessMessage) {
                    showSuccessfulConnectionMessage(project, connectionName);
                }
            } catch (ConfigurationException e) {
                if (showErrorMessage) {
                    showInvalidConfigMessage(project, e);

                }
            } catch (Exception e) {
                if (showErrorMessage) {
                    showErrorConnectionMessage(project, connectionName, e);
                }
            }
        });

    }

    public void testConfigConnection(ConnectionSettings connectionSettings, boolean showMessageDialog) {
        Project project = connectionSettings.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        try {
            databaseSettings.validate();

            if (databaseSettings.isDatabaseInitialized()) {
                ensureAuthenticationProvided(databaseSettings, (authenticationInfo) ->
                        attemptConfigConnection(
                                connectionSettings,
                                authenticationInfo,
                                showMessageDialog));
            } else {
                promptDatabaseInitDialog(databaseSettings,
                        option -> when(option == 0, () ->
                                ensureAuthenticationProvided(databaseSettings,
                                        authInfo -> attemptConfigConnection(
                                                connectionSettings,
                                                authInfo,
                                                showMessageDialog))));
            }




        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    private static void attemptConfigConnection(ConnectionSettings connectionSettings, AuthenticationInfo authentication, boolean showMessageDialog) {
        Project project = connectionSettings.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();

        Progress.modal(project, "Connecting to " + connectionName, false, progress -> {
            try {
                DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authentication, SessionId.TEST, false, null);
                Resources.close(connection);
                databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
                if (showMessageDialog) {
                    showSuccessfulConnectionMessage(project, connectionName);
                }
            } catch (Exception e) {
                databaseSettings.setConnectivityStatus(ConnectivityStatus.INVALID);
                if (showMessageDialog) {
                    showErrorConnectionMessage(project, connectionName, e);
                }
            }
        });
    }

    public void showConnectionInfo(ConnectionSettings connectionSettings, EnvironmentType environmentType) {
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();
        Project project = connectionSettings.getProject();

        try {
            databaseSettings.validate();
            ensureAuthenticationProvided(databaseSettings, (authenticationInfo) ->
                    Progress.modal(project, "Connecting to " + connectionName, false, (progress) -> {
                        try {
                            DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authenticationInfo, SessionId.TEST, false, null);
                            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
                            Resources.close(connection);
                            showConnectionInfoDialog(connectionInfo, connectionName, environmentType);
                        } catch (Exception e) {
                            showErrorConnectionMessage(project, connectionName, e);
                        }
                    }));

        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    private void ensureAuthenticationProvided(
            @NotNull ConnectionDatabaseSettings databaseSettings,
            @NotNull ParametricRunnable.Basic<AuthenticationInfo> callback) {

        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo().clone();
        if (!authenticationInfo.isProvided()) {
            promptAuthenticationDialog(null, authenticationInfo, callback);
        } else {
            callback.run(authenticationInfo);
        }
    }

    static void promptDatabaseInitDialog(ConnectionHandler connection, MessageCallback callback) {
        ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
        promptDatabaseInitDialog(databaseSettings, callback);
    }

    private static void promptDatabaseInitDialog(ConnectionDatabaseSettings databaseSettings, MessageCallback callback) {
        DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
        if (databaseInfo.getUrlType() == DatabaseUrlType.FILE) {
            String file = databaseInfo.getFiles().getMainFile().getPath();
            Project project = databaseSettings.getProject();
            if (Strings.isEmpty(file)) {
                showErrorDialog(project, "Wrong database configuration", "Database file not specified");
            } else if (!new File(file).exists()) {
                showWarningDialog(
                        project,
                        "Database file not available",
                        "The database file \"" + file + "\" does not exist.\nDo you want to create it?",
                        options("Create", "Cancel"), 0,
                        callback);
            }
        }
    }

    static void promptConnectDialog(ConnectionHandler connection, @Nullable String actionDesc, MessageCallback callback) {
        showInfoDialog(
                connection.getProject(),
                "Not connected to database",
                "You are not connected to database \"" + connection.getName() + "\". \n" +
                        "If you want to continue" + (actionDesc == null ? "" : " with " + actionDesc) + ", you need to connect.",
                ConnectionAction.OPTIONS_CONNECT_CANCEL, 0,
                callback);
    }

    static void showErrorConnectionMessage(Project project, String connectionName, Throwable e) {
        showErrorDialog(
                project,
                "Connection error",
                "Cannot connect to \"" + connectionName + "\".\n" + (e == null || e.getMessage() == null ? "Unknown reason" : e.getMessage()));
    }

    private static void showSuccessfulConnectionMessage(Project project, String connectionName) {
        showInfoDialog(
                project,
                "Connection successful",
                "Connection to \"" + connectionName + "\" was successful.");
    }

    private static void showInvalidConfigMessage(Project project, ConfigurationException e) {
        showErrorDialog(
                project,
                "Invalid configuration",
                e.getMessage());
    }

    public static void showConnectionInfoDialog(ConnectionHandler connection) {
        Dispatch.run(() -> {
            ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(connection);
            infoDialog.setModal(true);
            infoDialog.show();
        });
    }

    private static void showConnectionInfoDialog(ConnectionInfo connectionInfo, String connectionName, EnvironmentType environmentType) {
        Dispatch.run(() -> {
            ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(null, connectionInfo, connectionName, environmentType);
            infoDialog.setModal(true);
            infoDialog.show();
        });
    }

    void promptAuthenticationDialog(
            @Nullable ConnectionHandler connection,
            @NotNull AuthenticationInfo authenticationInfo,
            @NotNull ParametricRunnable.Basic<AuthenticationInfo> callback) {

        ConnectionAuthenticationDialog passwordDialog = new ConnectionAuthenticationDialog(getProject(), connection, authenticationInfo);
        passwordDialog.show();
        if (passwordDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            AuthenticationInfo newAuthenticationInfo = passwordDialog.getAuthenticationInfo();
            if (connection != null) {
                AuthenticationInfo storedAuthenticationInfo = connection.getAuthenticationInfo();

                if (passwordDialog.isRememberCredentials()) {
                    String oldUser = storedAuthenticationInfo.getUser();
                    String oldPassword = storedAuthenticationInfo.getPassword();

                    storedAuthenticationInfo.setUser(newAuthenticationInfo.getUser());
                    storedAuthenticationInfo.setPassword(newAuthenticationInfo.getPassword());
                    storedAuthenticationInfo.setType(newAuthenticationInfo.getType());

                    storedAuthenticationInfo.updateKeyChain(oldUser, oldPassword);
                } else {
                    AuthenticationInfo temporaryAuthenticationInfo = newAuthenticationInfo.clone();
                    temporaryAuthenticationInfo.setTemporary(true);
                    connection.setTemporaryAuthenticationInfo(temporaryAuthenticationInfo);
                }
                connection.getInstructions().setAllowAutoConnect(true);
            }
            callback.run(newAuthenticationInfo);
        }
    }

    /*********************************************************
     *                     Miscellaneous                     *
     *********************************************************/
    @Nullable
    public ConnectionHandler getConnection(ConnectionId connectionId) {
        return getConnectionBundle().getConnection(connectionId);
     }

     public List<ConnectionHandler> getConnections(Predicate<ConnectionHandler> predicate) {
        return Lists.filtered(getConnections(), predicate);
     }

     public List<ConnectionHandler> getConnections() {
         return getConnectionBundle().getConnections();
     }

     public ConnectionHandler getActiveConnection(Project project) {
         ConnectionHandler connection = null;
         VirtualFile virtualFile = Editors.getSelectedFile(project);
         if (DatabaseBrowserManager.getInstance(project).getBrowserToolWindow().isActive() || virtualFile == null) {
             connection = DatabaseBrowserManager.getInstance(project).getActiveConnection();
         }

         if (connection == null && virtualFile != null) {
             FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
             connection = contextManager.getConnection(virtualFile);
         }

         return connection;
     }

    public boolean hasUncommittedChanges() {
        for (ConnectionHandler connection : getConnections()) {
            if (connection.hasUncommittedChanges()) {
                return true;
            }
        }
        return false;
    }

    private void commitAll(@Nullable Runnable callback) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());

        List<ConnectionHandler> connections = getConnections(
                connection -> connection.hasUncommittedChanges());

        for (ConnectionHandler connection : connections) {
            Runnable commitCallback = isLast(connections, connection) ? callback : null;
            transactionManager.commit(connection, null, false, false, commitCallback);
        }
    }

    private void rollbackAll(@Nullable Runnable callback) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());

        List<ConnectionHandler> connections = getConnections(
                connection -> connection.hasUncommittedChanges());

        for (ConnectionHandler connection : connections) {
            Runnable rollbackCallback = isLast(connections, connection) ? callback : null;
            transactionManager.rollback(connection, null, false, false, rollbackCallback);
        }
    }

    public boolean isValidConnectionId(ConnectionId connectionId) {
        return getConnection(connectionId) != null;
    }

    private class CloseIdleConnectionTask extends TimerTask {
        @Override
        public void run() {
            try {
                List<ConnectionHandler> connections = getConnections();
                for (ConnectionHandler connection : connections) {
                    resolveIdleStatus(connection);
                }
            } catch (Exception e){
                log.error("Failed to release idle connections", e);
            }
        }

        private void resolveIdleStatus(ConnectionHandler connection) {
            try {
                List<TransactionAction> actions = actions(TransactionAction.DISCONNECT_IDLE);

                Failsafe.nd(connection);
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
                List<DBNConnection> connections = connection.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);

                for (DBNConnection conn : connections) {
                    if (conn.isIdle() && conn.isNot(ResourceStatus.RESOLVING_TRANSACTION)) {
                        int idleMinutes = conn.getIdleMinutes();
                        int idleMinutesToDisconnect = connection.getSettings().getDetailSettings().getIdleTimeToDisconnect();
                        if (idleMinutes > idleMinutesToDisconnect) {
                            if (conn.hasDataChanges()) {
                                conn.set(ResourceStatus.RESOLVING_TRANSACTION, true);
                                Dispatch.run(() -> {
                                    IdleConnectionDialog idleConnectionDialog = new IdleConnectionDialog(connection, conn);
                                    idleConnectionDialog.show();
                                });
                            } else {
                                transactionManager.execute(connection, conn, actions, false, null);
                            }
                        }
                    }
                }
            } catch (ProcessCanceledException ignore) {}
        }
    }

    void disposeConnections(@NotNull List<ConnectionHandler> connections) {
        if (connections.size() > 0) {
            Project project = getProject();
            Dispatch.run(() -> {
                List<ConnectionId> connectionIds = ConnectionHandler.ids(connections);

                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                executionManager.closeExecutionResults(connectionIds);

                DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                databaseFileManager.closeDatabaseFiles(connectionIds);

                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                methodExecutionManager.cleanupExecutionHistory(connectionIds);

                Background.run(() -> {
                    for (ConnectionHandler connection : connections) {
                        connection.getConnectionPool().closeConnections();
                        Disposer.dispose(connection);
                    }
                });
            });
        }
    }

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    @Override
    public boolean canCloseProject() {
        if (hasUncommittedChanges()) {
            boolean exitApp = InternalApi.isAppExitInProgress();
            Project project = getProject();
            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
            TransactionManagerSettings transactionManagerSettings = transactionManager.getSettings();
            InteractiveOptionBroker<TransactionOption> closeProjectOptionHandler = transactionManagerSettings.getCloseProject();

            closeProjectOptionHandler.resolve(
                    list(project.getName()),
                    option -> {
                        switch (option) {
                            case COMMIT: {
                                commitAll(() -> closeProject(exitApp));
                                break;
                            }
                            case ROLLBACK: {
                                rollbackAll(() -> closeProject(exitApp));
                                break;
                            }
                            case REVIEW_CHANGES: {
                                transactionManager.showPendingTransactionsOverviewDialog(null);
                                break;
                            }
                        }
                    });

            return false;
        }
        return true;
    }

    /**********************************************
    *                ProjectComponent             *
    ***********************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /*********************************************************
     *                PersistentStateComponent               *
     *********************************************************/
    @Override
    @Nullable
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {}
}
package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.option.InteractiveOptionBroker;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsAdapter;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.info.ui.ConnectionInfoDialog;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;
import static com.dci.intellij.dbn.common.util.CollectionUtil.isLast;
import static com.dci.intellij.dbn.common.util.CommonUtil.list;
import static com.dci.intellij.dbn.common.util.MessageUtil.*;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.actions;

@State(
    name = ConnectionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ConnectionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static final String COMPONENT_NAME = "DBNavigator.Project.ConnectionManager";

    private Timer idleConnectionCleaner;
    private ConnectionBundle connectionBundle;
    private static ConnectionHandlerRef lastUsedConnection;

    public static ConnectionManager getInstance(@NotNull Project project) {
        return getComponent(project);
    }

    private static ConnectionManager getComponent(@NotNull Project project) {
        return Failsafe.getComponent(project, ConnectionManager.class);
    }

    private ConnectionManager(Project project) {
        super(project);
        connectionBundle = new ConnectionBundle(project);
        Disposer.register(this, connectionBundle);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        Project project = getProject();
        EventUtil.subscribe(project, this, ConnectionSettingsListener.TOPIC, connectionSettingsListener);
        idleConnectionCleaner = new Timer("DBN - Idle Connection Cleaner [" + project.getName() + "]");
        idleConnectionCleaner.schedule(new CloseIdleConnectionTask(), TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }

    @Override
    public void disposeInner() {
        if (idleConnectionCleaner != null) {
            idleConnectionCleaner.cancel();
            idleConnectionCleaner.purge();
        }
        ConnectionBundle connectionBundle = getConnectionBundle();
        Disposer.dispose(connectionBundle);
        super.disposeInner();
    }

    @Nullable
    private static ConnectionHandler getLastUsedConnection() {
        return ConnectionHandlerRef.get(lastUsedConnection);
    }

    @Nullable
    public static ConnectionInfo getLastUsedConnectionInfo() {
        ConnectionHandler lastUsedConnection = getLastUsedConnection();
        return lastUsedConnection == null ? null : lastUsedConnection.getConnectionInfo();
    }

    static void setLastUsedConnection(@NotNull ConnectionHandler lastUsedConnection) {
        ConnectionManager.lastUsedConnection = lastUsedConnection.getRef();
    }

    /*********************************************************
    *                       Listeners                        *
    *********************************************************/

    private ConnectionSettingsListener connectionSettingsListener = new ConnectionSettingsAdapter() {
        @Override
        public void connectionChanged(ConnectionId connectionId) {
            ConnectionHandler connectionHandler = getConnectionHandler(connectionId);
            if (connectionHandler != null) {
                Project project = getProject();
                Progress.background(
                        project,
                        "Refreshing database objects", true,
                        (progress) -> {
                            List<TransactionAction> actions = actions(TransactionAction.DISCONNECT);

                            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
                            List<DBNConnection> connections = connectionHandler.getConnections();
                            for (DBNConnection connection : connections) {
                                Progress.check(progress);
                                transactionManager.execute(connectionHandler, connection, actions, false, null);
                            }
                            connectionHandler.getObjectBundle().getObjectListContainer().refresh();
                        });
            }
        }
    };

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public ConnectionBundle getConnectionBundle() {
        return connectionBundle;
    }

    public static void testConnection(ConnectionHandler connectionHandler, SchemaId schemaId, SessionId sessionId, boolean showSuccessMessage, boolean showErrorMessage) {
        Project project = connectionHandler.getProject();
        Progress.prompt(project, "Trying to connect to " + connectionHandler.getName(), true,
                (progress) -> {
                    ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
                    String connectionName = connectionHandler.getName();
                    try {
                        databaseSettings.checkConfiguration();
                        connectionHandler.getConnection(sessionId, schemaId);
                        ConnectionHandlerStatusHolder connectionStatus = connectionHandler.getConnectionStatus();
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

    public static void testConfigConnection(ConnectionSettings connectionSettings, boolean showMessageDialog) {
        Project project = connectionSettings.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        try {
            databaseSettings.checkConfiguration();

            if (databaseSettings.isDatabaseInitialized()) {
                ensureAuthenticationProvided(databaseSettings, (authenticationInfo) ->
                        attemptConfigConnection(
                                connectionSettings,
                                authenticationInfo,
                                showMessageDialog));
            } else {
                promptDatabaseInitDialog(databaseSettings,
                        (option) -> conditional(option == 0,
                                () -> ensureAuthenticationProvided(databaseSettings,
                                        (authenticationInfo) -> attemptConfigConnection(
                                                connectionSettings,
                                                authenticationInfo,
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

        Progress.modal(project, "Connecting to " + connectionName, false,
                (progress) -> {
                    try {
                        DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authentication, SessionId.TEST, false, null);
                        ConnectionUtil.close(connection);
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

    public static void showConnectionInfo(ConnectionSettings connectionSettings, EnvironmentType environmentType) {
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();
        Project project = connectionSettings.getProject();

        try {
            databaseSettings.checkConfiguration();
            ensureAuthenticationProvided(databaseSettings, (authenticationInfo) ->
                    Progress.modal(project, "Connecting to " + connectionName, false, (progress) -> {
                        try {
                            DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authenticationInfo, SessionId.TEST, false, null);
                            if (connection != null) {
                                ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
                                ConnectionUtil.close(connection);
                                showConnectionInfoDialog(connectionInfo, connectionName, environmentType);
                            } // TODO else??
                        } catch (Exception e) {
                            showErrorConnectionMessage(project, connectionName, e);
                        }
                    }));

        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    private static void ensureAuthenticationProvided(
            @NotNull ConnectionDatabaseSettings databaseSettings,
            @NotNull ParametricRunnable<AuthenticationInfo> callback) {

        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo().clone();
        if (!authenticationInfo.isProvided()) {
            promptAuthenticationDialog(null, authenticationInfo, callback);
        } else {
            callback.run(authenticationInfo);
        }
    }

    static void promptDatabaseInitDialog(ConnectionHandler connectionHandler, MessageCallback callback) {
        ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
        promptDatabaseInitDialog(databaseSettings, callback);
    }

    private static void promptDatabaseInitDialog(ConnectionDatabaseSettings databaseSettings, MessageCallback callback) {
        DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
        if (databaseInfo.getUrlType() == DatabaseUrlType.FILE) {
            String file = databaseInfo.getFiles().getMainFile().getPath();
            Project project = databaseSettings.getProject();
            if (StringUtils.isEmpty(file)) {
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

    static void promptConnectDialog(ConnectionHandler connectionHandler, @Nullable String actionDesc, MessageCallback callback) {
        showInfoDialog(
                connectionHandler.getProject(),
                "Not connected to database",
                "You are not connected to database \"" + connectionHandler.getName() + "\". \n" +
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

    public static void showConnectionInfoDialog(ConnectionHandler connectionHandler) {
        Dispatch.invokeNonModal(() -> {
            ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(connectionHandler);
            infoDialog.setModal(true);
            infoDialog.show();
        });
    }

    private static void showConnectionInfoDialog(ConnectionInfo connectionInfo, String connectionName, EnvironmentType environmentType) {
        Dispatch.invokeNonModal(() -> {
            ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(null, connectionInfo, connectionName, environmentType);
            infoDialog.setModal(true);
            infoDialog.show();
        });
    }

    static void promptAuthenticationDialog(
            @Nullable ConnectionHandler connectionHandler,
            @NotNull AuthenticationInfo authenticationInfo,
            @NotNull ParametricRunnable<AuthenticationInfo> callback) {

        ConnectionAuthenticationDialog passwordDialog = new ConnectionAuthenticationDialog(null, connectionHandler, authenticationInfo);
        passwordDialog.show();
        if (passwordDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            AuthenticationInfo newAuthenticationInfo = passwordDialog.getAuthenticationInfo();
            if (connectionHandler != null) {
                AuthenticationInfo storedAuthenticationInfo = connectionHandler.getAuthenticationInfo();

                if (passwordDialog.isRememberCredentials()) {
                    String oldUser = storedAuthenticationInfo.getUser();
                    String oldPassword = storedAuthenticationInfo.getPassword();

                    storedAuthenticationInfo.setUser(newAuthenticationInfo.getUser());
                    storedAuthenticationInfo.setPassword(newAuthenticationInfo.getPassword());
                    storedAuthenticationInfo.setOsAuthentication(newAuthenticationInfo.isOsAuthentication());
                    storedAuthenticationInfo.setEmptyAuthentication(newAuthenticationInfo.isEmptyAuthentication());

                    storedAuthenticationInfo.updateKeyChain(oldUser, oldPassword);
                } else {
                    AuthenticationInfo temporaryAuthenticationInfo = newAuthenticationInfo.clone();
                    temporaryAuthenticationInfo.setTemporary(true);
                    connectionHandler.setTemporaryAuthenticationInfo(temporaryAuthenticationInfo);
                }
                connectionHandler.getInstructions().setAllowAutoConnect(true);
            }
            callback.run(newAuthenticationInfo);
        }
    }

    /*********************************************************
     *                     Miscellaneous                     *
     *********************************************************/
    @Nullable
    public ConnectionHandler getConnectionHandler(ConnectionId connectionId) {
         for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers().getFullList()) {
            if (connectionHandler.getConnectionId() == connectionId) {
                return connectionHandler;
            }
         }
         return null;
     }

     public List<ConnectionHandler> getConnectionHandlers(Predicate<ConnectionHandler> predicate) {
        return getConnectionHandlers().stream().filter(predicate).collect(Collectors.toList());
     }

     public List<ConnectionHandler> getConnectionHandlers() {
         return getConnectionBundle().getConnectionHandlers();
     }

     public ConnectionHandler getActiveConnection(Project project) {
         ConnectionHandler connectionHandler = null;
         VirtualFile virtualFile = EditorUtil.getSelectedFile(project);
         if (DatabaseBrowserManager.getInstance(project).getBrowserToolWindow().isActive() || virtualFile == null) {
             connectionHandler = DatabaseBrowserManager.getInstance(project).getActiveConnection();
         }

         if (connectionHandler == null && virtualFile != null) {
             connectionHandler = FileConnectionMappingManager.getInstance(project).getConnectionHandler(virtualFile);
         }

         return connectionHandler;
     }

    public boolean hasUncommittedChanges() {
        for (ConnectionHandler connectionHandler : getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                return true;
            }
        }
        return false;
    }

    private void commitAll(@Nullable Runnable callback) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());

        List<ConnectionHandler> connectionHandlers = getConnectionHandlers(
                connectionHandler -> connectionHandler.hasUncommittedChanges());

        for (ConnectionHandler connectionHandler : connectionHandlers) {
            Runnable commitCallback = isLast(connectionHandlers, connectionHandler) ? callback : null;
            transactionManager.commit(connectionHandler, null, false, false, commitCallback);
        }
    }

    private void rollbackAll(@Nullable Runnable callback) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());

        List<ConnectionHandler> connectionHandlers = getConnectionHandlers(
                connectionHandler -> connectionHandler.hasUncommittedChanges());

        for (ConnectionHandler connectionHandler : connectionHandlers) {
            Runnable rollbackCallback = isLast(connectionHandlers, connectionHandler) ? callback : null;
            transactionManager.rollback(connectionHandler, null, false, false, rollbackCallback);
        }
    }

    public boolean isValidConnectionId(ConnectionId connectionId) {
        return getConnectionHandler(connectionId) != null;
    }

    private class CloseIdleConnectionTask extends TimerTask {
        @Override
        public void run() {
            try {
                List<ConnectionHandler> connectionHandlers = getConnectionHandlers();
                connectionHandlers.forEach(connectionHandler -> resolveIdleStatus(connectionHandler));
            } catch (Exception e){
                LOGGER.error("Failed to release idle connections", e);
            }
        }

        private void resolveIdleStatus(ConnectionHandler connectionHandler) {
            Failsafe.guarded(() -> {
                List<TransactionAction> actions = actions(TransactionAction.DISCONNECT_IDLE);

                Failsafe.ensure(connectionHandler);
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
                List<DBNConnection> activeConnections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);

                activeConnections.
                        stream().
                        filter(connection -> connection.isIdle() && connection.isNot(ResourceStatus.RESOLVING_TRANSACTION)).
                        forEach(connection -> {
                            int idleMinutes = connection.getIdleMinutes();
                            int idleMinutesToDisconnect = connectionHandler.getSettings().getDetailSettings().getIdleTimeToDisconnect();
                            if (idleMinutes > idleMinutesToDisconnect) {
                                if (connection.hasDataChanges()) {
                                    connection.set(ResourceStatus.RESOLVING_TRANSACTION, true);
                                    Dispatch.invokeNonModal(() -> {
                                        IdleConnectionDialog idleConnectionDialog = new IdleConnectionDialog(connectionHandler, connection);
                                        idleConnectionDialog.show();
                                    });
                                } else {
                                    transactionManager.execute(connectionHandler, connection, actions, false, null);
                                }
                            }
                        });
            });
        }
    }

    void disposeConnections(@NotNull List<ConnectionHandler> connectionHandlers) {
        if (connectionHandlers.size() > 0) {
            Project project = getProject();
            Dispatch.invoke(() -> {
                List<ConnectionId> connectionIds = ConnectionHandler.ids(connectionHandlers);

                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                executionManager.closeExecutionResults(connectionIds);

                DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                databaseFileManager.closeDatabaseFiles(connectionIds);

                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                methodExecutionManager.cleanupExecutionHistory(connectionIds);

                Background.run(() -> {
                    connectionHandlers.forEach(connectionHandler -> {
                        connectionHandler.getConnectionPool().closeConnections();
                        DisposerUtil.dispose(connectionHandler);
                    });
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
            Project project = getProject();
            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
            TransactionManagerSettings transactionManagerSettings = transactionManager.getSettings();
            InteractiveOptionBroker<TransactionOption> closeProjectOptionHandler = transactionManagerSettings.getCloseProject();

            closeProjectOptionHandler.resolve(
                    list(project.getName()),
                    option -> {
                        switch (option) {
                            case COMMIT: {
                                commitAll(() -> closeProject());
                                break;
                            }
                            case ROLLBACK: {
                                rollbackAll(() -> closeProject());
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
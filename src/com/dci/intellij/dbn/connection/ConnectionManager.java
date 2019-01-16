package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ide.IdeMonitor;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.ModalTask;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@State(
    name = ConnectionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ConnectionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.ConnectionManager";

    private Timer idleConnectionCleaner;
    private ConnectionBundle connectionBundle;
    private static ConnectionHandlerRef lastUsedConnection;

    public static ConnectionManager getInstance(@NotNull Project project) {
        return getComponent(project);
    }

    private static ConnectionManager getComponent(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ConnectionManager.class);
    }

    private ConnectionManager(final Project project) {
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
    public void dispose() {
        ConnectionBundle connectionBundle = getConnectionBundle();
        super.dispose();
        if (idleConnectionCleaner != null) {
            idleConnectionCleaner.cancel();
            idleConnectionCleaner.purge();
        }
        Disposer.dispose(connectionBundle);
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
                BackgroundTask.invoke(project,
                        TaskInstructions.create("Refreshing database objects", TaskInstruction.BACKGROUNDED, TaskInstruction.CANCELLABLE),
                        (data, progress) -> {
                            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
                            List<DBNConnection> connections = connectionHandler.getConnections();
                            for (DBNConnection connection : connections) {
                                transactionManager.execute(connectionHandler, connection, false, TransactionAction.DISCONNECT);
                            }
                            connectionHandler.getObjectBundle().getObjectListContainer().reload();
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

    public static void testConnection(ConnectionHandler connectionHandler, boolean showSuccessMessage, boolean showErrorMessage) {
        Project project = connectionHandler.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
        String connectionName = connectionHandler.getName();
        try {
            databaseSettings.checkConfiguration();
            connectionHandler.getMainConnection();
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
    }

    public static void testConfigConnection(final ConnectionSettings connectionSettings, final boolean showMessageDialog) {
        Project project = connectionSettings.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();
        try {
            databaseSettings.checkConfiguration();

            ModalTask<AuthenticationInfo> connectCallback = ModalTask.create(project, "Connecting to " + connectionName, false, (authentication, progress) -> {
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

            if (databaseSettings.isDatabaseInitialized()) {
                promptTemporaryAuthenticationDialog(databaseSettings, connectCallback);
            } else {
                promptDatabaseInitDialog(databaseSettings,
                        MessageCallback.create(0, option ->
                                promptTemporaryAuthenticationDialog(databaseSettings, connectCallback)));
            }




        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    public static void showConnectionInfo(final ConnectionSettings connectionSettings, final EnvironmentType environmentType) {
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();
        Project project = connectionSettings.getProject();

        try {
            databaseSettings.checkConfiguration();
            promptTemporaryAuthenticationDialog(
                    databaseSettings,
                    ModalTask.create(project, "Connecting to " + connectionName, false, (authentication, progress) -> {
                        try {
                            DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authentication, SessionId.TEST, false, null);
                            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
                            ConnectionUtil.close(connection);
                            showConnectionInfoDialog(connectionInfo, connectionName, environmentType);
                        } catch (Exception e) {
                            showErrorConnectionMessage(project, connectionName, e);
                        }
                    }));

        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    private static void promptTemporaryAuthenticationDialog(ConnectionDatabaseSettings databaseSettings, RunnableTask<AuthenticationInfo> callback) {
        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo().clone();
        if (!authenticationInfo.isProvided()) {
            promptAuthenticationDialog(null, authenticationInfo, callback);
        } else {
            callback.start();
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
                MessageUtil.showErrorDialog(project, "Wrong database configuration", "Database file not specified");
            } else if (!new File(file).exists()) {
                MessageUtil.showWarningDialog(
                        project,
                        "Database file not available",
                        "The database file \"" + file + "\" does not exist.\nDo you want to create?",
                        new String[]{"Create", "Cancel"}, 0,
                        callback);
            }
        }
    }

    static void promptConnectDialog(ConnectionHandler connectionHandler, @Nullable String actionDesc, MessageCallback callback) {
        MessageUtil.showInfoDialog(
                connectionHandler.getProject(),
                "Not connected to database",
                "You are not connected to database \"" + connectionHandler.getName() + "\". \n" +
                        "If you want to continue" + (actionDesc == null ? "" : " with " + actionDesc) + ", you need to connect.",
                ConnectionAction.OPTIONS_CONNECT_CANCEL, 0,
                callback);
    }

    static void showErrorConnectionMessage(Project project, String connectionName, Throwable e) {
        MessageUtil.showErrorDialog(
                project,
                "Connection error",
                "Cannot connect to \"" + connectionName + "\".\n" + (e == null || e.getMessage() == null ? "Unknown reason" : e.getMessage()));
    }

    private static void showSuccessfulConnectionMessage(Project project, String connectionName) {
        MessageUtil.showInfoDialog(
                project,
                "Connection successful",
                "Connection to \"" + connectionName + "\" was successful.");
    }

    private static void showInvalidConfigMessage(Project project, ConfigurationException e) {
        MessageUtil.showErrorDialog(
                project,
                "Invalid configuration",
                e.getMessage());
    }

    public static void showConnectionInfoDialog(final ConnectionHandler connectionHandler) {
        ConditionalLaterInvocator.invoke(() -> {
            ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(connectionHandler);
            infoDialog.setModal(true);
            infoDialog.show();
        });
    }

    private static void showConnectionInfoDialog(final ConnectionInfo connectionInfo, final String connectionName, final EnvironmentType environmentType) {
        SimpleLaterInvocator.invoke(() -> {
            ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(null, connectionInfo, connectionName, environmentType);
            infoDialog.setModal(true);
            infoDialog.show();
        });
    }

    static void promptAuthenticationDialog(@Nullable ConnectionHandler connectionHandler, @NotNull AuthenticationInfo authenticationInfo, RunnableTask<AuthenticationInfo> callback) {
        ConnectionAuthenticationDialog passwordDialog = new ConnectionAuthenticationDialog(null, connectionHandler, authenticationInfo);
        passwordDialog.show();
        if (passwordDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            AuthenticationInfo newAuthenticationInfo = passwordDialog.getAuthenticationInfo();
            if (connectionHandler != null) {
                AuthenticationInfo storedAuthenticationInfo = connectionHandler.getAuthenticationInfo();

                if (passwordDialog.isRememberCredentials()) {
                    storedAuthenticationInfo.setUser(newAuthenticationInfo.getUser());
                    storedAuthenticationInfo.setPassword(newAuthenticationInfo.getPassword());
                    storedAuthenticationInfo.setOsAuthentication(newAuthenticationInfo.isOsAuthentication());
                    storedAuthenticationInfo.setEmptyPassword(newAuthenticationInfo.isEmptyPassword());
                } else {
                    connectionHandler.setTemporaryAuthenticationInfo(newAuthenticationInfo.clone());
                }
                connectionHandler.getInstructions().setAllowAutoConnect(true);
            }
            callback.setData(newAuthenticationInfo);
            callback.start();
        }
    }

    /*********************************************************
     *                     Miscellaneous                     *
     *********************************************************/
    @Nullable
    public ConnectionHandler getConnectionHandler(ConnectionId connectionId) {
         for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers().getFullList()) {
            if (connectionHandler.getId() == connectionId) {
                return connectionHandler;
            }
         }
         return null;
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
        for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                return true;
            }
        }
        return false;
    }

    private void commitAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                transactionManager.commit(connectionHandler, null, false, false);
            }
        }
    }

    private void rollbackAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                transactionManager.rollback(connectionHandler, null, false, false);
            }
        }
    }

    public boolean isValidConnectionId(ConnectionId connectionId) {
        return getConnectionHandler(connectionId) != null;
    }

    private class CloseIdleConnectionTask extends TimerTask {
        public void run() {
            try {
                for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
                    resolveIdleStatus(connectionHandler);
                }
            } catch (Exception ignore){}
        }

        private void resolveIdleStatus(final ConnectionHandler connectionHandler) {
            FailsafeUtil.check(connectionHandler);
            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
            List<DBNConnection> activeConnections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);

            for (DBNConnection connection : activeConnections) {
                if (connection.isIdle() && connection.isNot(ResourceStatus.RESOLVING_TRANSACTION)) {

                    int idleMinutes = connection.getIdleMinutes();
                    int idleMinutesToDisconnect = connectionHandler.getSettings().getDetailSettings().getIdleTimeToDisconnect();
                    if (idleMinutes > idleMinutesToDisconnect) {
                        if (connection.hasDataChanges()) {
                            connection.set(ResourceStatus.RESOLVING_TRANSACTION, true);
                            SimpleLaterInvocator.invoke(() -> {
                                IdleConnectionDialog idleConnectionDialog = new IdleConnectionDialog(connectionHandler, connection);
                                idleConnectionDialog.show();
                            });
                        } else {
                            transactionManager.execute(connectionHandler, connection, false, TransactionAction.DISCONNECT_IDLE);
                        }
                    }
                }
            }

        }
    }

    void disposeConnections(@NotNull List<ConnectionHandler> connectionHandlers) {
        if (connectionHandlers.size() > 0) {
            final Project project = getProject();
            ConditionalLaterInvocator.invoke(() -> {
                List<ConnectionId> connectionIds = new ArrayList<>();
                for (ConnectionHandler connectionHandler : connectionHandlers) {
                    connectionIds.add(connectionHandler.getId());
                }

                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                executionManager.closeExecutionResults(connectionIds);

                DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                databaseFileManager.closeDatabaseFiles(connectionIds);

                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                methodExecutionManager.cleanupExecutionHistory(connectionIds);

                BackgroundTask.invoke(project,
                        TaskInstructions.create("Cleaning up connections", TaskInstruction.CANCELLABLE),
                        (data, progress) -> DisposerUtil.dispose(connectionHandlers));
            });
        }
    }

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    @Override
    public boolean canCloseProject(@NotNull Project project) {
        return canClose(project, IdeMonitor.getInstance().getProjectCloseCallback(project));
    }

    @Override
    public boolean canExitApplication() {
        return true;//canClose(null, closeApplicationRunnable);
    }

    private boolean canClose(Project project, Runnable successCallback) {
        if (project == getProject() && hasUncommittedChanges()) {
            TransactionManagerSettings transactionManagerSettings = DatabaseTransactionManager.getInstance(project).getTransactionManagerSettings();
            InteractiveOptionHandler<TransactionOption> closeProjectOptionHandler = transactionManagerSettings.getCloseProject();

            TransactionOption result = closeProjectOptionHandler.resolve(project.getName());
            switch (result) {
                case COMMIT: commitAll(); return true;
                case ROLLBACK: rollbackAll(); return true;
                case REVIEW_CHANGES: return DatabaseTransactionManager.getInstance(project).showPendingTransactionsOverviewDialog(null);
                case CANCEL: return false;
            }
        }
        return true;
    }

    /**********************************************
    *                ProjectComponent             *
    ***********************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /*********************************************************
     *                PersistentStateComponent               *
     *********************************************************/
    @Nullable
    public Element getState() {
        return null;
    }

    public void loadState(@NotNull Element element) {}
}
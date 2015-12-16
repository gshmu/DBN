package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.ModalTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
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
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

@State(
        name = "DBNavigator.Project.ConnectionManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ConnectionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
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
        idleConnectionCleaner.cancel();
        idleConnectionCleaner.purge();
        Disposer.dispose(connectionBundle);
    }

    @Nullable
    public static ConnectionHandler getLastUsedConnection() {
        return ConnectionHandlerRef.get(lastUsedConnection);
    }

    @Nullable
    public static ConnectionInfo getLastUsedConnectionInfo() {
        ConnectionHandler lastUsedConnection = getLastUsedConnection();
        return lastUsedConnection == null ? null : lastUsedConnection.getConnectionInfo();
    }

    public static void setLastUsedConnection(@NotNull ConnectionHandler lastUsedConnection) {
        ConnectionManager.lastUsedConnection = lastUsedConnection.getRef();
    }

    /*********************************************************
    *                       Listeners                        *
    *********************************************************/

    ConnectionSettingsListener connectionSettingsListener = new ConnectionSettingsAdapter() {
        @Override
        public void connectionChanged(String connectionId) {
            ConnectionHandler connectionHandler = getConnectionHandler(connectionId);
            if (connectionHandler != null) {
                connectionHandler.getConnectionPool().closeConnectionsSilently();
                connectionHandler.getObjectBundle().getObjectListContainer().reload(true);
            }
        }
    };

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public ConnectionBundle getConnectionBundle() {
        return connectionBundle;
    }

    public void testConnection(ConnectionHandler connectionHandler, boolean showSuccessMessage, boolean showErrorMessage) {
        Project project = getProject();
        ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
        String connectionName = connectionHandler.getName();
        try {
            databaseSettings.checkConfiguration();
            connectionHandler.getStandaloneConnection();
            ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
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

    public void testConfigConnection(final ConnectionSettings connectionSettings, final boolean showMessageDialog) {
        final Project project = connectionSettings.getProject();
        final ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        final String connectionName = databaseSettings.getName();
        try {
            databaseSettings.checkConfiguration();
            final AuthenticationInfo temporaryAuthenticationInfo = getTemporaryAuthentication(databaseSettings);
            if (temporaryAuthenticationInfo == null){
                return;
            }

            new ModalTask(project, "Connecting to " + connectionName, false) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        Connection connection = ConnectionUtil.connect(connectionSettings, temporaryAuthenticationInfo, false, null, ConnectionType.TEST);
                        ConnectionUtil.closeConnection(connection);
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
                }
            }.start();

        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    public void showConnectionInfo(final ConnectionSettings connectionSettings, final EnvironmentType environmentType) {
        final ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        final String connectionName = databaseSettings.getName();
        final Project project = connectionSettings.getProject();

        try {
            databaseSettings.checkConfiguration();
            final AuthenticationInfo temporaryAuthenticationInfo = getTemporaryAuthentication(databaseSettings);
            if (temporaryAuthenticationInfo == null) {
                return;
            }

            new ModalTask(project, "Connecting to " + connectionName, false) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        Connection connection = ConnectionUtil.connect(connectionSettings, temporaryAuthenticationInfo, false, null, ConnectionType.TEST);
                        ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
                        ConnectionUtil.closeConnection(connection);
                        showConnectionInfoDialog(getProject(), connectionInfo, connectionName, environmentType);
                    } catch (Exception e) {
                        showErrorConnectionMessage(project, connectionName, e);
                    }

                }
            }.start();
        } catch (ConfigurationException e) {
            showInvalidConfigMessage(project, e);
        }
    }

    public AuthenticationInfo getTemporaryAuthentication(ConnectionDatabaseSettings databaseSettings) {
        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo().clone();
        if (!authenticationInfo.isProvided()) {
            return openUserPasswordDialog(databaseSettings.getProject(), null, authenticationInfo);
        }
        return authenticationInfo;
    }

    private void showErrorConnectionMessage(Project project, String connectionName, Exception e) {
        MessageUtil.showErrorDialog(
                project,
                "Connection error",
                "Cannot connect to \"" + connectionName + "\".\n" + e.getMessage());
    }

    private void showSuccessfulConnectionMessage(Project project, String connectionName) {
        MessageUtil.showInfoDialog(
                project,
                "Connection successful",
                "Connection to \"" + connectionName + "\" was successful.");
    }

    private void showInvalidConfigMessage(Project project, ConfigurationException e) {
        MessageUtil.showErrorDialog(
                project,
                "Invalid configuration",
                e.getMessage());
    }

    public void showConnectionInfoDialog(final ConnectionHandler connectionHandler) {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(connectionHandler);
                infoDialog.setModal(true);
                infoDialog.show();
            }
        }.start();
    }

    public void showConnectionInfoDialog(final Project project, final ConnectionInfo connectionInfo, final String connectionName, final EnvironmentType environmentType) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(project, connectionInfo, connectionName, environmentType);
                infoDialog.setModal(true);
                infoDialog.show();
            }
        }.start();
    }

    public static AuthenticationInfo openUserPasswordDialog(Project project, @Nullable ConnectionHandler connectionHandler, @NotNull AuthenticationInfo authenticationInfo) {
        ConnectionAuthenticationDialog passwordDialog = new ConnectionAuthenticationDialog(project, connectionHandler, authenticationInfo);
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

                connectionHandler.setAllowConnection(true);
            }
            return newAuthenticationInfo;
        }

        return null;
    }

    /*********************************************************
     *                     Miscellaneous                     *
     *********************************************************/
    @Nullable
    public ConnectionHandler getConnectionHandler(String connectionId) {
         for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers().getFullList()) {
            if (connectionHandler.getId().equals(connectionId)) {
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
             connectionHandler = FileConnectionMappingManager.getInstance(project).getActiveConnection(virtualFile);
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

    public void commitAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                transactionManager.commit(connectionHandler, false, false);
            }
        }
    }

    public void rollbackAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                transactionManager.rollback(connectionHandler, false, false);
            }
        }
    }

    public boolean isValidConnectionId(String connectionId) {
        return getConnectionHandler(connectionId) != null;
    }

    private class CloseIdleConnectionTask extends TimerTask {
        public void run() {
            for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
                resolveIdleStatus(connectionHandler);
            }
        }
        private void resolveIdleStatus(final ConnectionHandler connectionHandler) {
            FailsafeUtil.check(connectionHandler);
            final DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
            final ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
            if (connectionHandler.getLoadMonitor().isIdle() && !connectionStatus.isResolvingIdleStatus()) {
                int idleMinutes = connectionHandler.getIdleMinutes();
                int idleMinutesToDisconnect = connectionHandler.getSettings().getDetailSettings().getIdleTimeToDisconnect();
                if (idleMinutes > idleMinutesToDisconnect) {
                    if (connectionHandler.hasUncommittedChanges()) {
                        connectionHandler.getConnectionStatus().setResolvingIdleStatus(true);
                        new SimpleLaterInvocator() {
                            @Override
                            protected void execute() {
                                IdleConnectionDialog idleConnectionDialog = new IdleConnectionDialog(connectionHandler);
                                idleConnectionDialog.show();
                            }
                        }.start();
                    } else {
                        transactionManager.execute(connectionHandler, false, TransactionAction.DISCONNECT_IDLE);
                    }
                }
            }
        }
    }

    public void disposeConnections(@NotNull final List<ConnectionHandler> connectionHandlers) {
        if (connectionHandlers.size() > 0) {
            final Project project = getProject();
            new ConditionalLaterInvocator() {
                @Override
                protected void execute() {
                    List<String> connectionIds = new ArrayList<String>();
                    for (ConnectionHandler connectionHandler : connectionHandlers) {
                        connectionIds.add(connectionHandler.getId());
                    }

                    ExecutionManager executionManager = ExecutionManager.getInstance(project);
                    executionManager.closeExecutionResults(connectionIds);

                    DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                    databaseFileManager.closeDatabaseFiles(connectionIds);

                    MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                    methodExecutionManager.cleanupExecutionHistory(connectionIds);

                    new BackgroundTask(project, "Cleaning up connections", true) {
                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            DisposerUtil.dispose(connectionHandlers);
                        }
                    }.start();

                }
            }.start();
        }
    }

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    @Override
    public boolean canCloseProject(Project project) {
        if (project == getProject() && hasUncommittedChanges()) {
            TransactionManagerSettings transactionManagerSettings = DatabaseTransactionManager.getInstance(project).getTransactionManagerSettings();
            InteractiveOptionHandler<TransactionOption> closeProjectOptionHandler = transactionManagerSettings.getCloseProject();

            TransactionOption result = closeProjectOptionHandler.resolve(project.getName());
            switch (result) {
                case COMMIT: commitAll(); return true;
                case ROLLBACK: rollbackAll(); return true;
                case REVIEW_CHANGES: return DatabaseTransactionManager.getInstance(project).showUncommittedChangesOverviewDialog(null);
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
        return "DBNavigator.Project.ConnectionManager";
    }

    /*********************************************************
     *                PersistentStateComponent               *
     *********************************************************/
    @Nullable
    public Element getState() {
        return null;
    }

    public void loadState(Element element) {}
}
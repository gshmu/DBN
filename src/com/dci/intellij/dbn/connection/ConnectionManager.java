package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.dialog.MessageDialog;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettingsListener;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.info.ui.ConnectionInfoDialog;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.dci.intellij.dbn.connection.transaction.ui.IdleConnectionDialog;
import com.dci.intellij.dbn.connection.ui.ConnectionUserPasswordDialog;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
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
    private final ConnectionSettingsListener connectionSettingsListener;
    private Timer idleConnectionCleaner;

    public static ConnectionManager getInstance(@NotNull Project project) {
        return getComponent(project);
    }

    private static ConnectionManager getComponent(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ConnectionManager.class);
    }

    private ConnectionManager(final Project project) {
        super(project);
        connectionSettingsListener = new ConnectionSettingsListener() {
            @Override
            public void settingsChanged(String connectionId) {
                ConnectionHandler connectionHandler = getConnectionHandler(connectionId);
                if (connectionHandler != null) {
                    connectionHandler.getConnectionPool().closeConnectionsSilently();
                    connectionHandler.getObjectBundle().getObjectListContainer().reload(true);
                }
            }
        };
    }

    @Override
    public void initComponent() {
        super.initComponent();
        Project project = getProject();
        EventManager.subscribe(project, ConnectionBundleSettingsListener.TOPIC, connectionBundleSettingsListener);
        EventManager.subscribe(project, ConnectionSettingsListener.TOPIC, connectionSettingsListener);
        idleConnectionCleaner = new Timer("DBN - Idle Connection Cleaner [" + project.getName() + "]");
        idleConnectionCleaner.schedule(new CloseIdleConnectionTask(), TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }

    @Override
    public void dispose() {
        ConnectionBundle connectionBundle = getConnectionBundle();
        super.dispose();
        idleConnectionCleaner.cancel();
        idleConnectionCleaner.purge();
        EventManager.unsubscribe(
                connectionBundleSettingsListener,
                connectionSettingsListener);
        Disposer.dispose(connectionBundle);
    }

    /*********************************************************
    *                       Listeners                        *
    *********************************************************/

    private ConnectionBundleSettingsListener connectionBundleSettingsListener = new ConnectionBundleSettingsListener() {
        @Override
        public void settingsChanged() {
            EventManager.notify(getProject(), ConnectionManagerListener.TOPIC).connectionsChanged();
        }
    };

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public ConnectionBundle getConnectionBundle() {
        return ConnectionBundleSettings.getInstance(getProject()).getConnectionBundle();
    }

    public void testConnection(ConnectionHandler connectionHandler, boolean showSuccessMessage, boolean showErrorMessage) {
        Project project = getProject();
        ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
        try {
            connectionHandler.getStandaloneConnection();
            if (showSuccessMessage) {
                MessageDialog.showInfoDialog(
                        project,
                        "Successfully connected to \"" + connectionHandler.getName() + "\".",
                        databaseSettings.getConnectionDetails(),
                        false);
            }
        } catch (Exception e) {
            if (showErrorMessage) {
                MessageDialog.showErrorDialog(
                        project,
                        "Could not connect to \"" + connectionHandler.getName() + "\".",
                        databaseSettings.getConnectionDetails() + "\n\n" + e.getMessage(),
                        false);
            }
        }
    }

    public static void testConfigConnection(ConnectionDatabaseSettings databaseSettings, boolean showMessageDialog) {
        Project project = databaseSettings.getProject();
        try {
            Authentication temporaryAuthentication = null;
            Authentication authentication = databaseSettings.getAuthentication();
            if (!authentication.isProvided()) {
                temporaryAuthentication = openUserPasswordDialog(project, null, authentication.clone());
                if (temporaryAuthentication == null){
                    return;
                }
            }

            Connection connection = ConnectionUtil.connect(databaseSettings, temporaryAuthentication, false, null, ConnectionType.TEST);
            ConnectionUtil.closeConnection(connection);
            databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
            if (showMessageDialog) {
                MessageDialog.showInfoDialog(
                        project,
                        "Test connection to \"" + databaseSettings.getName() + "\" succeeded. Configuration is valid.",
                        databaseSettings.getConnectionDetails(),
                        false);
            }

        } catch (Exception e) {
            databaseSettings.setConnectivityStatus(ConnectivityStatus.INVALID);
            if (showMessageDialog) {
                MessageDialog.showErrorDialog(
                        project,
                        "Could not connect to \"" + databaseSettings.getName() + "\".",
                        databaseSettings.getConnectionDetails() + "\n\n" + e.getMessage(),
                        false);
            }
        }
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

    public ConnectionInfo showConnectionInfo(ConnectionSettings connectionSettings) {
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        return showConnectionInfo(databaseSettings);
    }

    public static ConnectionInfo showConnectionInfo(ConnectionDatabaseSettings databaseSettings) {
        Project project = databaseSettings.getProject();
        try {
            Authentication temporaryAuthentication = null;
            Authentication authentication = databaseSettings.getAuthentication();
            if (!authentication.isProvided()) {
                temporaryAuthentication = openUserPasswordDialog(project, null, authentication.clone());
                if (temporaryAuthentication == null) {
                    return null;
                }
            }

            Connection connection = ConnectionUtil.connect(databaseSettings, temporaryAuthentication, false, null, ConnectionType.TEST);
            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
            ConnectionUtil.closeConnection(connection);
            MessageDialog.showInfoDialog(
                    project,
                    "Database details for connection \"" + databaseSettings.getName() + '"',
                    connectionInfo.toString(),
                    false);
            return connectionInfo;

        } catch (Exception e) {
            MessageDialog.showErrorDialog(
                    project,
                    "Could not connect to \"" + databaseSettings.getName() + "\".",
                    databaseSettings.getConnectionDetails() + "\n\n" + e.getMessage(),
                    false);
            return null;
        }
    }

    public static Authentication openUserPasswordDialog(Project project, @Nullable ConnectionHandler connectionHandler, @NotNull Authentication authentication) {
        ConnectionUserPasswordDialog passwordDialog = new ConnectionUserPasswordDialog(project, connectionHandler, authentication);
        passwordDialog.show();
        if (passwordDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            Authentication newAuthentication = passwordDialog.getAuthentication();
            if (connectionHandler != null) {
                ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
                Authentication storedAuthentication = databaseSettings.getAuthentication();

                if (passwordDialog.isRememberCredentials()) {
                    storedAuthentication.setUser(newAuthentication.getUser());
                    storedAuthentication.setPassword(newAuthentication.getPassword());
                } else {
                    connectionHandler.setTemporaryAuthentication(newAuthentication.clone());
                }

                connectionHandler.setAllowConnection(true);
            }
            return newAuthentication;
        }

        return null;
    }

    /*********************************************************
     *                     Miscellaneous                     *
     *********************************************************/
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

    private class CloseIdleConnectionTask extends TimerTask {
        public void run() {
            for (ConnectionHandler connectionHandler : getConnectionBundle().getConnectionHandlers()) {
                resolveIdleStatus(connectionHandler);
            }
        }
        private void resolveIdleStatus(final ConnectionHandler connectionHandler) {
            final DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
            final ConnectionStatus connectionStatus = connectionHandler.getConnectionStatus();
            if (connectionStatus!= null && !connectionStatus.isResolvingIdleStatus()) {
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
        final Project project = getProject();
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                executionManager.closeExecutionResults(connectionHandlers);

                DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                databaseFileManager.closeDatabaseFiles(connectionHandlers);

                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                methodExecutionManager.cleanupExecutionHistory(connectionHandlers);

                new BackgroundTask(project, "Cleaning up connections", true) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                        DisposerUtil.dispose(connectionHandlers);
                    }
                }.start();

            }
        }.start();
    }

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    @Override
    public boolean canCloseProject(Project project) {
        if (project == getProject() && hasUncommittedChanges()) {
            TransactionManagerSettings transactionManagerSettings = DatabaseTransactionManager.getInstance(project).getTransactionManagerSettings();
            InteractiveOptionHandler<TransactionOption> closeProjectOptionHandler = transactionManagerSettings.getCloseProjectOptionHandler();

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

    public void loadState(Element element) {
        if (getConnectionBundle().isEmpty()) {
            Element connectionsElement = element.getChild("connections");
            if (connectionsElement != null) {
                ProjectSettingsManager.getSettings(getProject()).getConnectionSettings().readConfiguration(connectionsElement);
            }
        }
    }
}
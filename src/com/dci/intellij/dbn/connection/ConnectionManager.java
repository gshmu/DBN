package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.dialog.MessageDialog;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettingsListener;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.info.ui.ConnectionInfoDialog;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.ui.IdleConnectionDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionManager extends AbstractProjectComponent {
    private final ConnectionSettingsListener connectionSettingsListener;
    private Timer idleConnectionCleaner;

    private InteractiveOptionHandler closeProjectOptionHandler =
            new InteractiveOptionHandler(
                    "Uncommitted changes",
                    "You have uncommitted changes on one or more connections for project \"{0}\". \n" +
                    "Please specify whether to commit or rollback these changes before closing the project",
                    2, "Commit", "Rollback", "Review Changes", "Cancel");

    public static ConnectionManager getInstance(Project project) {
        return project.getComponent(ConnectionManager.class);
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
        idleConnectionCleaner = new Timer("Idle connection cleaner [" + project.getName() + "]");
        idleConnectionCleaner.schedule(new CloseIdleConnectionTask(), TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }

    @Override
    public void disposeComponent() {
        idleConnectionCleaner.cancel();
        idleConnectionCleaner.purge();
        EventManager.unsubscribe(
                connectionBundleSettingsListener,
                connectionSettingsListener);
        super.disposeComponent();
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
        return ProjectConnectionBundle.getInstance(getProject());
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

    public void testConfigConnection(ConnectionDatabaseSettings databaseSettings, boolean showMessageDialog) {
        Project project = getProject();
        try {
            Connection connection = ConnectionUtil.connect(databaseSettings, null, false, null);
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
            public void execute() {
                ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(connectionHandler);
                infoDialog.setModal(true);
                infoDialog.show();
            }
        }.start();
    }

    public ConnectionInfo showConnectionInfo(ConnectionSettings connectionSettings) {
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        return showConnectionInfo(databaseSettings, detailSettings);
    }

    public ConnectionInfo showConnectionInfo(ConnectionDatabaseSettings databaseSettings, @Nullable ConnectionDetailSettings detailSettings) {
        try {
            Map<String, String> connectionProperties = detailSettings == null ? null : detailSettings.getProperties();
            Connection connection = ConnectionUtil.connect(databaseSettings, connectionProperties, false, null);
            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
            ConnectionUtil.closeConnection(connection);
            MessageDialog.showInfoDialog(
                    getProject(),
                    "Database details for connection \"" + databaseSettings.getName() + "\"",
                    connectionInfo.toString(),
                    false);
            return connectionInfo;

        } catch (Exception e) {
            MessageDialog.showErrorDialog(
                    getProject(),
                    "Could not connect to \"" + databaseSettings.getName() + "\".",
                    databaseSettings.getConnectionDetails() + "\n\n" + e.getMessage(),
                    false);
            return null;
        }
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

         if (connectionHandler == null && virtualFile!= null) {
             connectionHandler = FileConnectionMappingManager.getInstance(project).getActiveConnection(virtualFile);
         }

         return connectionHandler;
     }

    public boolean hasUncommittedChanges() {
        ConnectionBundle connectionBundle = getConnectionBundle();
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                return true;
            }
        }
        return false;
    }

    public void commitAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        ConnectionBundle connectionBundle = getConnectionBundle();
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                transactionManager.commit(connectionHandler, false, false);
            }
        }
    }

    public void rollbackAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        ConnectionBundle connectionBundle = getConnectionBundle();
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            if (connectionHandler.hasUncommittedChanges()) {
                transactionManager.rollback(connectionHandler, false, false);
            }
        }
    }

    private class CloseIdleConnectionTask extends TimerTask {
        public void run() {
            ConnectionBundle connectionBundle = getConnectionBundle();
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
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
                            public void execute() {
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

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    @Override
    public boolean canCloseProject(Project project) {
        if (project == getProject() && hasUncommittedChanges()) {
            int result = closeProjectOptionHandler.resolve(project.getName());
            switch (result) {
                case 0: commitAll(); return true;
                case 1: rollbackAll(); return true;
                case 2: return DatabaseTransactionManager.getInstance(project).showUncommittedChangesOverviewDialog(null);
                case 3: return false;
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
        return "DBNavigator.Project.DatabaseConnectionManager";
    }
}
package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.dci.intellij.dbn.connection.info.ui.ConnectionInfoDialog;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.ui.IdleConnectionDialog;
import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;

public class ConnectionManager extends AbstractProjectComponent implements ProjectManagerListener{
    private List<ConnectionBundle> connectionBundles = new ArrayList<ConnectionBundle>();
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

    private ConnectionManager(Project project) {
        super(project);
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(project, this);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        Project project = getProject();
        EventManager.subscribe(project, ProjectTopics.MODULES, moduleListener);
        EventManager.subscribe(project, ConnectionBundleSettingsListener.TOPIC, connectionBundleSettingsListener);
        initConnectionBundles();
        idleConnectionCleaner = new Timer("Idle connection cleaner [" + project.getName() + "]");
        idleConnectionCleaner.schedule(new CloseIdleConnectionTask(), TimeUtil.ONE_MINUTE, TimeUtil.ONE_MINUTE);
    }

    @Override
    public void disposeComponent() {
        idleConnectionCleaner.cancel();
        idleConnectionCleaner.purge();
        EventManager.unsubscribe(
                moduleListener,
                connectionBundleSettingsListener);
        super.disposeComponent();
    }

    /*********************************************************
    *                       Listeners                        *
    *********************************************************/
    private ModuleListener moduleListener = new ModuleAdapter() {
        public void moduleAdded(Project project, Module module) {
            initConnectionBundles();
        }

        public void moduleRemoved(Project project, Module module) {
            initConnectionBundles();
        }

        public void modulesRenamed(Project project, List<Module> modules) {
            for (Module module : modules) {
                ModuleConnectionBundle connectionBundle = ModuleConnectionBundle.getInstance(module);
                if (connectionBundle.getConnectionHandlers().size() > 0) {
                    initConnectionBundles();
                    break;
                }
            }
        }
    };

    private ConnectionBundleSettingsListener connectionBundleSettingsListener = new ConnectionBundleSettingsListener() {
        @Override
        public void settingsChanged() {
            initConnectionBundles();
        }
    };

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public List<ConnectionBundle> getConnectionBundles() {
        return connectionBundles;
    }

    private synchronized void initConnectionBundles() {
        Project project = getProject();
        connectionBundles.clear();
        ProjectConnectionBundle projectConnectionBundle = ProjectConnectionBundle.getInstance(project);
        if (projectConnectionBundle.getConnectionHandlers().size() > 0) {
            connectionBundles.add(projectConnectionBundle);
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            ModuleConnectionBundle moduleConnectionBundle = ModuleConnectionBundle.getInstance(module);
            if (moduleConnectionBundle.getConnectionHandlers().size() > 0) {
                connectionBundles.add(moduleConnectionBundle);
            }
        }
        Collections.sort(connectionBundles);
        EventManager.notify(project, ConnectionManagerListener.TOPIC).connectionsChanged();
    }

    public void testConnection(ConnectionHandler connectionHandler, boolean showMessageDialog) {
        Project project = getProject();
        ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
        try {
            connectionHandler.getStandaloneConnection();
            if (showMessageDialog) {
                MessageDialog.showInfoDialog(
                        project,
                        "Successfully connected to \"" + connectionHandler.getName() + "\".",
                        databaseSettings.getConnectionDetails(),
                        false);
            }
        } catch (Exception e) {
            if (showMessageDialog) {
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

    public void showConnectionInfoDialog(ConnectionHandler connectionHandler) {
        final ConnectionInfoDialog infoDialog = new ConnectionInfoDialog(connectionHandler);
        new ConditionalLaterInvocator() {

            @Override
            public void execute() {
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
         for (ConnectionBundle connectionBundle : connectionBundles) {
             for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers().getFullList()) {
                if (connectionHandler.getId().equals(connectionId)) {
                    return connectionHandler;
                }
             }
         }
         return null;
     }

     public Set<ConnectionHandler> getConnectionHandlers() {
         Set<ConnectionHandler> connectionHandlers = new THashSet<ConnectionHandler>();
         for (ConnectionBundle connectionBundle : connectionBundles) {
             connectionHandlers.addAll(connectionBundle.getConnectionHandlers());
         }
         return connectionHandlers;
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
        for (ConnectionBundle connectionBundle : getConnectionBundles()) {
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                if (connectionHandler.hasUncommittedChanges()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void commitAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        for (ConnectionBundle connectionBundle : getConnectionBundles()) {
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                if (connectionHandler.hasUncommittedChanges()) {
                    transactionManager.commit(connectionHandler, false, false);
                }
            }
        }
    }

    public void rollbackAll() {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
        for (ConnectionBundle connectionBundle : getConnectionBundles()) {
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                if (connectionHandler.hasUncommittedChanges()) {
                    transactionManager.rollback(connectionHandler, false, false);
                }
            }
        }
    }

    private class CloseIdleConnectionTask extends TimerTask {
        public void run() {
            for (ConnectionBundle connectionBundle : getConnectionBundles()) {
                for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                    resolveIdleStatus(connectionHandler);
                }
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
    public void projectOpened(Project project) {}

    @Override
    public boolean canCloseProject(Project project) {
        if (hasUncommittedChanges()) {
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

    @Override
    public void projectClosed(Project project) {
    }

    @Override
    public void projectClosing(Project project) {
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
package com.dci.intellij.dbn.editor.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

@State(
    name = "DBNavigator.Project.SessionEditorManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class SessionBrowserManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    private SessionBrowserManager(Project project) {
        super(project);
    }

    public static SessionBrowserManager getInstance(Project project) {
        return project.getComponent(SessionBrowserManager.class);
    }

    public SessionBrowserSettings getSessionBrowserSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getSessionBrowserSettings();
    }



    public void openSessionBrowser(ConnectionHandler connectionHandler) {
        Project project = connectionHandler.getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        DBSessionBrowserVirtualFile sessionBrowserFile = connectionHandler.getSessionBrowserFile();
        fileEditorManager.openFile(sessionBrowserFile, true);
    }

    public void interruptSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, SessionInterruptionType type) {
        final ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
        final DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        if (compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_DISCONNECT_TIMING)) {
            Project project = connectionHandler.getProject();

            SessionBrowserSettings sessionBrowserSettings = getSessionBrowserSettings();
            InteractiveOptionHandler<SessionInterruptionOption> disconnectOptionHandler =
                    type == SessionInterruptionType.KILL ? sessionBrowserSettings.getKillSessionOptionHandler() :
                    type == SessionInterruptionType.DISCONNECT  ? sessionBrowserSettings.getDisconnectSessionOptionHandler() : null;

            if (disconnectOptionHandler != null) {
                SessionInterruptionOption result = disconnectOptionHandler.resolve(connectionHandler.getName());
                if (result != SessionInterruptionOption.CANCEL && result != SessionInterruptionOption.ASK) {
                    doInterruptSessions(sessionBrowser, sessionIds, type, result);
                }
            }
        } else {
            doInterruptSessions(sessionBrowser, sessionIds, SessionInterruptionType.KILL, SessionInterruptionOption.NORMAL);
        }
    }

    private void doInterruptSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, final SessionInterruptionType type, final SessionInterruptionOption option) {
        final ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
        final DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        new BackgroundTask(getProject(), "Killing Sessions", false, true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                Project project = connectionHandler.getProject();
                Connection connection = null;
                try {
                    final String killedAction = type == SessionInterruptionType.KILL ? "killed" : "disconnected";
                    final String killingAction = type == SessionInterruptionType.KILL? "killing" : "disconnecting";
                    connection = connectionHandler.getPoolConnection();
                    Map<Object, SQLException> errors = new HashMap<Object, SQLException>();
                    final DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    progressIndicator.setIndeterminate(true);

                    for (Object sessionId : sessionIds.keySet()) {
                        Object serialNumber = sessionIds.get(sessionId);
                        if (progressIndicator.isCanceled()) return;
                        try {
                            boolean immediate = option == SessionInterruptionOption.IMMEDIATE;
                            boolean postTransaction = option == SessionInterruptionOption.POST_TRANSACTION;
                            switch (type) {
                                case DISCONNECT: metadataInterface.disconnectSession(sessionId, serialNumber, postTransaction, immediate, connection); break;
                                case KILL: metadataInterface.killSession(sessionId, serialNumber, immediate, connection); break;
                            }
                        } catch (SQLException e) {
                            errors.put(sessionId, e);
                        }
                    }

                    if (sessionIds.size() == 1) {
                        Object sessionId = sessionIds.keySet().iterator().next();
                        if (errors.size() == 0) {
                            MessageUtil.showInfoDialog(project, "Info", "Session with id \"" + sessionId + "\" " + killedAction +".");
                        } else {
                            SQLException exception = errors.get(sessionId);
                            MessageUtil.showErrorDialog(project, "Error " + killingAction + " session with id \"" + sessionId + "\"." , exception);
                        }
                    } else {
                        if (errors.size() == 0) {
                            MessageUtil.showInfoDialog(project, "Info", sessionIds.size() + " sessions " + killedAction + ".");
                        } else {
                            StringBuilder message = new StringBuilder("Error " + killingAction + " one or more of the selected sessions:");
                            for (Object sessionId : sessionIds.keySet()) {
                                SQLException exception = errors.get(sessionId);
                                message.append("\n - session id ").append(sessionId).append(": ");
                                if (exception == null) message.append(killedAction); else message.append(exception.getMessage().trim());

                            }
                            MessageUtil.showErrorDialog(project, message.toString());
                        }

                    }
                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(project, "Error performing operation", e);
                } finally {
                    sessionBrowser.reload();
                    connectionHandler.freePoolConnection(connection);
                }
            }
        }.start();
    }

    /****************************************
    *             ProjectComponent          *
    *****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.SessionEditorManager";
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }


    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        return element;
    }

    @Override
    public void loadState(Element element) {
    }
}

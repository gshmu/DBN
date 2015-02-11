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
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

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

    public void openSessionBrowser(ConnectionHandler connectionHandler) {
        Project project = connectionHandler.getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        DBSessionBrowserVirtualFile sessionBrowserFile = connectionHandler.getSessionBrowserFile();
        fileEditorManager.openFile(sessionBrowserFile, true);
    }

    public void disconnectSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, final boolean kill) {
        final ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
        final DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        if (compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_DISCONNECT_TIMING)) {
            Project project = connectionHandler.getProject();
            String title;
            String message;
            String[] options;
            SimpleTask task;
            DialogWrapper.DoNotAskOption doNotAskOption = null;

            if (kill) {
                title = "Kill Sessions";
                message = "Please select kill option";
                options = new String[] {"Kill", "Kill Immediate", "Cancel"};
                task = new SimpleTask() {
                    @Override
                    protected void execute() {
                        int option = getOption();
                        if (option != 2) {
                            boolean immediate = option == 1;
                            doDisconnectSessions(sessionBrowser, sessionIds, true, immediate, false);
                        }
                    }
                };
                doNotAskOption = new DialogWrapper.DoNotAskOption() {
                    @Override
                    public boolean isToBeShown() {
                        return false;
                    }

                    @Override
                    public void setToBeShown(boolean toBeShown, int exitCode) {

                    }

                    @Override
                    public boolean canBeHidden() {
                        return false;
                    }

                    @Override
                    public boolean shouldSaveOptionsOnCancel() {
                        return false;
                    }

                    @NotNull
                    @Override
                    public String getDoNotShowMessage() {
                        return null;
                    }
                };
            } else {
                title = "Disconnect Sessions";
                message = "Please select disconnect option";
                options = new String[] {"Disconnect Immediate", "Disconnect Post Transaction", "Cancel"};
                task = new SimpleTask() {
                    @Override
                    protected void execute() {
                        int option = getOption();
                        if (option != 2) {
                            boolean immediate = option == 0;
                            boolean postTransaction = option == 1;
                            doDisconnectSessions(sessionBrowser, sessionIds, false, immediate, postTransaction);
                        }
                    }
                };

            }

            MessageUtil.showQuestionDialog(project, title, message, options, 0, task, doNotAskOption);
        } else {
            doDisconnectSessions(sessionBrowser, sessionIds, kill, false, false);
        }
    }

    private void doDisconnectSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, final boolean kill, final boolean immediate, final boolean postTransaction) {
        final ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
        final DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        new BackgroundTask(getProject(), "Killing Sessions", false, true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                Project project = connectionHandler.getProject();
                Connection connection = null;
                try {
                    final String killedAction = kill ? "killed" : "disconnected";
                    final String killingAction = kill ? "killing" : "disconnecting";
                    connection = connectionHandler.getPoolConnection();
                    Map<Object, SQLException> errors = new HashMap<Object, SQLException>();
                    final DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    progressIndicator.setIndeterminate(true);

                    for (Object sessionId : sessionIds.keySet()) {
                        Object serialNumber = sessionIds.get(sessionId);
                        if (progressIndicator.isCanceled()) return;
                        try {
                            if (kill)
                                metadataInterface.killSession(sessionId, serialNumber, immediate, connection); else
                                metadataInterface.disconnectSession(sessionId, serialNumber, postTransaction, immediate, connection);
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

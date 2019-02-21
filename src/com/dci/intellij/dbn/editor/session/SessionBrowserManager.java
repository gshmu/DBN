package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.option.InteractiveOptionBroker;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;
import static com.dci.intellij.dbn.common.util.CommonUtil.list;

@State(
    name = SessionBrowserManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class SessionBrowserManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    public static final String COMPONENT_NAME = "DBNavigator.Project.SessionEditorManager";

    private Timer timestampUpdater;
    private List<DBSessionBrowserVirtualFile> openFiles = new ArrayList<>();

    private SessionBrowserManager(Project project) {
        super(project);
    }

    public static SessionBrowserManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, SessionBrowserManager.class);
    }

    public SessionBrowserSettings getSessionBrowserSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getSessionBrowserSettings();
    }

    public void openSessionBrowser(ConnectionHandler connectionHandler) {
        ConnectionAction.invoke(
                "opening the session browser",
                connectionHandler,
                (Integer) null,
                action -> {
                    Project project = getProject();
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    DBSessionBrowserVirtualFile sessionBrowserFile = connectionHandler.getSessionBrowserFile();
                    fileEditorManager.openFile(sessionBrowserFile, true);
                });
    }

    public SessionBrowserModel loadSessions(DBSessionBrowserVirtualFile sessionBrowserFile) {
        ConnectionHandler connectionHandler = sessionBrowserFile.getConnectionHandler();
        DBNConnection connection = null;
        DBNResultSet resultSet = null;
        try {
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            connection = connectionHandler.getPoolConnection(true);
            resultSet = (DBNResultSet) metadataInterface.loadSessions(connection);
            return new SessionBrowserModel(connectionHandler, resultSet);
        } catch (SQLException e) {
            SessionBrowserModel model = new SessionBrowserModel(connectionHandler);
            model.setLoadError(e.getMessage());
            return model;

        } finally {
            ConnectionUtil.close(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
    }

    public String loadSessionCurrentSql(ConnectionHandler connectionHandler, Object sessionId) {
        DBNConnection connection = null;
        ResultSet resultSet = null;
        try {
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            if (DatabaseFeature.SESSION_CURRENT_SQL.isSupported(connectionHandler)) {
                DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();

                connection = connectionHandler.getPoolConnection(true);
                resultSet = metadataInterface.loadSessionCurrentSql(sessionId, connection);
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
            } else {
                return "";
            }
        } catch (SQLException e) {
            sendWarningNotification("Session Browser", "Could not load current session SQL. Cause: {0}", e.getMessage());
        } finally {
            ConnectionUtil.close(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
        return "";
    }

    public void interruptSessions(@NotNull SessionBrowser sessionBrowser, Map<Object, Object> sessionIds, SessionInterruptionType type) {
        ConnectionHandler connectionHandler = Failsafe.get(sessionBrowser.getConnectionHandler());
        if (DatabaseFeature.SESSION_INTERRUPTION_TIMING.isSupported(connectionHandler)) {

            SessionBrowserSettings sessionBrowserSettings = getSessionBrowserSettings();
            InteractiveOptionBroker<SessionInterruptionOption> disconnect =
                    type == SessionInterruptionType.KILL ? sessionBrowserSettings.getKillSession() :
                    type == SessionInterruptionType.DISCONNECT  ? sessionBrowserSettings.getDisconnectSession() : null;

            if (disconnect != null) {
                String subject = sessionIds.size() > 1 ? "selected sessions" : "session with id \"" + sessionIds.keySet().iterator().next().toString() + "\"";
                disconnect.resolve(
                        list(subject, connectionHandler.getName()),
                        option -> {
                            if (option != SessionInterruptionOption.CANCEL && option != SessionInterruptionOption.ASK) {
                                doInterruptSessions(sessionBrowser, sessionIds, type, option);
                            }
                        });
            }
        } else {
            doInterruptSessions(sessionBrowser, sessionIds, SessionInterruptionType.KILL, SessionInterruptionOption.NORMAL);
        }
    }

    private void doInterruptSessions(@NotNull SessionBrowser sessionBrowser, Map<Object, Object> sessionIds, SessionInterruptionType type, SessionInterruptionOption option) {
        String killedAction = type == SessionInterruptionType.KILL ? "killed" : "disconnected";
        String killingAction = type == SessionInterruptionType.KILL? "killing" : "disconnecting";
        String taskAction = (type == SessionInterruptionType.KILL? "Killing" : "Disconnecting") + (sessionIds.size() == 1 ? " Session" : " Sessions");

        Project project = getProject();
        BackgroundTask.invoke(project,
                instructions(taskAction, TaskInstruction.CANCELLABLE),
                (data, progress) -> {
                    ConnectionHandler connectionHandler = Failsafe.get(sessionBrowser.getConnectionHandler());
                    DBNConnection connection = null;
                    try {
                        connection = connectionHandler.getPoolConnection(true);
                        Map<Object, SQLException> errors = new HashMap<>();
                        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
                        DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();

                        for (Object sessionId : sessionIds.keySet()) {
                            Object serialNumber = sessionIds.get(sessionId);
                            if (progress.isCanceled()) return;
                            try {
                                boolean immediate = option == SessionInterruptionOption.IMMEDIATE;
                                boolean postTransaction = option == SessionInterruptionOption.POST_TRANSACTION;
                                switch (type) {
                                    case DISCONNECT:
                                        metadataInterface.disconnectSession(sessionId, serialNumber, postTransaction, immediate, connection);
                                        break;
                                    case KILL:
                                        metadataInterface.killSession(sessionId, serialNumber, immediate, connection);
                                        break;
                                }
                            } catch (SQLException e) {
                                errors.put(sessionId, e);
                            }
                        }

                        if (sessionIds.size() == 1) {
                            Object sessionId = sessionIds.keySet().iterator().next();
                            if (errors.size() == 0) {
                                MessageUtil.showInfoDialog(project, "Info", "Session " + sessionId + " " + killedAction + ".");
                            } else {
                                SQLException exception = errors.get(sessionId);
                                MessageUtil.showErrorDialog(project, "Error " + killingAction + " session " + sessionId + ".", exception);
                            }
                        } else {
                            if (errors.size() == 0) {
                                MessageUtil.showInfoDialog(project, "Info", sessionIds.size() + " sessions " + killedAction + ".");
                            } else {
                                StringBuilder message = new StringBuilder("Error " + killingAction + " one or more of the selected sessions:");
                                for (Object sessionId : sessionIds.keySet()) {
                                    SQLException exception = errors.get(sessionId);
                                    message.append("\n - session id ").append(sessionId).append(": ");
                                    if (exception == null) message.append(killedAction);
                                    else message.append(exception.getMessage().trim());

                                }
                                MessageUtil.showErrorDialog(project, message.toString());
                            }

                        }
                    } catch (SQLException e) {
                        MessageUtil.showErrorDialog(project, "Error performing operation", e);
                    } finally {
                        connectionHandler.freePoolConnection(connection);
                        sessionBrowser.loadSessions(false);
                    }
                });
    }

    private class UpdateTimestampTask extends TimerTask {
        @Override
        public void run() {
            if (openFiles.size() > 0) {
                ReadActionRunner.invoke(false, () ->
                        Failsafe.lenient(null, () -> {
                            Project project = getProject();
                            if (!project.isDisposed()) {
                                List<SessionBrowser> sessionBrowsers = new ArrayList<>();
                                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                                FileEditor[] editors = fileEditorManager.getAllEditors();
                                for (FileEditor editor : editors) {
                                    if (editor instanceof SessionBrowser) {
                                        SessionBrowser sessionBrowser = (SessionBrowser) editor;
                                        sessionBrowsers.add(sessionBrowser);
                                    }
                                }

                                SimpleLaterInvocator.invokeNonModal(() -> {
                                    for (SessionBrowser sessionBrowser : sessionBrowsers) {
                                        sessionBrowser.refreshLoadTimestamp();
                                    }
                                });
                            }
                            return null;
                        }));
            }
        }
    }

    /****************************************
    *             ProjectComponent          *
    *****************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public void projectOpened() {
        EventUtil.subscribe(getProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBSessionBrowserVirtualFile) {
                boolean schedule = openFiles.size() == 0;
                DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
                openFiles.add(sessionBrowserFile);

                if (schedule) {
                    timestampUpdater = new Timer("DBN - Session Browser (timestamp update timer)");
                    timestampUpdater.schedule(new UpdateTimestampTask(), TimeUtil.ONE_SECOND, TimeUtil.ONE_SECOND);
                }
            }
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBSessionBrowserVirtualFile) {
                DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
                openFiles.remove(sessionBrowserFile);

                if (openFiles.size() == 0 && timestampUpdater != null) {
                    timestampUpdater.cancel();
                    timestampUpdater.purge();
                }
            }
        }
    };

    @Override
    public void dispose() {
        super.dispose();
        if (timestampUpdater != null) {
            timestampUpdater.cancel();
            timestampUpdater.purge();
        }
        openFiles.clear();
    }


    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        return new Element("state");
    }

    @Override
    public void loadState(@NotNull Element element) {
    }
}

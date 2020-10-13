package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.option.InteractiveOptionBroker;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterface;
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
import com.intellij.util.containers.ContainerUtil;
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

import static com.dci.intellij.dbn.common.util.CommonUtil.list;

@State(
    name = SessionBrowserManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class SessionBrowserManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    public static final String COMPONENT_NAME = "DBNavigator.Project.SessionEditorManager";

    private final List<DBSessionBrowserVirtualFile> openFiles = ContainerUtil.createConcurrentList();
    private Timer timestampUpdater;

    private SessionBrowserManager(Project project) {
        super(project);
        subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    public static SessionBrowserManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, SessionBrowserManager.class);
    }

    public SessionBrowserSettings getSessionBrowserSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getSessionBrowserSettings();
    }

    public void openSessionBrowser(ConnectionHandler connectionHandler) {
        ConnectionAction.invoke("opening the session browser", false, connectionHandler,
                (action) -> {
                    Project project = getProject();
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    DBSessionBrowserVirtualFile sessionBrowserFile = connectionHandler.getSessionBrowserFile();
                    fileEditorManager.openFile(sessionBrowserFile, true);
                });
    }

    public SessionBrowserModel loadSessions(DBSessionBrowserVirtualFile sessionBrowserFile) {
        ConnectionHandler connectionHandler = sessionBrowserFile.getConnectionHandler();

        try {
            return DatabaseInterface.call(true,
                    connectionHandler,
                    (provider, connection) -> {
                        DBNResultSet resultSet = null;
                        try {
                            resultSet = (DBNResultSet) provider.getMetadataInterface().loadSessions(connection);
                            return new SessionBrowserModel(connectionHandler, resultSet);
                        } finally {
                            ResourceUtil.close(resultSet);
                        }
                    });

        } catch (SQLException e) {
            SessionBrowserModel model = new SessionBrowserModel(connectionHandler);
            model.setLoadError(e.getMessage());
            return model;
        }
    }

    public String loadSessionCurrentSql(ConnectionHandler connectionHandler, Object sessionId) {
        if (DatabaseFeature.SESSION_CURRENT_SQL.isSupported(connectionHandler)) {
            try {
                return DatabaseInterface.call(true,
                        connectionHandler,
                        (provider, connection) -> {
                            ResultSet resultSet = null;
                            try {
                                resultSet = provider.getMetadataInterface().loadSessionCurrentSql(sessionId, connection);
                                if (resultSet.next()) {
                                    return resultSet.getString(1);
                                }
                            } finally {
                                ResourceUtil.close(resultSet);
                            }
                            return "";
                        });
            } catch (SQLException e) {
                sendWarningNotification(
                        NotificationGroup.SESSION_BROWSER,
                        "Could not load current session SQL: {0}", e);
            }
        }
        return "";
    }

    public void interruptSessions(@NotNull SessionBrowser sessionBrowser, Map<Object, Object> sessionIds, SessionInterruptionType type) {
        ConnectionHandler connectionHandler = Failsafe.nn(sessionBrowser.getConnectionHandler());
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
        Progress.prompt(project, taskAction, true,
                (progress) -> {
                    try {
                        ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();

                        DatabaseInterface.run(true,
                                connectionHandler,
                                (provider, connection) -> {
                                    Map<Object, SQLException> errors = new HashMap<>();
                                    DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                                    for (Object sessionId : sessionIds.keySet()) {
                                        Object serialNumber = sessionIds.get(sessionId);
                                        checkDisposed();
                                        ProgressMonitor.checkCancelled();

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
                                });
                    } catch (SQLException e) {
                        MessageUtil.showErrorDialog(project, "Error performing operation", e);
                    } finally {
                        sessionBrowser.loadSessions(false);
                    }
                });
    }

    private class UpdateTimestampTask extends TimerTask {
        @Override
        public void run() {
            if (openFiles.size() > 0) {
                Read.run(() -> {
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

                        if (sessionBrowsers.size() > 0) {
                            Dispatch.run(() -> {
                                for (SessionBrowser sessionBrowser : sessionBrowsers) {
                                    sessionBrowser.refreshLoadTimestamp();
                                }
                            });
                        }
                    }
                });
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

    private final FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBSessionBrowserVirtualFile) {
                boolean schedule = openFiles.size() == 0;
                DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
                openFiles.add(sessionBrowserFile);

                if (schedule) {
                    timestampUpdater = new Timer("DBN - Session Browser (timestamp update timer)");
                    timestampUpdater.schedule(new UpdateTimestampTask(), TimeUtil.Millis.ONE_SECOND, TimeUtil.Millis.ONE_SECOND);
                }
            }
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBSessionBrowserVirtualFile) {
                DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
                openFiles.remove(sessionBrowserFile);

                if (openFiles.size() == 0 && timestampUpdater != null) {
                    SafeDisposer.dispose(timestampUpdater);
                }
            }
        }
    };

    @Override
    public void disposeInner() {
        SafeDisposer.dispose(timestampUpdater);
        super.disposeInner();
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

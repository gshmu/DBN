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
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
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

    public void killSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, final boolean immediate) {
        final ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
        final DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
        new BackgroundTask(getProject(), "Killing Sessions", false, true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                Project project = connectionHandler.getProject();
                Connection connection = null;
                try {
                    connection = connectionHandler.getPoolConnection();
                    Map<Object, SQLException> errors = new HashMap<Object, SQLException>();
                    for (Object sessionId : sessionIds.keySet()) {
                        Object serialNumber = sessionIds.get(sessionId);
                        if (progressIndicator.isCanceled()) return;
                        try {
                            metadataInterface.killUserSession(sessionId, serialNumber, immediate, connection);
                        } catch (SQLException e) {
                            errors.put(sessionId, e);
                        }
                    }

                    if (sessionIds.size() == 1) {
                        Object sessionId = sessionIds.keySet().iterator().next();
                        if (errors.size() == 0) {
                            MessageUtil.showInfoDialog(project, "Info", "Session with id \"" + sessionId + "\" killed.");
                        } else {
                            SQLException exception = errors.get(sessionId);
                            MessageUtil.showErrorDialog(project, "Error killing session with id \"" + sessionId + "\"" , exception);
                        }
                    } else {
                        if (errors.size() == 0) {
                            MessageUtil.showInfoDialog(project, "Info", sessionIds.size() + " sessions killed.");
                        } else {
                            StringBuilder message = new StringBuilder("Could not kill one or more of the selected sessions:");
                            for (Object sessionId : sessionIds.keySet()) {
                                SQLException exception = errors.get(sessionId);
                                message.append("\n - session id ").append(sessionId).append(": ");
                                if (exception == null) message.append("killed"); else message.append(exception.getMessage().trim());

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

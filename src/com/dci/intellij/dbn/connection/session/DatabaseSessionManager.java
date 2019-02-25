package com.dci.intellij.dbn.connection.session;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.ui.CreateRenameSessionDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;

@State(
    name = DatabaseSessionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseSessionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseSessionManager";

    private DatabaseSessionManager(final Project project) {
        super(project);
    }

    public static DatabaseSessionManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseSessionManager.class);
    }

    public void showCreateSessionDialog(ConnectionHandler connectionHandler, @Nullable ParametricRunnable<DatabaseSession> callback) {
        showCreateRenameSessionDialog(connectionHandler, null, callback);
    }

    public void showRenameSessionDialog(@NotNull DatabaseSession session, @Nullable ParametricRunnable<DatabaseSession> callback) {
        showCreateRenameSessionDialog(session.getConnectionHandler(), session, callback);
    }


    private void showCreateRenameSessionDialog(ConnectionHandler connectionHandler, DatabaseSession session, @Nullable ParametricRunnable<DatabaseSession> callback) {
        Dispatch.invokeNonModal(() -> {
            CreateRenameSessionDialog dialog = session == null ?
                    new CreateRenameSessionDialog(connectionHandler) :
                    new CreateRenameSessionDialog(connectionHandler, session);
            dialog.setModal(true);
            dialog.show();
            if (callback != null) {
                callback.run(dialog.getSession());
            }
        });
    }

    public DatabaseSession createSession(ConnectionHandler connectionHandler, String name) {
        DatabaseSession session = connectionHandler.getSessionBundle().createSession(name);
        EventUtil.notify(getProject(), SessionManagerListener.TOPIC).sessionCreated(session);
        return session;
    }

    public void renameSession(DatabaseSession session, String newName) {
        ConnectionHandler connectionHandler = session.getConnectionHandler();
        String oldName = session.getName();
        connectionHandler.getSessionBundle().renameSession(oldName, newName);
        EventUtil.notify(getProject(), SessionManagerListener.TOPIC).sessionChanged(session);
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void deleteSession(final DatabaseSession session, boolean confirm) {
        if (confirm) {
            MessageUtil.showQuestionDialog(
                    getProject(),
                    "Delete Session",
                    "Are you sure you want to delete the session \"" + session.getName() + "\" for connection\"" + session.getConnectionHandler().getName() + "\"" ,
                    MessageUtil.OPTIONS_YES_NO, 0,
                    (option) -> conditional(option == 0,
                            () -> deleteSession(session)));
        } else {
            deleteSession(session);
        }
    }

    public void deleteSession(@NotNull DatabaseSession session) {
        ConnectionHandler connectionHandler = session.getConnectionHandler();
        connectionHandler.getSessionBundle().deleteSession(session.getId());
        EventUtil.notify(getProject(), SessionManagerListener.TOPIC).sessionDeleted(session);
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionBundle().getAllConnectionHandlers();
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            Element connectionElement = new Element("connection");
            element.addContent(connectionElement);
            connectionElement.setAttribute("id", connectionHandler.getConnectionId().id());

            List<DatabaseSession> sessions = connectionHandler.getSessionBundle().getSessions();
            for (DatabaseSession session : sessions) {
                if (session.isCustom()) {
                    Element sessionElement = new Element("session");
                    connectionElement.addContent(sessionElement);
                    sessionElement.setAttribute("name", session.getName());
                    sessionElement.setAttribute("id", session.getId().id());
                }
            }
        }
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        for (Element connectionElement : element.getChildren()) {
            ConnectionId connectionId = ConnectionId.get(connectionElement.getAttributeValue("id"));
            ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);

            if (connectionHandler != null) {
                DatabaseSessionBundle sessionBundle = connectionHandler.getSessionBundle();
                for (Element sessionElement : connectionElement.getChildren()) {
                    String sessionName = sessionElement.getAttributeValue("name");
                    SessionId sessionId = SessionId.get(sessionElement.getAttributeValue("id"));
                    sessionBundle.addSession(sessionId, sessionName);
                }
            }
        }
    }
}

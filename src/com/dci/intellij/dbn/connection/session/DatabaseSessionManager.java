package com.dci.intellij.dbn.connection.session;

import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.session.ui.CreateRenameSessionDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

@State(
        name = "DBNavigator.Project.DatabaseSessionManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DatabaseSessionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private DatabaseSessionManager(final Project project) {
        super(project);
    }

    public static DatabaseSessionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseSessionManager.class);
    }

    public void showCreateSessionDialog(ConnectionHandler connectionHandler) {
        showCreateRenameSessionDialog(connectionHandler, null);
    }

    public void showRenameSessionDialog(@NotNull DatabaseSession session) {
        showCreateRenameSessionDialog(session.getConnectionHandler(), session);
    }


    private void showCreateRenameSessionDialog(final ConnectionHandler connectionHandler, final DatabaseSession session) {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                CreateRenameSessionDialog dialog = session == null ?
                        new CreateRenameSessionDialog(connectionHandler) :
                        new CreateRenameSessionDialog(connectionHandler, session);
                dialog.setModal(true);
                dialog.show();
            }
        }.start();
    }

    public void createSession(ConnectionHandler connectionHandler, String name) {
        connectionHandler.getSessionBundle().createSession(name);
    }

    public void renameSession(DatabaseSession session, String newName) {
        ConnectionHandler connectionHandler = session.getConnectionHandler();
        String oldName = session.getName();
        connectionHandler.getSessionBundle().renameSession(oldName, newName);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseSessionManager";
    }

    public void deleteSession(final DatabaseSession session) {
        final Project project = getProject();
        MessageUtil.showQuestionDialog(
                project,
                "Delete session",
                "Are you sure you want to delete this session?",
                MessageUtil.OPTIONS_YES_NO, 0,
                new MessageCallback(0) {
                    @Override
                    protected void execute() {
                        ConnectionHandler connectionHandler = session.getConnectionHandler();
                        String sessionName = session.getName();
                        connectionHandler.getSessionBundle().removeSession(sessionName);
                    }
                });
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
            connectionElement.setAttribute("id", connectionHandler.getId().id());

            List<DatabaseSession> sessions = connectionHandler.getSessionBundle().getSessions();
            for (DatabaseSession session : sessions) {
                if (session.isCustom()) {
                    Element sessionElement = new Element("session");
                    connectionElement.addContent(sessionElement);
                    sessionElement.setAttribute("name", session.getName());
                }
            }
        }
        return element;
    }

    @Override
    public void loadState(Element element) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        for (Element connectionElement : element.getChildren()) {
            ConnectionId connectionId = ConnectionId.get(connectionElement.getAttributeValue("id"));
            ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);

            if (connectionHandler != null) {
                DatabaseSessionBundle sessionBundle = connectionHandler.getSessionBundle();
                for (Element sessionElement : connectionElement.getChildren()) {
                    String sessionName = sessionElement.getAttributeValue("name");
                    DatabaseSession session = sessionBundle.getSession(sessionName, true);
                }
            }
        }
    }
}

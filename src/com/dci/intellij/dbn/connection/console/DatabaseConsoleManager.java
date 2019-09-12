package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.action.UserDataKeys;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.console.ui.CreateRenameConsoleDialog;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.dci.intellij.dbn.connection.session.SessionManagerListener;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.util.EventDispatcher;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;

@State(
    name = DatabaseConsoleManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseConsoleManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseConsoleManager";

    private final EventDispatcher<VirtualFileListener> eventDispatcher = EventDispatcher.create(VirtualFileListener.class);

    private DatabaseConsoleManager(final Project project) {
        super(project);
        EventUtil.subscribe(getProject(), this, SessionManagerListener.TOPIC, sessionManagerListener);
    }

    public static DatabaseConsoleManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseConsoleManager.class);
    }

    public void showCreateConsoleDialog(ConnectionHandler connectionHandler, DBConsoleType consoleType) {
        showCreateRenameConsoleDialog(connectionHandler, null, consoleType);
    }

    public void showRenameConsoleDialog(@NotNull DBConsoleVirtualFile consoleVirtualFile) {
        showCreateRenameConsoleDialog(
                consoleVirtualFile.getConnectionHandler(), consoleVirtualFile, consoleVirtualFile.getType());
    }


    private void showCreateRenameConsoleDialog(final ConnectionHandler connectionHandler, final DBConsoleVirtualFile consoleVirtualFile, final DBConsoleType consoleType) {
        Dispatch.run(() -> {
            CreateRenameConsoleDialog createConsoleDialog = consoleVirtualFile == null ?
                    new CreateRenameConsoleDialog(connectionHandler, consoleType) :
                    new CreateRenameConsoleDialog(connectionHandler, consoleVirtualFile);
            createConsoleDialog.setModal(true);
            createConsoleDialog.show();
        });
    }

    public void createConsole(ConnectionHandler connectionHandler, String name, DBConsoleType type) {
        DBConsole console = connectionHandler.getConsoleBundle().createConsole(name, type);
        DBConsoleVirtualFile consoleFile = console.getVirtualFile();
        consoleFile.setText("");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(consoleFile, true);
        VirtualFileEvent fileEvent = new VirtualFileEvent(this, consoleFile, name, null);
        eventDispatcher.getMulticaster().fileCreated(fileEvent);
    }

    public void renameConsole(DBConsoleVirtualFile consoleFile, String newName) {
        ConnectionHandler connectionHandler = consoleFile.getConnectionHandler();
        String oldName = consoleFile.getName();
        connectionHandler.getConsoleBundle().renameConsole(oldName, newName);
        VirtualFilePropertyEvent event = new VirtualFilePropertyEvent(this, consoleFile, VirtualFile.PROP_NAME, oldName, newName);
        eventDispatcher.getMulticaster().propertyChanged(event);
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void deleteConsole(final DBConsoleVirtualFile consoleFile) {
        final Project project = getProject();
        MessageUtil.showQuestionDialog(
                project,
                "Delete console",
                "You will loose the information contained in this console.\n" +
                        "Are you sure you want to delete the console?",
                MessageUtil.OPTIONS_YES_NO, 0,
                (option) -> conditional(option == 0,
                        () -> {
                            DatabaseFileManager.getInstance(project).closeFile(consoleFile);
                            ConnectionHandler connectionHandler = consoleFile.getConnectionHandler();
                            String fileName = consoleFile.getName();
                            connectionHandler.getConsoleBundle().removeConsole(fileName);
                            VirtualFileEvent fileEvent = new VirtualFileEvent(this, consoleFile, fileName, null);
                            eventDispatcher.getMulticaster().fileDeleted(fileEvent);
                        }));
    }


    /***************************************
     *         SessionManagerListener      *
     ***************************************/
    private SessionManagerListener sessionManagerListener = new SessionManagerListener() {
        @Override
        public void sessionCreated(DatabaseSession session) {}

        @Override
        public void sessionDeleted(DatabaseSession session) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionBundle().getAllConnectionHandlers();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                List<DBConsole> consoles = connectionHandler.getConsoleBundle().getConsoles();
                for (DBConsole console : consoles) {
                    DBConsoleVirtualFile virtualFile = console.getVirtualFile();
                    if (virtualFile.getDatabaseSession() == session) {
                        DatabaseSession mainSession = connectionHandler.getSessionBundle().getMainSession();
                        virtualFile.setDatabaseSession(mainSession);
                    }
                }
            }
        }

        @Override
        public void sessionChanged(DatabaseSession session) {}
    };

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

            List<DBConsole> consoles = connectionHandler.getConsoleBundle().getConsoles();
            for (DBConsole console : consoles) {
                DBConsoleVirtualFile virtualFile = console.getVirtualFile();
                Element consoleElement = new Element("console");
                connectionElement.addContent(consoleElement);

                DatabaseSession databaseSession = CommonUtil.nvl(
                        virtualFile.getDatabaseSession(),
                        connectionHandler.getSessionBundle().getMainSession());

                consoleElement.setAttribute("name", console.getName());
                consoleElement.setAttribute("type", console.getConsoleType().name());
                consoleElement.setAttribute("schema", CommonUtil.nvl(virtualFile.getDatabaseSchemaName(), ""));
                consoleElement.setAttribute("session", databaseSession.getName());
                consoleElement.addContent(new CDATA(virtualFile.getContent().exportContent()));
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
                DatabaseConsoleBundle consoleBundle = connectionHandler.getConsoleBundle();
                for (Element consoleElement : connectionElement.getChildren()) {
                    String consoleName = consoleElement.getAttributeValue("name");

                    // schema
                    String schema = consoleElement.getAttributeValue("schema");

                    // session
                    String session = consoleElement.getAttributeValue("session");
                    DatabaseSessionBundle sessionBundle = connectionHandler.getSessionBundle();
                    DatabaseSession databaseSession = StringUtil.isEmpty(session) ?
                            sessionBundle.getMainSession() :
                            sessionBundle.getSession(session);


                    DBConsoleType consoleType = SettingsSupport.getEnumAttribute(consoleElement, "type", DBConsoleType.class);

                    String consoleText = SettingsSupport.readCdata(consoleElement);

                    DBConsole console = consoleBundle.getConsole(consoleName, consoleType, true);
                    DBConsoleVirtualFile virtualFile = console.getVirtualFile();
                    virtualFile.putUserData(UserDataKeys.CONSOLE_TEXT, consoleText);
                    //virtualFile.setText(consoleText);
                    virtualFile.setDatabaseSchemaName(schema);
                    virtualFile.setDatabaseSession(databaseSession);
                }
            }
        }
    }

    @Override
    public void projectOpened() {
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        for (ConnectionHandler connectionHandler : connectionManager.getConnectionHandlers()) {
            for (DBConsole console : connectionHandler.getConsoleBundle().getConsoles()) {
                DBConsoleVirtualFile virtualFile = console.getVirtualFile();
                String text = virtualFile.getUserData(UserDataKeys.CONSOLE_TEXT);
                virtualFile.putUserData(UserDataKeys.CONSOLE_TEXT, null);
                virtualFile.setText(text);
            }
        }

        super.projectOpened();
    }
}

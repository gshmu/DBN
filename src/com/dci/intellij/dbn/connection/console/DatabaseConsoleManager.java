package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.console.ui.CreateRenameConsoleDialog;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
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

@State(
    name = DatabaseConsoleManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseConsoleManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseConsoleManager";

    private final EventDispatcher<VirtualFileListener> eventDispatcher = EventDispatcher.create(VirtualFileListener.class);

    private DatabaseConsoleManager(final Project project) {
        super(project);
    }

    public static DatabaseConsoleManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseConsoleManager.class);
    }

    public void showCreateConsoleDialog(ConnectionHandler connectionHandler, DBConsoleType consoleType) {
        showCreateRenameConsoleDialog(connectionHandler, null, consoleType);
    }

    public void showRenameConsoleDialog(@NotNull DBConsoleVirtualFile consoleVirtualFile) {
        showCreateRenameConsoleDialog(
                consoleVirtualFile.getConnectionHandler(), consoleVirtualFile, consoleVirtualFile.getType());
    }


    private void showCreateRenameConsoleDialog(final ConnectionHandler connectionHandler, final DBConsoleVirtualFile consoleVirtualFile, final DBConsoleType consoleType) {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                CreateRenameConsoleDialog createConsoleDialog = consoleVirtualFile == null ?
                        new CreateRenameConsoleDialog(connectionHandler, consoleType) :
                        new CreateRenameConsoleDialog(connectionHandler, consoleVirtualFile);
                createConsoleDialog.setModal(true);
                createConsoleDialog.show();
            }
        }.start();
    }

    public void createConsole(ConnectionHandler connectionHandler, String name, DBConsoleType type) {
        DBConsoleVirtualFile consoleFile = connectionHandler.getConsoleBundle().createConsole(name, type);
        consoleFile.setText("");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(consoleFile, true);
        eventDispatcher.getMulticaster().fileCreated(new VirtualFileEvent(this, consoleFile, name, null));
    }

    public void renameConsole(DBConsoleVirtualFile consoleFile, String newName) {
        ConnectionHandler connectionHandler = consoleFile.getConnectionHandler();
        String oldName = consoleFile.getName();
        connectionHandler.getConsoleBundle().renameConsole(oldName, newName);
        VirtualFilePropertyEvent event = new VirtualFilePropertyEvent(this, consoleFile, VirtualFile.PROP_NAME, oldName, newName);
        eventDispatcher.getMulticaster().propertyChanged(event);
    }

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
                new MessageCallback(0) {
                    @Override
                    protected void execute() {
                        FileEditorManager.getInstance(project).closeFile(consoleFile);
                        ConnectionHandler connectionHandler = consoleFile.getConnectionHandler();
                        String fileName = consoleFile.getName();
                        connectionHandler.getConsoleBundle().removeConsole(fileName);
                        eventDispatcher.getMulticaster().fileDeleted(new VirtualFileEvent(this, consoleFile, fileName, null));
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

            List<DBConsoleVirtualFile> consoles = connectionHandler.getConsoleBundle().getConsoles();
            for (DBConsoleVirtualFile console : consoles) {
                Element consoleElement = new Element("console");
                connectionElement.addContent(consoleElement);

                consoleElement.setAttribute("name", console.getName());
                consoleElement.setAttribute("type", console.getType().name());
                consoleElement.addContent(new CDATA(console.getContent().exportContent()));
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
                DatabaseConsoleBundle consoleBundle = connectionHandler.getConsoleBundle();
                for (Element consoleElement : connectionElement.getChildren()) {
                    String consoleName = consoleElement.getAttributeValue("name");
                    DBConsoleType consoleType = SettingsUtil.getEnumAttribute(consoleElement, "type", DBConsoleType.class);

                    String consoleText = SettingsUtil.readCdata(consoleElement);

                    DBConsoleVirtualFile consoleVirtualFile = consoleBundle.getConsole(consoleName, consoleType, true);
                    consoleVirtualFile.setText(consoleText);
                }
            }
        }
    }
}

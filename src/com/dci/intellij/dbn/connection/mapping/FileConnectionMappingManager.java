package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.dci.intellij.dbn.connection.session.SessionManagerListener;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.action.DBNDataKeys.*;

@State(
    name = FileConnectionMappingManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class FileConnectionMappingManager extends AbstractProjectComponent implements ProjectComponent, PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.FileConnectionMappingManager";

    private Set<FileConnectionMapping> mappings = new THashSet<>();

    private FileConnectionMappingManager(Project project) {
        super(project);
        VirtualFileManager.getInstance().addVirtualFileListener(virtualFileListener);
        EventUtil.subscribe(getProject(), this, SessionManagerListener.TOPIC, sessionManagerListener);
    }

    @NotNull
    public static FileConnectionMappingManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, FileConnectionMappingManager.class);
    }

    @Override
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }


    public boolean setConnectionHandler(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
        ConnectionHandlerRef connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);

        if (virtualFile instanceof LightVirtualFile) {
            virtualFile.putUserData(CONNECTION_HANDLER, connectionHandlerRef);
            return true;
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(CONNECTION_HANDLER, connectionHandlerRef);

            ConnectionId connectionId = connectionHandler == null ? null : connectionHandler.getConnectionId();
            SchemaId currentSchema = connectionHandler == null ? null  : connectionHandler.getUserSchema();

            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping == null) {
                mapping = new FileConnectionMapping(virtualFile.getUrl(), connectionId, SessionId.MAIN, currentSchema);
                mappings.add(mapping);
                return true;
            } else {
                if (mapping.getConnectionId() == null || mapping.getConnectionId() != connectionId) {
                    mapping.setConnectionId(connectionId);
                    mapping.setSessionId(SessionId.MAIN);
                    if (connectionHandler != null) {
                        // overwrite current schema only if the existing
                        // selection is not a valid schema for the given connection
                        currentSchema = mapping.getSchemaId();
                        setDatabaseSchema(virtualFile, currentSchema);
                    } else {
                        setDatabaseSchema(virtualFile, null);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setDatabaseSchema(VirtualFile virtualFile, SchemaId schemaId) {
        if (virtualFile instanceof LightVirtualFile) {
            virtualFile.putUserData(DATABASE_SCHEMA, schemaId);
            return true;
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setDatabaseSchema(schemaId);
            return true;
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(DATABASE_SCHEMA, schemaId);
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping != null) {
                if (schemaId == null) {
                    mapping.setSchemaId(null);
                } else if (!schemaId.equals(mapping.getSchemaId())){
                    mapping.setSchemaId(schemaId);
                }

                return true;
            }
        }

        return false;
    }

    public boolean setDatabaseSession(VirtualFile virtualFile, DatabaseSession session) {
        if (virtualFile instanceof LightVirtualFile) {
            virtualFile.putUserData(DATABASE_SESSION, session);
            return true;
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(DATABASE_SESSION, session);
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping != null) {
                if (session == null) {
                    mapping.setSessionId(null);
                } else if (session.getId() != mapping.getSessionId()){
                    mapping.setSessionId(session.getId());
                }

                return true;
            }
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setDatabaseSession(session);
            return true;
        }

        return false;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler(@NotNull VirtualFile virtualFile) {
        return Failsafe.lenient(null, () -> {
            Project project = getProject();
            if (virtualFile instanceof LightVirtualFile) {
                ConnectionHandlerRef connectionHandlerRef = virtualFile.getUserData(CONNECTION_HANDLER);
                if (connectionHandlerRef == null) {
                    LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
                    VirtualFile originalFile = lightVirtualFile.getOriginalFile();
                    if (originalFile != null && !originalFile.equals(virtualFile)) {
                        return getConnectionHandler(originalFile);
                    }
                }
                return ConnectionHandlerRef.get(connectionHandlerRef);
            }

            // if the file is a database content file then get the connection from the underlying database object
            if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
                if (virtualFile instanceof FileConnectionMappingProvider) {
                    FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                    return connectionMappingProvider.getConnectionHandler();
                }
            }

            if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
                // if the file is an attached ddl file, then resolve the object which it is
                // linked to, and return its database connection
                DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
                if (schemaObject != null) {
                    ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                    if (DatabaseFileSystem.isFileOpened(schemaObject)) {
                        return connectionHandler;
                    }
                }

                // lookup connection mappings
                ConnectionHandlerRef connectionHandlerRef = virtualFile.getUserData(CONNECTION_HANDLER);
                if (connectionHandlerRef == null) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                        ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(mapping.getConnectionId());
                        if (connectionHandler == null) {
                            ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
                            connectionHandler = connectionBundle.getVirtualConnection(mapping.getConnectionId());
                        }
                        connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);

                        virtualFile.putUserData(CONNECTION_HANDLER, connectionHandlerRef);

                    }
                }
                return ConnectionHandlerRef.get(connectionHandlerRef);
            }
            return null;
        });
    }

    @Nullable
    public SchemaId getDatabaseSchema(@NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            SchemaId schemaId = virtualFile.getUserData(DATABASE_SCHEMA);
            if (schemaId == null) {
                LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
                VirtualFile originalFile = lightVirtualFile.getOriginalFile();
                if (originalFile != null && !originalFile.equals(virtualFile)) {
                    return getDatabaseSchema(originalFile);
                }
            }
            return schemaId;
        }

        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getSchemaId();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its parent schema
            Project project = getProject();
            DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
            DBSchemaObject schemaObject = fileAttachmentManager.getEditableObject(virtualFile);
            if (schemaObject != null && DatabaseFileSystem.isFileOpened(schemaObject)) {
                return schemaObject.getSchemaIdentifier();
            }

            // lookup schema mappings
            SchemaId currentSchema = virtualFile.getUserData(DATABASE_SCHEMA);
            if (currentSchema == null) {
                ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        currentSchema = mapping.getSchemaId();
                        if (currentSchema == null) {
                            currentSchema = connectionHandler.getDefaultSchema();
                        }
                        mapping.setSchemaId(currentSchema);
                        virtualFile.putUserData(DATABASE_SCHEMA, currentSchema);
                    }
                }
            } else {
                return currentSchema;
            }
        }
        return null;
    }

    @Nullable
    public DatabaseSession getDatabaseSession(@NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            DatabaseSession databaseSession = virtualFile.getUserData(DATABASE_SESSION);
            if (databaseSession == null) {
                LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
                VirtualFile originalFile = lightVirtualFile.getOriginalFile();
                if (originalFile != null && !originalFile.equals(virtualFile)) {
                    return getDatabaseSession(originalFile);
                }
            }
            return databaseSession;
        }

        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getDatabaseSession();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            // lookup schema mappings
            DatabaseSession databaseSession = virtualFile.getUserData(DATABASE_SESSION);
            if (databaseSession == null) {
                ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        SessionId sessionId = mapping.getSessionId();
                        databaseSession = connectionHandler.getSessionBundle().getSession(sessionId);
                        virtualFile.putUserData(DATABASE_SESSION, databaseSession);
                    }
                }
            }
            return databaseSession;
        }
        return null;
    }


    @Nullable
    public DBNConnection getConnection(@NotNull VirtualFile virtualFile) {
        ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
        if (connectionHandler != null) {
            DatabaseSession databaseSession = getDatabaseSession(virtualFile);
            if (databaseSession != null) {
                return connectionHandler.getConnectionPool().getSessionConnection(databaseSession.getId());
            }
        }
        return null;
    }

    private FileConnectionMapping lookupMapping(VirtualFile virtualFile) {
        String fileUrl = virtualFile.getUrl();
        return lookupMapping(fileUrl);
    }

    private FileConnectionMapping lookupMapping(String fileUrl) {
        for (FileConnectionMapping mapping : mappings) {
            if (fileUrl.equals(mapping.getFileUrl())) {
                return mapping;
            }
        }
        return null;
    }

    private void removeMapping(VirtualFile virtualFile) {
        FileConnectionMapping mapping = lookupMapping(virtualFile);
        if (mapping != null) {
            mappings.remove(mapping);
        }
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connectionHandler) {
        List<VirtualFile> list = new ArrayList<>();

        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        for (FileConnectionMapping mapping : mappings) {
            ConnectionId connectionId = mapping.getConnectionId();
            if (connectionHandler.getConnectionId() == connectionId) {
                VirtualFile file = localFileSystem.findFileByPath(mapping.getFileUrl());
                if (file != null) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public void setConnectionHandler(@NotNull Editor editor, @Nullable ConnectionHandler connectionHandler) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (isConnectionSelectable(virtualFile)) {
            boolean changed = setConnectionHandler(virtualFile, connectionHandler);
            if (changed) {
                DocumentUtil.touchDocument(editor, true);

                FileConnectionMappingListener mappingListener = EventUtil.notify(getProject(), FileConnectionMappingListener.TOPIC);
                mappingListener.connectionChanged(virtualFile, connectionHandler);
            }
        }
    }

    public void setDatabaseSchema(@NotNull Editor editor, SchemaId schema) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (isSchemaSelectable(virtualFile)) {
            boolean changed = setDatabaseSchema(virtualFile, schema);
            if (changed) {
                DocumentUtil.touchDocument(editor, false);

                FileConnectionMappingListener mappingListener = EventUtil.notify(getProject(), FileConnectionMappingListener.TOPIC);
                mappingListener.schemaChanged(virtualFile, schema);
            }
        }
    }

    public void setDatabaseSession(@NotNull Editor editor, DatabaseSession session) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (isSessionSelectable(virtualFile)) {
            setDatabaseSession(virtualFile, session);

            FileConnectionMappingListener mappingListener = EventUtil.notify(getProject(), FileConnectionMappingListener.TOPIC);
            mappingListener.sessionChanged(virtualFile, session);
        }
    }

    public boolean isConnectionSelectable(VirtualFile virtualFile) {
        return virtualFile != null &&
                virtualFile.getFileType() instanceof DBLanguageFileType &&
                (VirtualFileUtil.isLocalFileSystem(virtualFile) ||
                        virtualFile instanceof LightVirtualFile);
    }

    public boolean isSchemaSelectable(VirtualFile virtualFile) {
        return virtualFile != null &&
                virtualFile.getFileType() instanceof DBLanguageFileType &&
                (VirtualFileUtil.isLocalFileSystem(virtualFile) ||
                        virtualFile instanceof LightVirtualFile ||
                        virtualFile instanceof DBConsoleVirtualFile);
    }

    public boolean isSessionSelectable(VirtualFile virtualFile) {
        return virtualFile != null &&
                virtualFile.getFileType() instanceof DBLanguageFileType &&
                (VirtualFileUtil.isLocalFileSystem(virtualFile) ||
                        virtualFile instanceof LightVirtualFile ||
                        virtualFile instanceof DBConsoleVirtualFile);
    }


    public void selectConnectionAndSchema(@NotNull DBLanguagePsiFile file, @NotNull ConnectionAction callback) {
        SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
            Project project = getProject();
            ConnectionHandler activeConnection = file.getConnectionHandler();
            if (activeConnection == null || activeConnection.isVirtual()) {
                String message =
                        activeConnection == null ?
                                "The file is not linked to any connection.\nTo continue with the statement execution please select a target connection." :
                                "The connection you selected for this file is a virtual connection, used only to decide the SQL dialect.\n" +
                                        "You can not execute statements against this connection. Please select a proper connection to continue.";


                MessageUtil.showWarningDialog(project,
                        "No valid connection", message,
                        new String[]{"Select Connection", "Cancel"}, 0,
                        MessageCallback.create(0, option ->
                                promptConnectionSelector(file, false, true, true, callback)));

            } else if (file.getSchemaId() == null) {
                String message =
                        "You did not select any schema to run the statement against.\n" +
                                "To continue with the statement execution please select a schema.";
                MessageUtil.showWarningDialog(project,
                        "No schema selected", message,
                        new String[]{"Use Current Schema", "Select Schema", "Cancel"}, 0,
                        MessageCallback.create(null, option -> {
                            if (option == 0) {
                                callback.start();
                            } else if (option == 1) {
                                promptSchemaSelector(file, callback);
                            }
                        }));
            } else {
                callback.start();
            }
        });
    }

    /***************************************************
     *             Select connection popup             *
     ***************************************************/
    public void promptConnectionSelector(DBLanguagePsiFile psiFile, boolean showVirtualConnections, boolean showCreateOption, boolean promptSchemaSelection, SimpleTask callback) {
        Project project = getProject();
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        FiltrableList<ConnectionHandler> connectionHandlers = connectionBundle.getConnectionHandlers();

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionHandlers.size() > 0) {
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                ConnectionSelectAction connectionAction = new ConnectionSelectAction(connectionHandler, psiFile, promptSchemaSelection, callback);
                actionGroup.add(connectionAction);
            }
        }

        if (showVirtualConnections) {
            actionGroup.addSeparator();
            for (ConnectionHandler virtualConnectionHandler : connectionBundle.getVirtualConnections()) {
                ConnectionSelectAction connectionAction = new ConnectionSelectAction(virtualConnectionHandler, psiFile, promptSchemaSelection, callback);
                actionGroup.add(connectionAction);
            }
        }

        if (showCreateOption) {
            actionGroup.addSeparator();
            actionGroup.add(new ConnectionSetupAction());
        }

        ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                "Select Connection",
                actionGroup,
                SimpleDataContext.getProjectContext(null),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true,
                null,
                1000,
                action -> {
                    if (action instanceof ConnectionSelectAction) {
                        ConnectionSelectAction connectionSelectAction = (ConnectionSelectAction) action;
                        return connectionSelectAction.isSelected();
                    }
                    return false;
                },
                null);

        popupBuilder.showCenteredInCurrentWindow(project);
    }


    private class ConnectionSelectAction extends AbstractConnectionAction {
        private PsiFileRef<DBLanguagePsiFile> fileRef;
        private SimpleTask callback;
        private boolean promptSchemaSelection;

        private ConnectionSelectAction(ConnectionHandler connectionHandler, DBLanguagePsiFile file, boolean promptSchemaSelection, SimpleTask callback) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon(), connectionHandler);
            this.fileRef = PsiFileRef.from(file);
            this.callback = callback;
            this.promptSchemaSelection = promptSchemaSelection;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DBLanguagePsiFile file = fileRef.get();

            if (file != null) {
                file.setConnectionHandler(connectionHandler);
                if (promptSchemaSelection) {
                    promptSchemaSelector(file, callback);
                } else {
                    if (file.getSchemaId() == null) {
                        SchemaId defaultSchema = connectionHandler.getDefaultSchema();
                        file.setDatabaseSchema(defaultSchema);
                    }
                    if (callback != null) {
                        callback.start();
                    }
                }

            }
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                ConnectionHandler connectionHandler = file.getConnectionHandler();
                return connectionHandler != null && connectionHandler.getConnectionId().equals(getConnectionHandler().getConnectionId());
            }
            return false;

        }
    }

    private static class ConnectionSetupAction extends AnAction {
        private ConnectionSetupAction() {
            super("Setup New Connection", null, Icons.CONNECTION_NEW);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.ensureProject(e);
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSchemaSelector(final DBLanguagePsiFile psiFile, final RunnableTask callback) throws IncorrectOperationException {
        ConnectionAction.invoke("selecting the current schema", psiFile, (Integer) null, action -> {
            DefaultActionGroup actionGroup = new DefaultActionGroup();

            ConnectionHandler connectionHandler = action.getConnectionHandler();
            if (!connectionHandler.isVirtual() && !connectionHandler.isDisposed()) {
                List<DBSchema> schemas = connectionHandler.getObjectBundle().getSchemas();
                for (DBSchema schema  :schemas) {
                    SchemaSelectAction schemaAction = new SchemaSelectAction(psiFile, schema, callback);
                    actionGroup.add(schemaAction);
                }
            }

            ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Select Schema",
                    actionGroup,
                    SimpleDataContext.getProjectContext(null),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true,
                    null,
                    1000,
                    anAction -> {
                        if (anAction instanceof SchemaSelectAction) {
                            SchemaSelectAction schemaSelectAction = (SchemaSelectAction) anAction;
                            return schemaSelectAction.isSelected();
                        }
                        return false;
                    },
                    null);

            popupBuilder.showCenteredInCurrentWindow(getProject());
        });
    }


    private static class SchemaSelectAction extends AnObjectAction<DBSchema> {
        private PsiFileRef<DBLanguagePsiFile> fileRef;
        private RunnableTask callback;

        private SchemaSelectAction(DBLanguagePsiFile file, DBSchema schema, RunnableTask callback) {
            super(schema);
            this.fileRef = PsiFileRef.from(file);
            this.callback = callback;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                file.setDatabaseSchema(getSchema());
                if (callback != null) {
                    callback.start();
                }
            }
        }

        @Nullable
        public SchemaId getSchema() {
            return SchemaId.from(getObject());
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                SchemaId fileSchema = file.getSchemaId();
                return fileSchema != null && fileSchema.equals(getSchema());
            }
            return false;
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSessionSelector(final DBLanguagePsiFile psiFile, final RunnableTask callback) throws IncorrectOperationException {
        ConnectionAction.invoke("selecting the current session", psiFile, (Integer) null, action -> {
            DefaultActionGroup actionGroup = new DefaultActionGroup();

            ConnectionHandler connectionHandler = action.getConnectionHandler();
            if (!connectionHandler.isVirtual() && !connectionHandler.isDisposed()) {
                List<DatabaseSession> sessions = connectionHandler.getSessionBundle().getSessions();
                for (DatabaseSession session : sessions) {
                    SessionSelectAction sessionAction = new SessionSelectAction(psiFile, session, callback);
                    actionGroup.add(sessionAction);
                }
                actionGroup.addSeparator();
                actionGroup.add(new SessionCreateAction(psiFile, connectionHandler));
            }

            ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Select Session",
                    actionGroup,
                    SimpleDataContext.getProjectContext(null),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true,
                    null,
                    1000,
                    conditionAction -> {
                        if (conditionAction instanceof SessionSelectAction) {
                            SessionSelectAction sessionSelectAction = (SessionSelectAction) conditionAction;
                            return sessionSelectAction.isSelected();
                        }
                        return false;
                    },
                    null);

            popupBuilder.showCenteredInCurrentWindow(getProject());

        });
    }


    private class SessionSelectAction extends AnAction {
        private PsiFileRef<DBLanguagePsiFile> fileRef;
        private WeakRef<DatabaseSession> sessionRef;
        private RunnableTask callback;

        private SessionSelectAction(DBLanguagePsiFile file, DatabaseSession session, RunnableTask callback) {
            super(session.getName(), null, session.getIcon());
            this.fileRef = PsiFileRef.from(file);
            this.sessionRef = WeakRef.from(session);
            this.callback = callback;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBLanguagePsiFile file = fileRef.get();
            DatabaseSession session = sessionRef.get();
            if (file != null && session != null) {
                file.setDatabaseSession(session);
                if (callback != null) {
                    callback.start();
                }
            }
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                DatabaseSession fileSession = file.getDatabaseSession();
                return fileSession != null && fileSession.equals(sessionRef.get());
            }
            return false;
        }
    }

    private class SessionCreateAction extends DumbAwareAction {
        private PsiFileRef<DBLanguagePsiFile> fileRef;
        private ConnectionHandlerRef connectionHandlerRef;

        private SessionCreateAction(DBLanguagePsiFile file, ConnectionHandler connectionHandler) {
            super("New session...");
            this.fileRef = PsiFileRef.from(file);
            this.connectionHandlerRef = connectionHandler.getRef();
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            final DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                Project project = getProject();
                DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
                ConnectionHandler connectionHandler = connectionHandlerRef.getnn();
                sessionManager.showCreateSessionDialog(
                        connectionHandler,
                        SimpleTask.create(session -> {
                            if (session != null) {
                                file.setDatabaseSession(session);
                            }
                        }));
            }
        }
    }

    /***************************************
     *         VirtualFileListener         *
     ***************************************/
    private VirtualFileListener virtualFileListener = new VirtualFileListener() {
        @Override
        public void fileDeleted(@NotNull VirtualFileEvent event) {
            removeMapping(event.getFile());
        }

        @Override
        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            String oldFileUrl = event.getOldParent().getUrl() + "/" + event.getFileName();
            FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
            if (fileConnectionMapping != null) {
                fileConnectionMapping.setFileUrl(event.getFile().getUrl());
            }
        }

        @Override
        public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
            VirtualFile file = event.getFile();
            VirtualFile parent = file.getParent();
            if (file.isInLocalFileSystem() && parent != null) {
                String oldFileUrl = parent.getUrl() + "/" + event.getOldValue();
                FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
                if (fileConnectionMapping != null) {
                    fileConnectionMapping.setFileUrl(file.getUrl());
                }
            }
        }
    };

    /***************************************
     *         SessionManagerListener      *
     ***************************************/
    private SessionManagerListener sessionManagerListener = new SessionManagerListener() {
        @Override
        public void sessionCreated(DatabaseSession session) {}

        @Override
        public void sessionDeleted(DatabaseSession session) {
            for (FileConnectionMapping mapping : mappings) {
                if (session.getId() == mapping.getSessionId()) {
                    mapping.setSessionId(SessionId.MAIN);
                }
            }
        }

        @Override
        public void sessionChanged(DatabaseSession session) {}
    };

    /***************************************
     *          ProjectComponent         *
     ***************************************/
    @Override
    public void projectOpened() {}
    @Override
    public void projectClosed() {}
    @Override
    public void initComponent() {}
    @Override
    public void disposeComponent() {
        super.disposeComponent();
        mappings.clear();
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        for (FileConnectionMapping mapping : mappings) {
            Element mappingElement = new Element("mapping");
            mapping.writeState(mappingElement);
            element.addContent(mappingElement);
        }
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        for (Object child : element.getChildren()) {
            Element mappingElement = (Element) child;
            FileConnectionMapping mapping = new FileConnectionMapping();
            mapping.readState(mappingElement);

            VirtualFile virtualFile = virtualFileManager.findFileByUrl(mapping.getFileUrl());
            if (virtualFile != null && virtualFile.isValid()) {
                mappings.add(mapping);
            }
        }
    }
}


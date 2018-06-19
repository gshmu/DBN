package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.dci.intellij.dbn.connection.session.SessionManagerListener;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.editor.ui.DBLanguageFileEditorToolbarForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.action.DBNDataKeys.CONNECTION_HANDLER;
import static com.dci.intellij.dbn.common.action.DBNDataKeys.DATABASE_SCHEMA;
import static com.dci.intellij.dbn.common.action.DBNDataKeys.DATABASE_SESSION;

@State(
    name = FileConnectionMappingManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class FileConnectionMappingManager extends AbstractProjectComponent implements ProjectComponent, PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.FileConnectionMappingManager";

    private Set<FileConnectionMapping> mappings = new THashSet<FileConnectionMapping>();

    private FileConnectionMappingManager(Project project) {
        super(project);
        VirtualFileManager.getInstance().addVirtualFileListener(virtualFileListener);
        EventUtil.subscribe(getProject(), this, SessionManagerListener.TOPIC, sessionManagerListener);
    }

    @NotNull
    public static FileConnectionMappingManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, FileConnectionMappingManager.class);
    }

    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }


    public boolean setConnectionHandler(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(CONNECTION_HANDLER, connectionHandler);

            ConnectionId connectionId = connectionHandler == null ? null : connectionHandler.getId();
            String currentSchema = connectionHandler == null ? null  : connectionHandler.getUserName().toUpperCase();

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
                        String currentSchemaName = mapping.getSchemaName();
                        DBSchema schema = connectionHandler.isVirtual() || currentSchemaName == null ? null : connectionHandler.getObjectBundle().getSchema(currentSchemaName);
                        setDatabaseSchema(virtualFile, schema);
                    } else {
                        setDatabaseSchema(virtualFile, null);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setDatabaseSchema(VirtualFile virtualFile, DBSchema schema) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
            virtualFile.putUserData(DATABASE_SCHEMA, DBObjectRef.from(schema));
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping != null) {
                if (schema == null) {
                    mapping.setSchemaName(null);
                } else if (!schema.getName().equals(mapping.getSchemaName())){
                    mapping.setSchemaName(schema.getName());
                }

                return true;
            }
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setDatabaseSchema(schema);
            return true;
        }
        return false;
    }

    public boolean setDatabaseSession(VirtualFile virtualFile, DatabaseSession session) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
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
        try {
            Project project = getProject();
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
                ConnectionHandler connectionHandler = virtualFile.getUserData(CONNECTION_HANDLER);
                if (connectionHandler == null || connectionHandler.isDisposed()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                        connectionHandler = connectionManager.getConnectionHandler(mapping.getConnectionId());
                        if (connectionHandler == null) {
                            ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
                            connectionHandler = connectionBundle.getVirtualConnection(mapping.getConnectionId());
                        }

                        if (connectionHandler != null)
                            virtualFile.putUserData(CONNECTION_HANDLER, connectionHandler);
                    }
                }
                return connectionHandler;
            }
        } catch (ProcessCanceledException e) {
            return null;
        }
        return null;
    }

    @Nullable
    public DBSchema getDatabaseSchema(@NotNull VirtualFile virtualFile) {
        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getDatabaseSchema();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its parent schema
            Project project = getProject();
            DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
            if (schemaObject != null && DatabaseFileSystem.isFileOpened(schemaObject)) {
                return schemaObject.getSchema();
            }

            // lookup schema mappings
            DBObjectRef<DBSchema> currentSchemaRef = virtualFile.getUserData(DATABASE_SCHEMA);
            if (currentSchemaRef == null) {
                ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        String schemaName = mapping.getSchemaName();
                        if (StringUtil.isEmptyOrSpaces(schemaName)) {
                            DBSchema defaultSchema = connectionHandler.getDefaultSchema();
                            currentSchemaRef = defaultSchema == null ? null : defaultSchema.getRef();
                            schemaName = currentSchemaRef == null ? null : currentSchemaRef.getObjectName();
                        } else {
                            DBSchema schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                            currentSchemaRef = schema == null ? null : schema.getRef();
                        }
                        mapping.setSchemaName(schemaName);
                        virtualFile.putUserData(DATABASE_SCHEMA, currentSchemaRef);
                    }
                }
            } else {
                return currentSchemaRef.get();
            }
        }
        return null;
    }

    @Nullable
    public DatabaseSession getDatabaseSession(@NotNull VirtualFile virtualFile) {
        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getDatabaseSession();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {

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

    public void removeMapping(VirtualFile virtualFile) {
        FileConnectionMapping mapping = lookupMapping(virtualFile);
        if (mapping != null) {
            mappings.remove(mapping);
        }
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connectionHandler) {
        List<VirtualFile> list = new ArrayList<VirtualFile>();

        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        for (FileConnectionMapping mapping : mappings) {
            ConnectionId connectionId = mapping.getConnectionId();
            if (connectionHandler.getId() == connectionId) {
                VirtualFile file = virtualFileManager.findFileByUrl(mapping.getFileUrl());
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
        if (virtualFile != null && VirtualFileUtil.isLocalFileSystem(virtualFile) ) {
            boolean changed = setConnectionHandler(virtualFile, connectionHandler);
            if (changed) {
                DocumentUtil.touchDocument(editor, true);
                Project project = getProject();
                FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(virtualFile);
                if (fileEditor != null) {
                    DBLanguageFileEditorToolbarForm toolbarForm = fileEditor.getUserData(DBLanguageFileEditorToolbarForm.USER_DATA_KEY);
                    if (toolbarForm != null) {
                        toolbarForm.getAutoCommitLabel().setConnectionHandler(connectionHandler);
                    }
                }
            }
        }
    }

    public void setDatabaseSchema(@NotNull Editor editor, DBSchema schema) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile != null && (VirtualFileUtil.isLocalFileSystem(virtualFile) || virtualFile instanceof DBConsoleVirtualFile)) {
            boolean changed = setDatabaseSchema(virtualFile, schema);
            if (changed) {
                DocumentUtil.touchDocument(editor, false);
            }
        }
    }

    public void setDatabaseSession(@NotNull Editor editor, DatabaseSession session) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile != null && (VirtualFileUtil.isLocalFileSystem(virtualFile) || virtualFile instanceof DBConsoleVirtualFile)) {
            setDatabaseSession(virtualFile, session);
        }
    }


    public void selectConnectionAndSchema(@NotNull final DBLanguagePsiFile file, @NotNull final ConnectionAction callback) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
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
                            new MessageCallback(0) {
                                @Override
                                protected void execute() {
                                    promptConnectionSelector(file, false, true, true, callback);
                                }
                            });

                } else if (file.getDatabaseSchema() == null) {
                    String message =
                            "You did not select any schema to run the statement against.\n" +
                            "To continue with the statement execution please select a schema.";
                    MessageUtil.showWarningDialog(project,
                            "No schema selected", message,
                            new String[]{"Use Current Schema", "Select Schema", "Cancel"}, 0,
                            new MessageCallback() {
                                @Override
                                protected void execute() {
                                    Integer result = getData();
                                    if (result == 0) {
                                        callback.start();
                                    } else if (result == 1) {
                                        promptSchemaSelector(file, callback);
                                    }
                                }
                            });
                } else {
                    callback.start();
                }
            }
        }.start();
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
                new Condition<AnAction>() {
                    @Override
                    public boolean value(AnAction anAction) {
                        if (anAction instanceof ConnectionSelectAction) {
                            ConnectionSelectAction connectionSelectAction = (ConnectionSelectAction) anAction;
                            return connectionSelectAction.isSelected();
                        }
                        return false;
                    }
                },
                null);

        popupBuilder.showCenteredInCurrentWindow(project);
    }


    private class ConnectionSelectAction extends AbstractConnectionAction {
        private WeakReference<DBLanguagePsiFile> fileRef;
        private SimpleTask callback;
        private boolean promptSchemaSelection = false;

        private ConnectionSelectAction(ConnectionHandler connectionHandler, DBLanguagePsiFile file, boolean promptSchemaSelection, SimpleTask callback) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon(), connectionHandler);
            this.fileRef = new WeakReference<DBLanguagePsiFile>(file);
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
                    if (file.getDatabaseSchema() == null) {
                        DBSchema defaultSchema = connectionHandler.getDefaultSchema();
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
                return connectionHandler != null && connectionHandler.getId().equals(getConnectionHandler().getId());
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
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
            }
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSchemaSelector(final DBLanguagePsiFile psiFile, final RunnableTask callback) throws IncorrectOperationException {
        new ConnectionAction("selecting the current schema", psiFile) {
            @Override
            protected void execute() {
                DefaultActionGroup actionGroup = new DefaultActionGroup();

                ConnectionHandler connectionHandler = getConnectionHandler();
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
                        new Condition<AnAction>() {
                            @Override
                            public boolean value(AnAction anAction) {
                                if (anAction instanceof SchemaSelectAction) {
                                    SchemaSelectAction schemaSelectAction = (SchemaSelectAction) anAction;
                                    return schemaSelectAction.isSelected();
                                }
                                return false;
                            }
                        },
                        null);

                popupBuilder.showCenteredInCurrentWindow(getProject());
            }
        }.start();
    }


    private static class SchemaSelectAction extends AnObjectAction<DBSchema> {
        private WeakReference<DBLanguagePsiFile> fileRef;
        private RunnableTask callback;

        private SchemaSelectAction(DBLanguagePsiFile file, DBSchema schema, RunnableTask callback) {
            super(schema);
            this.fileRef = new WeakReference<DBLanguagePsiFile>(file);
            this.callback = callback;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                file.setDatabaseSchema(getObject());
                if (callback != null) {
                    callback.start();
                }
            }
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                DBSchema fileSchema = file.getDatabaseSchema();
                return fileSchema != null && fileSchema.equals(getObject());
            }
            return false;
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSessionSelector(final DBLanguagePsiFile psiFile, final RunnableTask callback) throws IncorrectOperationException {
        new ConnectionAction("selecting the current session", psiFile) {
            @Override
            protected void execute() {
                DefaultActionGroup actionGroup = new DefaultActionGroup();

                ConnectionHandler connectionHandler = getConnectionHandler();
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
                        new Condition<AnAction>() {
                            @Override
                            public boolean value(AnAction anAction) {
                                if (anAction instanceof SessionSelectAction) {
                                    SessionSelectAction sessionSelectAction = (SessionSelectAction) anAction;
                                    return sessionSelectAction.isSelected();
                                }
                                return false;
                            }
                        },
                        null);

                popupBuilder.showCenteredInCurrentWindow(getProject());
            }
        }.start();
    }


    private class SessionSelectAction extends AnAction {
        private WeakReference<DBLanguagePsiFile> fileRef;
        private WeakReference<DatabaseSession> sessionRef;
        private RunnableTask callback;

        private SessionSelectAction(DBLanguagePsiFile file, DatabaseSession session, RunnableTask callback) {
            super(session.getName(), null, session.getIcon());
            this.fileRef = new WeakReference<DBLanguagePsiFile>(file);
            this.sessionRef = new WeakReference<DatabaseSession>(session);
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
        private WeakReference<DBLanguagePsiFile> fileRef;
        private ConnectionHandlerRef connectionHandlerRef;

        private SessionCreateAction(DBLanguagePsiFile file, ConnectionHandler connectionHandler) {
            super("New session...");
            this.fileRef = new WeakReference<DBLanguagePsiFile>(file);
            this.connectionHandlerRef = connectionHandler.getRef();
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            final DBLanguagePsiFile file = fileRef.get();
            if (file != null) {
                Project project = getProject();
                DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
                ConnectionHandler connectionHandler = connectionHandlerRef.get();
                sessionManager.showCreateSessionDialog(connectionHandler, new SimpleTask<DatabaseSession>() {
                    @Override
                    protected void execute() {
                        DatabaseSession session = getData();
                        if (session != null) {
                            file.setDatabaseSession(session);
                        }
                    }
                });
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

        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            String oldFileUrl = event.getOldParent().getUrl() + "/" + event.getFileName();
            FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
            if (fileConnectionMapping != null) {
                fileConnectionMapping.setFileUrl(event.getFile().getUrl());
            }
        }

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
    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
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
    public void loadState(Element element) {
        if (element != null) {
            for (Object child : element.getChildren()) {
                Element mappingElement = (Element) child;
                FileConnectionMapping mapping = new FileConnectionMapping();
                mapping.readState(mappingElement);
                mappings.add(mapping);
            }
        }
    }
}


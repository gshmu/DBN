package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.file.util.VirtualFileUtil;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionSelectorOptions;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.mapping.ui.FileConnectionMappingDialog;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.dci.intellij.dbn.connection.session.SessionManagerListener;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.IncorrectOperationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.common.message.MessageCallback.when;
import static com.dci.intellij.dbn.common.util.Messages.options;
import static com.dci.intellij.dbn.common.util.Messages.showWarningDialog;
import static com.dci.intellij.dbn.connection.ConnectionSelectorOptions.Option.*;

@State(
    name = FileConnectionMappingManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Slf4j
public class FileConnectionMappingManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.FileConnectionMappingManager";

    @Getter
    private final FileConnectionMappingRegistry registry;

    private FileConnectionMappingManager(@NotNull Project project) {
        super(project);
        this.registry = new FileConnectionMappingRegistry(project);
        SafeDisposer.register(this, this.registry);
        //VirtualFileManager.getInstance().addVirtualFileListener(virtualFileListener);
        ProjectEvents.subscribe(project, this, VirtualFileManager.VFS_CHANGES, bulkFileListener);
        ProjectEvents.subscribe(project, this, SessionManagerListener.TOPIC, sessionManagerListener);
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

    public void openFileConnectionMappings() {
        FileConnectionMappingDialog dialog = new FileConnectionMappingDialog(getProject());
        dialog.show();
    }


    /*******************************************************************
     *                    Connection mappings                          *
     *******************************************************************/
    @Nullable
    public ConnectionHandler getConnectionHandler(@NotNull VirtualFile virtualFile) {
        return registry.getConnectionHandler(virtualFile);
    }

    public boolean setConnectionHandler(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
        return registry.setConnectionHandler(virtualFile, connectionHandler);
    }

    public void setConnectionHandler(@NotNull Editor editor, @Nullable ConnectionHandler connectionHandler) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (isConnectionSelectable(virtualFile)) {
            boolean changed = setConnectionHandler(virtualFile, connectionHandler);
            if (changed) {
                Documents.touchDocument(editor, true);

                ProjectEvents.notify(getProject(),
                        FileConnectionMappingListener.TOPIC,
                        (listener) -> listener.connectionChanged(virtualFile, connectionHandler));
            }
        }
    }


    /*******************************************************************
     *                        Schema mappings                          *
     *******************************************************************/
    @Nullable
    public SchemaId getDatabaseSchema(@NotNull VirtualFile virtualFile) {
        return registry.getDatabaseSchema(virtualFile);
    }

    public boolean setDatabaseSchema(VirtualFile virtualFile, SchemaId schemaId) {
        return registry.setDatabaseSchema(virtualFile, schemaId);
    }

    public void setDatabaseSchema(@NotNull Editor editor, SchemaId schema) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (isSchemaSelectable(virtualFile)) {
            boolean changed = registry.setDatabaseSchema(virtualFile, schema);
            if (changed) {
                Documents.touchDocument(editor, false);

                ProjectEvents.notify(getProject(),
                        FileConnectionMappingListener.TOPIC,
                        (listener) -> listener.schemaChanged(virtualFile, schema));
            }
        }
    }

    /*******************************************************************
     *                       Session mappings                          *
     *******************************************************************/
    @Nullable
    public DatabaseSession getDatabaseSession(@NotNull VirtualFile virtualFile) {
        return registry.getDatabaseSession(virtualFile);
    }

    public boolean setDatabaseSession(VirtualFile virtualFile, DatabaseSession session) {
        return registry.setDatabaseSession(virtualFile, session);
    }

    public void setDatabaseSession(@NotNull Editor editor, DatabaseSession session) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (isSessionSelectable(virtualFile)) {
            registry.setDatabaseSession(virtualFile, session);

            ProjectEvents.notify(getProject(),
                    FileConnectionMappingListener.TOPIC,
                    (listener) -> listener.sessionChanged(virtualFile, session));
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


    public void selectConnectionAndSchema(@NotNull DBLanguagePsiFile file, DataContext dataContext, @NotNull Runnable callback) {
        Dispatch.run(() -> {
            Project project = getProject();
            ConnectionHandler activeConnection = file.getConnectionHandler();
            if (activeConnection == null || activeConnection.isVirtual()) {
                String message =
                        activeConnection == null ?
                                "The file is not linked to any connection.\nTo continue with the statement execution please select a target connection." :
                                "The connection you selected for this file is a virtual connection, used only to decide the SQL dialect.\n" +
                                        "You can not execute statements against this connection. Please select a proper connection to continue.";


                ConnectionSelectorOptions options = new ConnectionSelectorOptions();
                options.set(SHOW_CREATE_CONNECTION, true);
                options.set(PROMPT_SCHEMA_SELECTION, true);

                showWarningDialog(project,
                        "No valid connection", message,
                        options("Select Connection", "Cancel"), 0,
                        option -> when(option == 0, () ->
                                promptConnectionSelector(file, dataContext, options, callback)));

            } else if (file.getSchemaId() == null) {
                String message =
                        "You did not select any schema to run the statement against.\n" +
                                "To continue with the statement execution please select a schema.";
                showWarningDialog(project,
                        "No schema selected", message,
                        options("Use Current Schema", "Select Schema", "Cancel"), 0,
                        (option) -> {
                            if (option == 0) {
                                callback.run();
                            } else if (option == 1) {
                                promptSchemaSelector(file, dataContext, callback);
                            }
                        });
            } else {
                callback.run();
            }
        });
    }

    /***************************************************
     *             Select connection popup             *
     ***************************************************/
    public void promptConnectionSelector(DBLanguagePsiFile psiFile, DataContext dataContext, ConnectionSelectorOptions options, Runnable callback) {
        Project project = getProject();
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        List<ConnectionHandler> connection = connectionBundle.getConnections();

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connection.size() > 0) {
            for (ConnectionHandler connectionHandler : connection) {
                ConnectionSelectAction connectionAction = new ConnectionSelectAction(
                        connectionHandler,
                        psiFile,
                        options.is(PROMPT_SCHEMA_SELECTION),
                        callback);
                actionGroup.add(connectionAction);
            }
        }

        if (options.is(SHOW_VIRTUAL_CONNECTIONS)) {
            actionGroup.addSeparator();
            for (ConnectionHandler virtualConnectionHandler : connectionBundle.getVirtualConnections()) {
                ConnectionSelectAction connectionAction = new ConnectionSelectAction(
                        virtualConnectionHandler,
                        psiFile,
                        options.is(PROMPT_SCHEMA_SELECTION),
                        callback);
                actionGroup.add(connectionAction);
            }
        }

        if (options.is(SHOW_CREATE_CONNECTION)) {
            actionGroup.addSeparator();
            actionGroup.add(new ConnectionSetupAction(project));
        }

        Dispatch.run(() -> {
            ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Select Connection",
                    actionGroup,
                    dataContext,
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
        });
    }


    private class ConnectionSelectAction extends AbstractConnectionAction {
        private final PsiFileRef<DBLanguagePsiFile> psiFile;
        private final Runnable callback;
        private final boolean promptSchemaSelection;

        private ConnectionSelectAction(ConnectionHandler connectionHandler, DBLanguagePsiFile file, boolean promptSchemaSelection, Runnable callback) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon(), connectionHandler);
            this.psiFile = PsiFileRef.of(file);
            this.callback = callback;
            this.promptSchemaSelection = promptSchemaSelection;
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
            DBLanguagePsiFile file = psiFile.get();
            if (file != null) {
                file.setConnectionHandler(connectionHandler);
                if (promptSchemaSelection) {
                    promptSchemaSelector(file, e.getDataContext(), callback);
                } else {
                    if (file.getSchemaId() == null) {
                        SchemaId defaultSchema = connectionHandler.getDefaultSchema();
                        file.setDatabaseSchema(defaultSchema);
                    }
                    if (callback != null) {
                        callback.run();
                    }
                }

            }
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = psiFile.get();
            if (file != null) {
                ConnectionHandler connectionHandler = file.getConnectionHandler();
                return connectionHandler != null && connectionHandler.getConnectionId().equals(getConnectionId());
            }
            return false;

        }
    }

    private static class ConnectionSetupAction extends ProjectAction {
        private final ProjectRef project;

        private ConnectionSetupAction(Project project) {
            super("Setup New Connection", null, Icons.CONNECTION_NEW);
            this.project = ProjectRef.of(project);
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
        }

        @Nullable
        @Override
        protected Project getProject() {
            return project.get();
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSchemaSelector(DBLanguagePsiFile psiFile, DataContext dataContext, Runnable callback) throws IncorrectOperationException {
        Project project = getProject();
        ConnectionAction.invoke("selecting the current schema", true, psiFile,
                (action) -> Progress.prompt(project, "Loading schemas", true,
                        (progress) -> {
                            DefaultActionGroup actionGroup = new DefaultActionGroup();

                            ConnectionHandler connectionHandler = action.getConnectionHandler();
                            if (Failsafe.check(connectionHandler) && !connectionHandler.isVirtual()) {
                                List<DBSchema> schemas = connectionHandler.getObjectBundle().getSchemas();
                                for (DBSchema schema : schemas) {
                                    SchemaSelectAction schemaAction = new SchemaSelectAction(psiFile, schema, callback);
                                    actionGroup.add(schemaAction);
                                }
                            }

                            Dispatch.run(() -> {
                                ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                                        "Select Schema",
                                        actionGroup,
                                        dataContext,
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

                                popupBuilder.showCenteredInCurrentWindow(project);
                            });
                        }));
    }


    private static class SchemaSelectAction extends AnObjectAction<DBSchema> {
        private final PsiFileRef<DBLanguagePsiFile> psiFile;
        private final Runnable callback;

        private SchemaSelectAction(DBLanguagePsiFile file, DBSchema schema, Runnable callback) {
            super(schema);
            this.psiFile = PsiFileRef.of(file);
            this.callback = callback;
        }

        @Override
        protected void actionPerformed(
                @NotNull AnActionEvent e,
                @NotNull Project project,
                @NotNull DBSchema object) {

            DBLanguagePsiFile file = psiFile.get();
            if (file != null) {
                file.setDatabaseSchema(getSchemaId());
                if (callback != null) {
                    callback.run();
                }
            }
        }

        @Nullable
        public SchemaId getSchemaId() {
            return SchemaId.from(getTarget());
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = psiFile.get();
            if (file != null) {
                SchemaId fileSchema = file.getSchemaId();
                return fileSchema != null && fileSchema.equals(getSchemaId());
            }
            return false;
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSessionSelector(DBLanguagePsiFile psiFile, DataContext dataContext, Runnable callback) throws IncorrectOperationException {
        Project project = getProject();
        ConnectionAction.invoke("selecting the current session", true, psiFile,
                (action) -> {
                    DefaultActionGroup actionGroup = new DefaultActionGroup();
                    ConnectionHandler connectionHandler = action.getConnectionHandler();
                    if (Failsafe.check(connectionHandler) && !connectionHandler.isVirtual()) {
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
                            dataContext,
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

                    popupBuilder.showCenteredInCurrentWindow(project);
                });
    }


    private static class SessionSelectAction extends AnAction {
        private final PsiFileRef<DBLanguagePsiFile> psiFile;
        private final WeakRef<DatabaseSession> session;
        private final Runnable callback;

        private SessionSelectAction(DBLanguagePsiFile file, DatabaseSession session, Runnable callback) {
            super(session.getName(), null, session.getIcon());
            this.psiFile = PsiFileRef.of(file);
            this.session = WeakRef.of(session);
            this.callback = callback;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBLanguagePsiFile file = psiFile.get();
            DatabaseSession session = this.session.get();
            if (file != null && session != null) {
                file.setDatabaseSession(session);
                if (callback != null) {
                    callback.run();
                }
            }
        }

        public boolean isSelected() {
            DBLanguagePsiFile file = psiFile.get();
            if (file != null) {
                DatabaseSession fileSession = file.getDatabaseSession();
                return fileSession != null && fileSession.equals(session.get());
            }
            return false;
        }
    }

    private class SessionCreateAction extends DumbAwareAction {
        private final PsiFileRef<DBLanguagePsiFile> psiFile;
        private final ConnectionHandlerRef connectionHandler;

        private SessionCreateAction(DBLanguagePsiFile file, ConnectionHandler connectionHandler) {
            super("New Session...");
            this.psiFile = PsiFileRef.of(file);
            this.connectionHandler = connectionHandler.getRef();
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBLanguagePsiFile file = psiFile.get();
            if (file != null) {
                Project project = getProject();
                DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
                ConnectionHandler connectionHandler = this.connectionHandler.ensure();
                sessionManager.showCreateSessionDialog(
                        connectionHandler,
                        (session) -> {
                            if (session != null) {
                                file.setDatabaseSession(session);
                            }
                        });
            }
        }
    }

    /***************************************
     *         VirtualFileListener         *
     ***************************************/

    private final BulkFileListener bulkFileListener = new BulkFileListener() {
        @Override
        public void after(@NotNull List<? extends VFileEvent> events) {
            Map<String, FileConnectionMapping> mappings = registry.getMappings();
            for (VFileEvent event : events) {
                VirtualFile file = event.getFile();
                if (file != null) {
                    if (event instanceof VFileDeleteEvent) {
                        registry.removeMapping(file);

                    } else if (event instanceof VFileMoveEvent) {
                        VFileMoveEvent moveEvent = (VFileMoveEvent) event;
                        String oldFileUrl = moveEvent.getOldParent().getUrl() + "/" + file.getName();
                        FileConnectionMapping fileConnectionMapping = mappings.get(oldFileUrl);
                        if (fileConnectionMapping != null) {
                            fileConnectionMapping.setFileUrl(event.getFile().getUrl());
                        }

                    } else if (event instanceof VFilePropertyChangeEvent) {
                        VFilePropertyChangeEvent propChangeEvent = (VFilePropertyChangeEvent) event;
                        VirtualFile parent = file.getParent();
                        if (file.isInLocalFileSystem() && parent != null) {
                            String oldFileUrl = parent.getUrl() + "/" + propChangeEvent.getOldValue();
                            FileConnectionMapping fileConnectionMapping = mappings.get(oldFileUrl);
                            if (fileConnectionMapping != null) {
                                fileConnectionMapping.setFileUrl(file.getUrl());
                            }
                        }
                    }
                }
            }
        }
    };

    /***************************************
     *         SessionManagerListener      *
     ***************************************/
    private final SessionManagerListener sessionManagerListener = new SessionManagerListener() {
        @Override
        public void sessionDeleted(DatabaseSession session) {
            Map<String, FileConnectionMapping> mappings = registry.getMappings();
            for (FileConnectionMapping mapping : mappings.values()) {
                if (session.getId() == mapping.getSessionId()) {
                    mapping.setSessionId(SessionId.MAIN);
                }
            }
        }
    };

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        Map<String, FileConnectionMapping> mappings = registry.getMappings();
        for (FileConnectionMapping mapping : mappings.values()) {
            Element mappingElement = new Element("mapping");
            mapping.writeState(mappingElement);
            element.addContent(mappingElement);
        }
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        Map<String, FileConnectionMapping> mappings = registry.getMappings();
        for (Element child : element.getChildren()) {
            FileConnectionMapping mapping = new FileConnectionMapping();
            mapping.readState(child);

            String fileUrl = mapping.getFileUrl();
            VirtualFile virtualFile = mapping.getFile();
            if (virtualFile != null) {
                mappings.put(fileUrl, mapping);
            }
        }
    }


    @Override
    protected void disposeInner() {
        super.disposeInner();
    }

}


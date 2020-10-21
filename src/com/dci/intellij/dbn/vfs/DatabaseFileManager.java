package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.dci.intellij.dbn.common.util.CommonUtil.list;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.MODIFIED;

@State(
    name = DatabaseFileManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseFileManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseFileManager";

    private final Set<DBEditableObjectVirtualFile> openFiles = ContainerUtil.newConcurrentSet();
    private Map<ConnectionId, List<DBObjectRef<DBSchemaObject>>> pendingOpenFiles = new HashMap<>();
    private boolean projectInitialized = false;
    private final String sessionId;

    private DatabaseFileManager(final Project project) {
        super(project);
        sessionId = UUID.randomUUID().toString();

        subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, fileEditorManagerListenerBefore);
        subscribe(ConnectionSettingsListener.TOPIC, connectionSettingsListener);

/*
        StartupManager.getInstance(project).registerPreStartupActivity(new Runnable() {
            @Override
            public void run() {
                projectInitializing = true;
            }
        });
*/


        StartupManager startupManager = StartupManager.getInstance(project);
        startupManager.registerPostStartupActivity((DumbAwareRunnable) () -> projectInitialized = true);
        startupManager.registerPostStartupActivity((DumbAwareRunnable) () -> reopenDatabaseEditors());

    }

    public static DatabaseFileManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseFileManager.class);
    }

    public boolean isProjectInitialized() {
        return projectInitialized;
    }

    /**
     * Use session boundaries for avoiding the reuse of disposed cached virtual files
     */
    public String getSessionId() {
        return sessionId;
    }

    public boolean isFileOpened(@NotNull DBObject object) {
        for (DBEditableObjectVirtualFile openFile : openFiles) {
            if (openFile.getObjectRef().is(object)) {
                return true;
            }
        }

        return false;
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @Override
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    private final ConnectionSettingsListener connectionSettingsListener = new ConnectionSettingsListener() {
        @Override
        public void connectionChanged(ConnectionId connectionId) {
            closeFiles(connectionId);
        }
    };

    private void closeFiles(ConnectionId connectionId) {
        Set<DBEditableObjectVirtualFile> filesToClose = new HashSet<>();
        for (DBEditableObjectVirtualFile openFile : openFiles) {
            if (openFile.getConnectionId() == connectionId) {
                filesToClose.add(openFile);
            }
        }

        for (DBEditableObjectVirtualFile virtualFile : filesToClose) {
            closeFile(virtualFile);
        }
    }

    public void closeFile(DBSchemaObject object) {
        if (isFileOpened(object)) {
            closeFile(object.getVirtualFile());
        }
    }

    public void closeFile(@NotNull VirtualFile file) {
        FileEditorManager editorManager = FileEditorManager.getInstance(getProject());
        Dispatch.runConditional(() -> editorManager.closeFile(file));
    }


    /*********************************************
     *            FileEditorManagerListener       *
     *********************************************/
    private final FileEditorManagerListener.Before fileEditorManagerListenerBefore = new FileEditorManagerListener.Before() {
        @Override
        public void beforeFileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                DBObjectRef<DBSchemaObject> objectRef = databaseFile.getObjectRef();
                objectRef.ensure();
            }
        }

        @Override
        public void beforeFileClosed(@NotNull FileEditorManager editorManager, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                if (databaseFile.isModified()) {
                    DBSchemaObject object = databaseFile.getObject();
                    String objectDescription = object.getQualifiedNameWithType();
                    Project project = getProject();

                    CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(project).getConfirmationSettings();
                    confirmationSettings.getExitOnChanges().resolve(
                            list(objectDescription),
                            option -> {
                                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);

                                switch (option) {
                                    case CANCEL: break;
                                    case SAVE: {
                                        sourceCodeManager.saveSourceCodeChanges(
                                                databaseFile,
                                                () -> closeFile(file));
                                        break;
                                    }

                                    case DISCARD: {
                                        sourceCodeManager.revertSourceCodeChanges(
                                                databaseFile,
                                                () -> closeFile(file));
                                        break;
                                    }
                                    case SHOW: {
                                        List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                                        for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                                            if (sourceCodeFile.is(MODIFIED)) {
                                                SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
                                                diffManager.opedDatabaseDiffWindow(sourceCodeFile);
                                            }
                                        }
                                    }
                                }
                            });
                    throw new ProcessCanceledException();
                }
            }

        }
    };

    private final FileEditorManagerListener fileEditorManagerListener  =new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                openFiles.add(databaseFile);
            }
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                openFiles.remove(databaseFile);
            }
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {

        }
    };

    public void closeDatabaseFiles(@NotNull final List<ConnectionId> connectionIds) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        for (VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
            if (virtualFile instanceof DBVirtualFileImpl) {
                DBVirtualFileImpl databaseVirtualFile = (DBVirtualFileImpl) virtualFile;
                ConnectionId connectionId = databaseVirtualFile.getConnectionId();
                if (connectionIds.contains(connectionId)) {
                    closeFile(virtualFile);
                }
            }
        }
    }


    @Override
    public void projectClosed() {
/*
            // TODO seems to be obsolete since file unique id
            PsiManagerImpl psiManager = (PsiManagerImpl) PsiManager.getInstance(project);
            FileManagerImpl fileManager = (FileManagerImpl) psiManager.getFileManager();
            ConcurrentMap<VirtualFile, FileViewProvider> fileViewProviderCache = fileManager.getVFileToViewProviderMap();
            for (VirtualFile virtualFile : fileViewProviderCache.keySet()) {
                if (virtualFile instanceof DBContentVirtualFile) {
                    DBContentVirtualFile contentVirtualFile = (DBContentVirtualFile) virtualFile;
                    if (contentVirtualFile.isDisposed() || !contentVirtualFile.isValid() ||contentVirtualFile.getProject() == project) {
                        fileViewProviderCache.remove(virtualFile);
                    }
                } else if (virtualFile instanceof DBObjectVirtualFile) {
                    DBObjectVirtualFile objectVirtualFile = (DBObjectVirtualFile) virtualFile;
                    if (objectVirtualFile.isDisposed() || !objectVirtualFile.isValid() || objectVirtualFile.getProject() == project) {
                        fileViewProviderCache.remove(virtualFile);
                    }
                }
            }
*/

        DatabaseFileSystem.getInstance().clearCachedFiles(getProject());
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element stateElement = new Element("state");
        Element openFilesElement = new Element("open-files");
        stateElement.addContent(openFilesElement);
        for (DBEditableObjectVirtualFile openFile : openFiles) {
            DBObjectRef<DBSchemaObject> objectRef = openFile.getObjectRef();
            Element fileElement = new Element("object");
            objectRef.writeState(fileElement);
            openFilesElement.addContent(fileElement);
        }

        return stateElement;
    }

    @Override
    public void loadState(@NotNull Element element) {
        Element openFilesElement = element.getChild("open-files");
        if (openFilesElement != null && pendingOpenFiles != null) {
            List<Element> fileElements = openFilesElement.getChildren();
            fileElements.forEach((fileElement) -> {
                DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.from(fileElement);
                if (objectRef != null) {
                    ConnectionId connectionId = objectRef.getConnectionId();
                    List<DBObjectRef<DBSchemaObject>> objectRefs =
                            pendingOpenFiles.computeIfAbsent(connectionId, k -> new ArrayList<>());
                    objectRefs.add(objectRef);
                }
            });
        }
    }

    private void reopenDatabaseEditors() {
        Project project = getProject();
        if (pendingOpenFiles != null && !pendingOpenFiles.isEmpty()) {
            Map<ConnectionId, List<DBObjectRef<DBSchemaObject>>> pendingOpenFiles = this.pendingOpenFiles;
            this.pendingOpenFiles = null;

            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            for (ConnectionId connectionId : pendingOpenFiles.keySet()) {

                List<DBObjectRef<DBSchemaObject>> objectRefs = pendingOpenFiles.get(connectionId);
                ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
                if (connectionHandler != null) {
                    ConnectionDetailSettings connectionDetailSettings = connectionHandler.getSettings().getDetailSettings();
                    if (connectionDetailSettings.isRestoreWorkspace()) {
                        reopenDatabaseEditors(objectRefs, connectionHandler);
                    }
                }
            }
        }
    }

    private static void reopenDatabaseEditors(@NotNull List<DBObjectRef<DBSchemaObject>> objectRefs, @NotNull ConnectionHandler connectionHandler) {
        Project project = connectionHandler.getProject();
        ConnectionAction.invoke("opening database editors", false, connectionHandler, action ->
                Progress.prompt(project, "Opening database editors (" + connectionHandler.getQualifiedName() + ")", true,
                        (progress) -> {
                            progress.setIndeterminate(true);
                            progress.setText2(connectionHandler.getQualifiedName());
                            DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();

                            for (DBObjectRef<DBSchemaObject> objectRef : objectRefs) {
                                if (progress.isCanceled()) continue;
                                if (connectionHandler.canConnect()) {
                                    DBSchemaObject object = objectRef.get(project);
                                    if (object != null) {
                                        progress.setText2(connectionHandler.getQualifiedName() + " - " + objectRef.getQualifiedNameWithType());
                                        object.initChildren();
                                        databaseFileSystem.openEditor(object, null, false, false);
                                    }
                                }
                            }
                        }));
    }
}

package com.dci.intellij.dbn.vfs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.editor.code.options.CodeEditorChangesOption;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.impl.FileManagerImpl;

@State(
    name = "DBNavigator.Project.DatabaseFileManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DatabaseFileManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private Map<DBObjectRef, DBEditableObjectVirtualFile> openFiles = new HashMap<DBObjectRef, DBEditableObjectVirtualFile>();

    private String sessionId;

    private DatabaseFileManager(Project project) {
        super(project);
        sessionId = UUID.randomUUID().toString();
    }
    public static DatabaseFileManager getInstance(@NotNull Project project) {
        return project.getComponent(DatabaseFileManager.class);
    }

    /**
     * Use session boundaries for avoiding the reuse of disposed cached virtual files
     */
    public String getSessionId() {
        return sessionId;
    }

    public boolean isFileOpened(DBSchemaObject object) {
        return openFiles.containsKey(object.getRef());
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseFileManager";
    }

    public void projectOpened() {
        Project project = getProject();
        EventManager.subscribe(project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        EventManager.subscribe(project, FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, fileEditorManagerListenerBefore);
        EventManager.subscribe(project, ConnectionSettingsListener.TOPIC, connectionSettingsListener);
    }

    public void projectClosed() {
        EventManager.unsubscribe(
                fileEditorManagerListener,
                fileEditorManagerListenerBefore,
                connectionSettingsListener);
    }

    private ConnectionSettingsListener connectionSettingsListener = new ConnectionSettingsListener() {
        @Override
        public void settingsChanged(String connectionId) {
            closeFiles(connectionId);
        }
    };

    private void closeFiles(String connectionId) {
        Set<DBEditableObjectVirtualFile> filesToClose = new HashSet<DBEditableObjectVirtualFile>();
        for (DBObjectRef objectRef : openFiles.keySet()) {
            if (objectRef.getConnectionId().equals(connectionId)) {
                filesToClose.add(openFiles.get(objectRef));
            }
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        for (DBEditableObjectVirtualFile virtualFile : filesToClose) {
            fileEditorManager.closeFile(virtualFile);
        }
    }

    /********************************************************
     *                ObjectFactoryListener                 *
     ********************************************************/

    public void closeFile(DBSchemaObject object) {
        if (isFileOpened(object)) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
            fileEditorManager.closeFile(object.getVirtualFile());
        }
    }

    /*********************************************
     *            FileEditorManagerListener       *
     *********************************************/
    FileEditorManagerListener.Before fileEditorManagerListenerBefore = new FileEditorManagerListener.Before() {
        @Override
        public void beforeFileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        }

        @Override
        public void beforeFileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                final DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                if (databaseFile.isModified()) {
                    DBSchemaObject object = databaseFile.getObject();

                    CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(getProject()).getConfirmationSettings();
                    InteractiveOptionHandler<CodeEditorChangesOption> optionHandler = confirmationSettings.getExitOnChangesOptionHandler();
                    CodeEditorChangesOption option = optionHandler.resolve(object.getQualifiedNameWithType());

                    switch (option) {
                        case SAVE: databaseFile.saveChanges(); break;
                        case DISCARD: databaseFile.revertChanges(); break;
                        case CANCEL: throw new ProcessCanceledException();
                    }
                }
            }

        }
    };
    private FileEditorManagerListener fileEditorManagerListener  =new FileEditorManagerAdapter() {
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                openFiles.put(databaseFile.getObjectRef(), databaseFile);
            }
        }

        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                openFiles.remove(databaseFile.getObjectRef());
            }
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {

        }
    };

    public void closeDatabaseFiles(@NotNull final List<ConnectionHandler> connectionHandlers) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        for (VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
            if (virtualFile instanceof DBVirtualFileImpl) {
                DBVirtualFileImpl databaseVirtualFile = (DBVirtualFileImpl) virtualFile;
                if (connectionHandlers.contains(databaseVirtualFile.getConnectionHandler())) {
                    fileEditorManager.closeFile(virtualFile);
                }
            }
        }
    }

    @Override
    public void projectClosing(Project project) {
        if (project == getProject()) {
            PsiManagerImpl psiManager = (PsiManagerImpl) PsiManager.getInstance(project);
            FileManagerImpl fileManager = (FileManagerImpl) psiManager.getFileManager();
            ConcurrentMap<VirtualFile, FileViewProvider> fileViewProviderCache = fileManager.getVFileToViewProviderMap();
            for (VirtualFile virtualFile : fileViewProviderCache.keySet()) {
                if (virtualFile instanceof DBContentVirtualFile) {
                    DBContentVirtualFile contentVirtualFile = (DBContentVirtualFile) virtualFile;
                    if (contentVirtualFile.isDisposed() || contentVirtualFile.getProject() == project) {
                        fileViewProviderCache.remove(virtualFile);
                    }
                } else if (virtualFile instanceof DBObjectVirtualFile) {
                    DBObjectVirtualFile objectVirtualFile = (DBObjectVirtualFile) virtualFile;
                    if (objectVirtualFile.isDisposed() || objectVirtualFile.getProject() == project) {
                        fileViewProviderCache.remove(virtualFile);
                    }
                }
            }

            DatabaseFileSystem.getInstance().clearCachedFiles(project);
        }
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {

    }
}

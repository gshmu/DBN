package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.EditorStateManager;
import com.dci.intellij.dbn.editor.code.SourceCodeMainEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBConnectionVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBDatasetFilterVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectListVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionStatementVirtualFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.*;

public class DatabaseFileSystem extends VirtualFileSystem implements /*NonPhysicalFileSystem, */ApplicationComponent {
    public static final String PS = "/";
    private static final String PROTOCOL = "db";
    private static final String PROTOCOL_PREFIX = PROTOCOL + "://";

    public enum FilePathType {
        OBJECTS("objects", "objects"),
        OBJECT_CONTENTS("object_contents", "object contents"),
        CONSOLES("consoles", "consoles"),
        SESSION_BROWSERS("session_browsers", "session browsers"),
        SESSION_STATEMENTS("session_statements", "session statements"),
        DATASET_FILTERS("dataset_filters", "dataset filters");

        private String urlToken;
        private String presentableUrlToken;

        FilePathType(String urlToken, String presentableUrlToken) {
            this.urlToken = urlToken + PS;
            this.presentableUrlToken = presentableUrlToken + PS;
        }

        @Override
        public String toString() {
            return urlToken;
        }

        boolean is(String path) {
            return path.startsWith(urlToken);
        }

        String collate(String path) {
            return path.substring(urlToken.length());
        }
    }

    static final IOException READONLY_FILE_SYSTEM = new IOException("Operation not supported");
    private Map<DBObjectRef, DBEditableObjectVirtualFile> filesCache = ContainerUtil.newConcurrentMap();

    public DatabaseFileSystem() {
    }

    public static DatabaseFileSystem getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseFileSystem.class);
    }
                                                                            
    @Override
    @NotNull
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * [connection_id]/consoles/[console_name]
     * [connection_id]/session_browsers/[console_name]
     * [connection_id]/dataset_filters/[dataset_ref_serialized]
     * [connection_id]/objects/[object_ref_serialized]
     * [connection_id]/object_contents/[content_Type]/[object_ref_serialized]
     */

    @Override
    @Nullable
    public VirtualFile findFileByPath(@NotNull @NonNls String path) {
        if (path.startsWith(PROTOCOL_PREFIX)) {
            path = path.substring(PROTOCOL_PREFIX.length());
        }

        int index = path.indexOf(PS);

        if (index > -1) {
            ConnectionId connectionId = ConnectionId.get(path.substring(0, index));
            ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            if (Failsafe.check(connectionHandler) && connectionHandler.isEnabled()) {
                if (allowFileLookup(connectionHandler)) {
                    Project project = connectionHandler.getProject();
                    String relativePath = path.substring(index + 1);
                    if (CONSOLES.is(relativePath)) {
                        String consoleName = CONSOLES.collate(relativePath);
                        return connectionHandler.getConsoleBundle().getConsole(consoleName);

                    } else if (SESSION_BROWSERS.is(relativePath)) {
                        return connectionHandler.getSessionBrowserFile();

                    } else if (OBJECTS.is(relativePath)) {
                        String objectIdentifier = OBJECTS.collate(relativePath);
                        DBObjectRef<DBSchemaObject> objectRef = new DBObjectRef<>(connectionId, objectIdentifier);
                        return findOrCreateDatabaseFile(project, objectRef);
                    } else if (OBJECT_CONTENTS.is(relativePath)) {
                        String contentIdentifier = OBJECT_CONTENTS.collate(relativePath);
                        int contentTypeEndIndex = contentIdentifier.indexOf(PS);
                        String contentTypeStr = contentIdentifier.substring(0, contentTypeEndIndex);
                        DBContentType contentType = DBContentType.valueOf(contentTypeStr.toUpperCase());

                        String objectIdentifier = contentIdentifier.substring(contentTypeEndIndex + 1);
                        DBObjectRef<DBSchemaObject> objectRef = new DBObjectRef<>(connectionId, objectIdentifier);
                        DBEditableObjectVirtualFile virtualFile = findOrCreateDatabaseFile(project, objectRef);
                        return virtualFile.getContentFile(contentType);
                    }
                }
            }
        }
        return null;
    }

    public boolean isValidPath(String path, Project project) {
        if (path.startsWith(PROTOCOL_PREFIX)) {
            path = path.substring(PROTOCOL_PREFIX.length());
        }
        if (path.startsWith("null")) {
            return false;
        }

        int index = path.indexOf(PS);
        if (index > -1) {
            ConnectionId connectionId = ConnectionId.get(path.substring(0, index));
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
            //ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            if (connectionHandler != null || !project.isInitialized()) {
                String relativePath = path.substring(index + 1);
                for (FilePathType pathType : FilePathType.values()) {
                    if (pathType.is(relativePath)) {
                        return true;
                    }
                }

            }
        }

        return false;
    }


    @NotNull
    @Override
    public String extractPresentableUrl(@NotNull String url) {
        if (url.startsWith(PROTOCOL_PREFIX)) {
            url = url.substring(PROTOCOL_PREFIX.length());
        }
        return extractPresentablePath(url);
    }

    public String extractPresentablePath(@NotNull String path) {
        int index = path.indexOf(PS);
        ConnectionId connectionId = ConnectionId.get(index == -1 ? path : path.substring(0, index));
        ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
        if (connectionHandler == null) {
            path = path.replace(connectionId.id(), "UNKNOWN");
        } else {
            path = path.replace(connectionId.id(), connectionHandler.getName());
        }

        if (index > -1) {
            for (FilePathType value : values()) {
                path = path.replace(value.urlToken, value.presentableUrlToken);
            }

            path = path.replace(PS, File.separator);
        }

        return path;
    }

    private boolean allowFileLookup(ConnectionHandler connectionHandler) {
        ConnectionDetailSettings connectionDetailSettings = connectionHandler.getSettings().getDetailSettings();
        if (connectionDetailSettings.isRestoreWorkspace()) {
            return true;
        } else {
            Project project = connectionHandler.getProject();
            DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
            return databaseFileManager.isProjectInitialized();
        }
    }

    @Nullable
    public DBEditableObjectVirtualFile findDatabaseFile(DBSchemaObject object) {
        DBObjectRef objectRef = object.getRef();
        return filesCache.get(objectRef);
    }

    @NotNull
    public DBEditableObjectVirtualFile findOrCreateDatabaseFile(@NotNull Project project, @NotNull DBObjectRef objectRef) {
        DBEditableObjectVirtualFile databaseFile = filesCache.get(objectRef);
        if (!Failsafe.check(databaseFile)){
            databaseFile = filesCache.get(objectRef);
            if (!Failsafe.check(databaseFile)){
                databaseFile = Read.call(() -> new DBEditableObjectVirtualFile(project, objectRef));
                filesCache.put(objectRef, databaseFile);
            }
        }
        return databaseFile;
    }

    public static boolean isFileOpened(DBSchemaObject object) {
        Project project = object.getProject();
        return DatabaseFileManager.getInstance(project).isFileOpened(object);
    }

    /********************************************************
     *                    GENERIC                           *
     ********************************************************/
    @NotNull
    static String createPath(DBVirtualFile virtualFile) {
        try {
            ConnectionId connectionId = virtualFile.getConnectionId();
            if (virtualFile instanceof DBConnectionVirtualFile) {
                DBConnectionVirtualFile file = (DBConnectionVirtualFile) virtualFile;
                return file.getConnectionId() + "";
            }

            if (virtualFile instanceof DBObjectVirtualFile) {
                DBObjectVirtualFile file = (DBObjectVirtualFile) virtualFile;
                DBObjectRef objectRef = file.getObjectRef();
                return objectRef.getConnectionId() + PS + OBJECTS + objectRef.serialize();

            }

            if (virtualFile instanceof DBContentVirtualFile) {
                DBContentVirtualFile file = (DBContentVirtualFile) virtualFile;
                DBObjectRef objectRef = file.getObject().getRef();
                DBContentType contentType = file.getContentType();
                return objectRef.getConnectionId() + PS + OBJECT_CONTENTS + contentType.name() + PS + objectRef.serialize();
            }

            if (virtualFile instanceof DBObjectListVirtualFile) {
                DBObjectListVirtualFile file = (DBObjectListVirtualFile) virtualFile;
                DBObjectList objectList = file.getObjectList();
                GenericDatabaseElement parentElement = objectList.getParentElement();
                String listName = objectList.getObjectType().getListName();
                String connectionPath = connectionId.id();
                if (parentElement instanceof DBObject) {
                    DBObject object = (DBObject) parentElement;
                    DBObjectRef objectRef = object.getRef();
                    return connectionPath + PS + objectRef.serialize() + PS + listName;
                } else {
                    return connectionPath + PS + listName; }
            }

            if (virtualFile instanceof DBDatasetFilterVirtualFile) {
                DBDatasetFilterVirtualFile file = (DBDatasetFilterVirtualFile) virtualFile;
                return connectionId + PS + DATASET_FILTERS + file.getDataset().getRef().serialize();
            }

            if (virtualFile instanceof DBConsoleVirtualFile) {
                DBConsoleVirtualFile file = (DBConsoleVirtualFile) virtualFile;
                return connectionId + PS + CONSOLES + file.getName();
            }

            if (virtualFile instanceof DBSessionBrowserVirtualFile) {
                DBSessionBrowserVirtualFile file = (DBSessionBrowserVirtualFile) virtualFile;
                return connectionId + PS + SESSION_BROWSERS + file.getName();

            }

            if (virtualFile instanceof DBSessionStatementVirtualFile) {
                DBSessionStatementVirtualFile file = (DBSessionStatementVirtualFile) virtualFile;
                return connectionId + PS + SESSION_STATEMENTS + file.getName();
            }

            throw new IllegalArgumentException("File of type " + virtualFile.getClass() + " is not supported");
        } catch (ProcessCanceledException e) {
            return "DISPOSED\""+ UUID.randomUUID().toString();
        }
    }

    @NotNull
    static String createUrl(DBVirtualFile virtualFile) {
        return PROTOCOL_PREFIX + createPath(virtualFile);
    }

    /*********************************************************
     *                  VirtualFileSystem                    *
     *********************************************************/

    @Override
    public void refresh(boolean b) {

    }

    @Override
    @Nullable
    public VirtualFile refreshAndFindFileByPath(@NotNull String s) {
        return null;
    }

    @Override
    public void addVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    @Override
    public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    public void forceRefreshFiles(boolean b, @NotNull VirtualFile... virtualFiles) {

    }

    @Override
    protected void deleteFile(Object o, @NotNull VirtualFile virtualFile) throws IOException {}

    @Override
    protected void moveFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @Override
    protected void renameFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @Override
    @NotNull
    protected VirtualFile createChildFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @Override
    @NotNull
    protected VirtualFile createChildDirectory(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @Override
    @NotNull
    protected VirtualFile copyFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DatabaseFileSystem";
    }

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {}

    /*********************************************************
     *              FileEditorManagerListener                *
     *********************************************************/
    public void openEditor(DBObject object, boolean focusEditor) {
        openEditor(object, null, false, focusEditor);
    }

    public void openEditor(DBObject object, @Nullable EditorProviderId editorProviderId, boolean focusEditor) {
        openEditor(object, editorProviderId, false, focusEditor);
    }

    public void openEditor(DBObject object, @Nullable EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        ConnectionAction.invoke("opening the object editor", false, object,
                (action) -> Progress.prompt(object.getProject(), "Opening editor", true,
                        (progress) -> {
                            if (Failsafe.check(object)) {
                                EditorProviderId providerId = editorProviderId;
                                if (editorProviderId == null) {
                                    EditorStateManager editorStateManager = EditorStateManager.getInstance(object.getProject());
                                    providerId = editorStateManager.getEditorProvider(object.getObjectType());
                                }

                                if (object.is(DBObjectProperty.SCHEMA_OBJECT)) {
                                    DBObjectListContainer childObjects = object.getChildObjects();
                                    if (childObjects != null) childObjects.load();

                                    openSchemaObject((DBSchemaObject) object, providerId, scrollBrowser, focusEditor);

                                } else if (object.getParentObject().is(DBObjectProperty.SCHEMA_OBJECT)) {
                                    DBObjectListContainer childObjects = object.getParentObject().getChildObjects();
                                    if (childObjects != null) childObjects.load();
                                    openChildObject(object, providerId, scrollBrowser, focusEditor);
                                }
                            }
                        }));
    }

    private void openSchemaObject(@NotNull DBSchemaObject object, EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        Project project = object.getProject();
        DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(project, object.getRef());
        databaseFile.setSelectedEditorProviderId(editorProviderId);
        if (!ProgressMonitor.isCancelled()) {
            Dispatch.invoke(() -> {
                if (Failsafe.check(object) && (isFileOpened(object) || databaseFile.preOpen())) {
                    DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(scrollBrowser);
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    fileEditorManager.openFile(databaseFile, focusEditor);
                    NavigationInstruction navigationInstruction = focusEditor ? NavigationInstruction.FOCUS_SCROLL : NavigationInstruction.SCROLL;
                    EditorUtil.selectEditor(project, null, databaseFile, editorProviderId, navigationInstruction);
                    DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(true);
                }
            });
        }
    }

    private void openChildObject(DBObject object, EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        DBSchemaObject schemaObject = (DBSchemaObject) object.getParentObject();
        Project project = schemaObject.getProject();
        DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(project, schemaObject.getRef());
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        sourceCodeManager.ensureSourcesLoaded(schemaObject, false);
        if (!ProgressMonitor.isCancelled()) {
            Dispatch.invoke(() -> {
                if (Failsafe.check(schemaObject) && (isFileOpened(schemaObject) || databaseFile.preOpen())) {
                    DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(scrollBrowser);
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    FileEditor[] fileEditors = fileEditorManager.openFile(databaseFile, focusEditor);
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof SourceCodeMainEditor) {
                            SourceCodeMainEditor sourceCodeEditor = (SourceCodeMainEditor) fileEditor;
                            NavigationInstruction navigationInstruction = focusEditor ? NavigationInstruction.FOCUS_SCROLL : NavigationInstruction.SCROLL;
                            EditorUtil.selectEditor(project, fileEditor, databaseFile, editorProviderId, navigationInstruction);
                            sourceCodeEditor.navigateTo(object);
                            break;
                        }
                    }
                    DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(true);
                }
            });
        }
    }

    public void closeEditor(DBSchemaObject object) {
        VirtualFile virtualFile = findDatabaseFile(object);
        if (virtualFile != null) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(object.getProject());
            fileEditorManager.closeFile(virtualFile);
        }
    }

    public void reopenEditor(DBSchemaObject object) {
        Project project = object.getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile virtualFile = findOrCreateDatabaseFile(project, object.getRef());
        if (fileEditorManager.isFileOpen(virtualFile)) {
            fileEditorManager.closeFile(virtualFile);
            fileEditorManager.openFile(virtualFile, false);
        }
    }

    void clearCachedFiles(Project project) {
        Iterator<DBObjectRef> objectRefs = filesCache.keySet().iterator();
        while (objectRefs.hasNext()) {
            DBObjectRef objectRef = objectRefs.next();
            DBEditableObjectVirtualFile file = filesCache.get(objectRef);
            if (file.isDisposed() || !file.isValid() || file.getProject() == project) {
                objectRefs.remove();
                Disposer.dispose(file);
            }
        }
    }

    public boolean isDatabaseUrl(String fileUrl) {
        return fileUrl.startsWith(PROTOCOL_PREFIX);
    }
}

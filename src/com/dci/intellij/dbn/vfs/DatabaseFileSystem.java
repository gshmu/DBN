package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.EditorStateManager;
import com.dci.intellij.dbn.editor.code.SourceCodeMainEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.CONSOLES;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.DATASET_FILTERS;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.OBJECTS;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.OBJECT_CONTENTS;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.SESSION_BROWSERS;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.SESSION_STATEMENTS;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.values;

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
    private Map<DBObjectRef, DBEditableObjectVirtualFile> filesCache = new HashMap<DBObjectRef, DBEditableObjectVirtualFile>();

    public DatabaseFileSystem() {
    }

    public static DatabaseFileSystem getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseFileSystem.class);
    }
                                                                            
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

    @Nullable
    public VirtualFile findFileByPath(@NotNull @NonNls String path) {
        int startIndex = 0;
        if (path.startsWith(PROTOCOL_PREFIX)) {
            startIndex = PROTOCOL_PREFIX.length();
        }

        int index = path.indexOf('/', startIndex);

        if (index > -1) {
            ConnectionId connectionId = ConnectionId.get(path.substring(startIndex, index));
            ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            if (connectionHandler != null && !connectionHandler.isDisposed() && connectionHandler.isEnabled()) {
                String relativePath = path.substring(index + 1);
                if (isValidPath(relativePath) && allowFileLookup(connectionHandler)) {
                    if (CONSOLES.is(relativePath)) {
                        String consoleName = CONSOLES.collate(relativePath);
                        return connectionHandler.getConsoleBundle().getConsole(consoleName);

                    } else if (SESSION_BROWSERS.is(relativePath)) {
                        return connectionHandler.getSessionBrowserFile();

                    } else if (OBJECTS.is(relativePath)) {
                        String objectIdentifier = OBJECTS.collate(relativePath);
                        DBObjectRef objectRef = new DBObjectRef(connectionId, objectIdentifier);
                        DBObject object = objectRef.get();
                        if (object != null && object.is(DBObjectProperty.EDITABLE)) {
                            return findOrCreateDatabaseFile((DBSchemaObject) object);
                        }
                    } else if (OBJECT_CONTENTS.is(relativePath)) {
                        String contentIdentifier = OBJECT_CONTENTS.collate(relativePath);
                        int contentTypeEndIndex = contentIdentifier.indexOf(PS);
                        String contentTypeStr = contentIdentifier.substring(0, contentTypeEndIndex);
                        DBContentType contentType = DBContentType.valueOf(contentTypeStr.toUpperCase());

                        String objectIdentifier = relativePath.substring(contentTypeEndIndex + 1);
                        DBObjectRef objectRef = new DBObjectRef(connectionId, objectIdentifier);
                        DBObject object = objectRef.get();
                        if (object != null && object.is(DBObjectProperty.EDITABLE)) {
                            DBEditableObjectVirtualFile virtualFile = findOrCreateDatabaseFile((DBSchemaObject) object);
                            return virtualFile.getContentFile(contentType);
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public String extractPresentableUrl(@NotNull String url) {
        ConnectionId connectionId = ConnectionId.get(url.substring(0, url.indexOf(PS)));
        ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
        String path = url;
        if (connectionHandler == null) {
            path = path.replace(connectionId.id(), "UNKNOWN");
        } else {
            path = path.replace(connectionId.id(), connectionHandler.getName());
        }

        for (FilePathType value : values()) {
            path = path.replace(value.urlToken, value.presentableUrlToken);
        }

        path = path.replace(PS, File.separator);

        return path;
    }

    boolean isValidPath(String objectPath) {
        return !objectPath.startsWith("null");
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

    private static DBEditableObjectVirtualFile createDatabaseFile(final DBSchemaObject object) {
        return new ReadActionRunner<DBEditableObjectVirtualFile>() {
            @Override
            protected DBEditableObjectVirtualFile run() {
                return new DBEditableObjectVirtualFile(object);
            }
        }.start();
    }

    @Nullable
    public DBEditableObjectVirtualFile findDatabaseFile(DBSchemaObject object) {
        DBObjectRef objectRef = object.getRef();
        return filesCache.get(objectRef);
    }

    @NotNull
    public DBEditableObjectVirtualFile findOrCreateDatabaseFile(DBSchemaObject object) {
        DBObjectRef objectRef = object.getRef();
        DBEditableObjectVirtualFile databaseFile = filesCache.get(objectRef);
        if (databaseFile == null || databaseFile.isDisposed()){
            databaseFile = createDatabaseFile(object);
            filesCache.put(objectRef, databaseFile);
        }
        return databaseFile;
    }

    public static boolean isFileOpened(DBSchemaObject object) {
        Project project = object.getProject();
        return DatabaseFileManager.getInstance(project).isFileOpened(object);
    }

    private static String createPath(ConnectionHandler connectionHandler) {
        return connectionHandler.getId().id();
    }

    /********************************************************
     *                    GENERIC                           *
     ********************************************************/
    @NotNull
    static String createPath(DBVirtualFile virtualFile) {
        ConnectionHandler connectionHandler = virtualFile.getConnectionHandler();
        if (virtualFile instanceof DBConnectionVirtualFile) {
            DBConnectionVirtualFile file = (DBConnectionVirtualFile) virtualFile;
            return createPath(file.getConnectionHandler());
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
            String connectionPath = createPath(connectionHandler);
            if (parentElement instanceof DBObject) {
                DBObject object = (DBObject) parentElement;
                DBObjectRef objectRef = object.getRef();
                return connectionPath + PS + objectRef.serialize() + PS + listName;
            } else {
                return connectionPath + PS + listName; }
        }

        if (virtualFile instanceof DBDatasetFilterVirtualFile) {
            DBDatasetFilterVirtualFile file = (DBDatasetFilterVirtualFile) virtualFile;
            return createPath(connectionHandler) + PS + DATASET_FILTERS + file.getDataset().getRef().serialize();
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile file = (DBConsoleVirtualFile) virtualFile;
            return createPath(connectionHandler) + PS + CONSOLES + file.getName();
        }

        if (virtualFile instanceof DBSessionBrowserVirtualFile) {
            DBSessionBrowserVirtualFile file = (DBSessionBrowserVirtualFile) virtualFile;
            return createPath(connectionHandler) + PS + SESSION_BROWSERS + file.getName();

        }

        if (virtualFile instanceof DBSessionStatementVirtualFile) {
            DBSessionStatementVirtualFile file = (DBSessionStatementVirtualFile) virtualFile;
            return createPath(connectionHandler) + PS + SESSION_STATEMENTS + file.getName();
        }

        throw new IllegalArgumentException("File of type " + virtualFile.getClass() + " is not supported");
    }

    @NotNull
    static String createUrl(DBVirtualFile virtualFile) {
        return PROTOCOL_PREFIX + createPath(virtualFile);
    }

    public static String getDefaultExtension(DBObject object) {
        if (object instanceof DBSchemaObject) {
            DBSchemaObject schemaObject = (DBSchemaObject) object;
            DDLFileType ddlFileType = schemaObject.getDDLFileType(null);
            DBLanguageFileType fileType = ddlFileType == null ? SQLFileType.INSTANCE : ddlFileType.getLanguageFileType();
            return fileType.getDefaultExtension();
        }
        return "";
    }

    /*********************************************************
     *                  VirtualFileSystem                    *
     *********************************************************/

    public void refresh(boolean b) {

    }

    @Nullable
    public VirtualFile refreshAndFindFileByPath(@NotNull String s) {
        return null;
    }

    public void addVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    public void forceRefreshFiles(boolean b, @NotNull VirtualFile... virtualFiles) {

    }

    protected void deleteFile(Object o, @NotNull VirtualFile virtualFile) throws IOException {}

    protected void moveFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    protected void renameFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @NotNull
    protected VirtualFile createChildFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @NotNull
    protected VirtualFile createChildDirectory(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    @NotNull
    protected VirtualFile copyFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1, @NotNull String s) throws IOException {
        throw READONLY_FILE_SYSTEM;
    }

    public boolean isReadOnly() {
        return true;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DatabaseFileSystem";
    }

    public void initComponent() {}

    public void disposeComponent() {}

    /*********************************************************
     *              FileEditorManagerListener                *
     *********************************************************/
    public void openEditor(final DBObject object, boolean focusEditor) {
        openEditor(object, null, false, focusEditor);
    }

    public void openEditor(final DBObject object, @Nullable EditorProviderId editorProviderId, boolean focusEditor) {
        openEditor(object, editorProviderId, false, focusEditor);
    }

    public void openEditor(final DBObject object, @Nullable final EditorProviderId editorProviderId, final boolean scrollBrowser, final boolean focusEditor) {
        new ConnectionAction("opening the object editor", object, new TaskInstructions("Opening editor", TaskInstruction.CANCELLABLE)) {
            @Override
            protected void execute() {
                EditorProviderId providerId = editorProviderId;
                if (editorProviderId == null) {
                    EditorStateManager editorStateManager = EditorStateManager.getInstance(getProject());
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
        }.start();
    }

    private void openSchemaObject(final DBSchemaObject object, final EditorProviderId editorProviderId, final boolean scrollBrowser, final boolean focusEditor) {
        final DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(object);
        databaseFile.setSelectedEditorProviderId(editorProviderId);
        if (!BackgroundTask.isProcessCancelled()) {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    if (isFileOpened(object) || databaseFile.preOpen()) {
                        DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(scrollBrowser);
                        Project project = object.getProject();
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        fileEditorManager.openFile(databaseFile, focusEditor);
                        NavigationInstruction navigationInstruction = focusEditor ? NavigationInstruction.FOCUS_SCROLL : NavigationInstruction.SCROLL;
                        EditorUtil.selectEditor(project, null, databaseFile, editorProviderId, navigationInstruction);
                        DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(true);
                    }
                }
            }.start();
        }
    }

    private void openChildObject(final DBObject object, final EditorProviderId editorProviderId, final boolean scrollBrowser, final boolean focusEditor) {
        final DBSchemaObject schemaObject = (DBSchemaObject) object.getParentObject();
        final Project project = schemaObject.getProject();
        final DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(schemaObject);
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        sourceCodeManager.ensureSourcesLoaded(schemaObject);
        if (!BackgroundTask.isProcessCancelled()) {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    if (isFileOpened(schemaObject) || databaseFile.preOpen()) {
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

                }
            }.start();
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
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(object.getProject());
        VirtualFile virtualFile = findOrCreateDatabaseFile(object);
        if (fileEditorManager.isFileOpen(virtualFile)) {
            fileEditorManager.closeFile(virtualFile);
            fileEditorManager.openFile(virtualFile, false);
        }
    }

    public void clearCachedFiles(Project project) {
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

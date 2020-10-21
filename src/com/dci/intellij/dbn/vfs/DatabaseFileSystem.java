package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.project.ProjectUtil;
import com.dci.intellij.dbn.common.routine.ProgressRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.SourceCodeMainEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBConnectionVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBDatasetFilterVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBFileOpenHandle;
import com.dci.intellij.dbn.vfs.file.DBObjectListVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionStatementVirtualFile;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.*;

public class DatabaseFileSystem extends VirtualFileSystem implements /*NonPhysicalFileSystem, */NamedComponent {
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

        private final String urlToken;
        private final String presentableUrlToken;

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

    private final Map<DBObjectRef, DBEditableObjectVirtualFile> filesCache = new ConcurrentHashMap<>();

    public DatabaseFileSystem() {
        ProjectUtil.projectClosed(project -> clearCachedFiles(project));
    }

    public static DatabaseFileSystem getInstance() {
        return (DatabaseFileSystem) VirtualFileManager.getInstance().getFileSystem(PROTOCOL);
        //return ApplicationManager.getApplication().getComponent(DatabaseFileSystem.class);
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
                        DBConsole console = connectionHandler.getConsoleBundle().getConsole(consoleName);
                        return Safe.call(console, target -> target.getVirtualFile());

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

    public static boolean isFileOpened(DBObject object) {
        Project project = object.getProject();
        DatabaseFileManager fileManager = DatabaseFileManager.getInstance(project);
        return fileManager.isFileOpened(object);
    }

    /********************************************************
     *                    GENERIC                           *
     ********************************************************/
    @NotNull
    static String createPath(DBVirtualFile virtualFile) {
        try {
            ConnectionId connectionId = virtualFile.getConnectionId();

            if (virtualFile instanceof DBConsoleVirtualFile) {
                DBConsoleVirtualFile file = (DBConsoleVirtualFile) virtualFile;
                return connectionId + PS + CONSOLES + file.getName();
            }

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

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DatabaseFileSystem";
    }

    /*********************************************************
     *              FileEditorManagerListener                *
     *********************************************************/
    public void connectAndOpenEditor(@NotNull DBObject object, @Nullable EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        if (isEditable(object)) {
            ConnectionAction.invoke(
                    "opening the object editor", false, object,
                    (action) -> {
                        Project project = object.getProject();
                        String title = "Opening editor (" + object.getQualifiedName() + ")";
                        ProgressRunnable runnable = (progress) -> openEditor(object, editorProviderId, scrollBrowser, focusEditor);

                        if (focusEditor)
                            Progress.prompt(project, title, true, runnable); else
                            Progress.background( project, title, true, runnable);
                    });
        }
    }

    public void openEditor(@NotNull DBObject object, @Nullable EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        if (isEditable(object) && !DBFileOpenHandle.isFileOpening(object)) {
            DBFileOpenHandle handle = DBFileOpenHandle.create(object).
                    withEditorProviderId(editorProviderId).
                    withEditorInstructions(NavigationInstructions.create(true, focusEditor, true)).
                    withBrowserInstructions(NavigationInstructions.create(false, false, scrollBrowser));

            try {
                handle.init();
                if (object.is(DBObjectProperty.SCHEMA_OBJECT)) {
                    object.initChildren();
                    openSchemaObject((DBFileOpenHandle<DBSchemaObject>) handle);

                } else {
                    DBObject parentObject = object.getParentObject();
                    if (parentObject.is(DBObjectProperty.SCHEMA_OBJECT)) {
                        parentObject.initChildren();
                        openChildObject(handle);
                    }
                }
            } catch (Throwable e) {
                handle.release();
            }
        }
    }

    private boolean isEditable(DBObject object) {
        if (Failsafe.check(object)) {
            DBContentType contentType = object.getContentType();
            boolean editable =
                    object.is(DBObjectProperty.SCHEMA_OBJECT) &&
                            (contentType.has(DBContentType.DATA) ||
                                    DatabaseFeature.OBJECT_SOURCE_EDITING.isSupported(object));

            if (!editable) {
                DBObject parentObject = object.getParentObject();
                if (parentObject instanceof DBSchemaObject) {
                    return isEditable(parentObject);
                }
            }
            return editable;
        }
        return false;
    }

    private void openSchemaObject(@NotNull DBFileOpenHandle<DBSchemaObject> handle) {
        DBSchemaObject object = handle.getObject();

        EditorProviderId editorProviderId = handle.getEditorProviderId();

        DBObjectRef objectRef = object.getRef();
        Project project = object.getProject();

        DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(project, objectRef);
        databaseFile.setSelectedEditorProviderId(editorProviderId);

        invokeFileOpen(handle, () -> {
            if (Failsafe.check(object)) {
                // open / reopen (select) the file
                if (isFileOpened(object) || promptFileOpen(databaseFile)) {
                    boolean focusEditor = handle.getEditorInstructions().isFocus();

                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    fileEditorManager.openFile(databaseFile, focusEditor);
                    NavigationInstructions instructions = focusEditor ? NavigationInstructions.FOCUS_SCROLL : NavigationInstructions.SCROLL;
                    EditorUtil.selectEditor(project, null, databaseFile, editorProviderId, instructions);
                }
            }
        });
    }

    private void openChildObject(DBFileOpenHandle handle) {
        DBObject object = handle.getObject();
        EditorProviderId editorProviderId = handle.getEditorProviderId();

        DBSchemaObject schemaObject = (DBSchemaObject) object.getParentObject();
        Project project = schemaObject.getProject();
        DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(project, schemaObject.getRef());
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        sourceCodeManager.ensureSourcesLoaded(schemaObject, false);

        invokeFileOpen(handle, () -> {
            if (Failsafe.check(schemaObject)) {
                // open / reopen (select) the file
                if (isFileOpened(schemaObject) || promptFileOpen(databaseFile)) {
                    boolean focusEditor = handle.getEditorInstructions().isFocus();

                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    FileEditor[] fileEditors = fileEditorManager.openFile(databaseFile, focusEditor);
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof SourceCodeMainEditor) {
                            SourceCodeMainEditor sourceCodeEditor = (SourceCodeMainEditor) fileEditor;
                            NavigationInstructions instructions = focusEditor ? NavigationInstructions.FOCUS_SCROLL : NavigationInstructions.SCROLL;
                            EditorUtil.selectEditor(project, fileEditor, databaseFile, editorProviderId, instructions);
                            sourceCodeEditor.navigateTo(object);
                            break;
                        }
                    }
                }
            }
        });
    }

    private static void invokeFileOpen(DBFileOpenHandle handle, Runnable opener) {
        if (ProgressMonitor.isCancelled()) {
            handle.release();
        } else {
            Dispatch.run(() -> {
                try {
                    boolean scrollBrowser = handle.getBrowserInstructions().isScroll();
                    DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(scrollBrowser);
                    opener.run();
                } finally {
                    DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(true);
                    handle.release();
                }
            });
        }
    }

    private static boolean promptFileOpen(@NotNull DBEditableObjectVirtualFile databaseFile) {
        DBSchemaObject object = databaseFile.getObject();
        Project project = object.getProject();
        DBContentType contentType = object.getContentType();
        if (contentType == DBContentType.DATA) {
            DBDataset dataset = (DBDataset) object;
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
            DatasetFilter filter = filterManager.getActiveFilter(dataset);

            if (filter == null) {
                DataEditorSettings settings = DataEditorSettings.getInstance(project);
                if (settings.getFilterSettings().isPromptFilterDialog()) {
                    int exitCode = filterManager.openFiltersDialog(dataset, true, false, settings.getFilterSettings().getDefaultFilterType());
                    return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                }
            }
        }
        else if (contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_SPEC_AND_BODY)) {
            DDLFileGeneralSettings ddlFileSettings = DDLFileSettings.getInstance(project).getGeneralSettings();
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            boolean ddlFileBinding = connectionHandler.getSettings().getDetailSettings().isEnableDdlFileBinding();
            if (ddlFileBinding && ddlFileSettings.isLookupDDLFilesEnabled()) {
                List<VirtualFile> attachedDDLFiles = databaseFile.getAttachedDDLFiles();
                if (attachedDDLFiles == null || attachedDDLFiles.isEmpty()) {
                    DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                    DBObjectRef<DBSchemaObject> objectRef = object.getRef();
                    List<VirtualFile> virtualFiles = fileAttachmentManager.lookupDetachedDDLFiles(objectRef);
                    if (virtualFiles.size() > 0) {
                        int exitCode = fileAttachmentManager.showFileAttachDialog(object, virtualFiles, true);
                        return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                    } else if (ddlFileSettings.isCreateDDLFilesEnabled()) {
                        MessageUtil.showQuestionDialog(
                                project, "No DDL file found",
                                "Could not find any DDL file for " + object.getQualifiedNameWithType() + ". Do you want to create one? \n" +
                                        "(You can disable this check in \"DDL File\" options)", MessageUtil.OPTIONS_YES_NO, 0,
                                (option) -> conditional(option == 0, () -> fileAttachmentManager.createDDLFile(objectRef)));

                    }
                }
            }
        }

        return true;
    }

    public void closeEditor(DBSchemaObject object) {
        VirtualFile virtualFile = findDatabaseFile(object);
        if (virtualFile != null) {
            DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(object.getProject());
            databaseFileManager.closeFile(virtualFile);
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
            if (file.getProject() == project) {
                objectRefs.remove();
                SafeDisposer.dispose(file);
            }
        }
    }

    public boolean isDatabaseUrl(String fileUrl) {
        return fileUrl.startsWith(PROTOCOL_PREFIX);
    }
}

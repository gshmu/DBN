package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.project.Projects;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.*;
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
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.file.*;
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

import static com.dci.intellij.dbn.common.dispose.Checks.*;
import static com.dci.intellij.dbn.common.message.MessageCallback.when;
import static com.dci.intellij.dbn.common.navigation.NavigationInstruction.*;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.FilePathType.*;

public class DatabaseFileSystem extends VirtualFileSystem implements /*NonPhysicalFileSystem, */NamedComponent {
    public static final char PS = '/';
    public static final String PSS = "" + '/';
    private static final String PROTOCOL = "db";
    private static final String PROTOCOL_PREFIX = PROTOCOL + "://";

    public enum FilePathType {
        OBJECTS("objects", "objects"),
        OBJECT_CONTENTS("object_contents", "object contents"),
        CONSOLES("consoles", "consoles"),
        SESSION_BROWSERS("session_browsers", "session browsers"),
        SESSION_STATEMENTS("session_statements", "session statements"),
        DATASET_FILTERS("dataset_filters", "dataset filters"),
        LOOSE_CONTENTS("loose_contents", "loose contents");

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

    private final Map<DBObjectRef<?>, DBEditableObjectVirtualFile> filesCache = new ConcurrentHashMap<>();

    public DatabaseFileSystem() {
        Projects.projectClosed(project -> clearCachedFiles(project));
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
        if (index < 0) return null;

        ConnectionId connectionId = ConnectionId.get(path.substring(0, index));
        ConnectionHandler connection = ConnectionHandler.get(connectionId);

        if (isNotValid(connection) || !connection.isEnabled())  return null;
        if (!allowFileLookup(connection)) return null;

        Project project = connection.getProject();
        String relativePath = path.substring(index + 1);
        if (CONSOLES.is(relativePath)) {
            String consoleName = CONSOLES.collate(relativePath);
            DBConsole console = connection.getConsoleBundle().getConsole(consoleName);
            return Safe.call(console, target -> target.getVirtualFile());

        } else if (SESSION_BROWSERS.is(relativePath)) {
            return connection.getSessionBrowserFile();

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

        return null;
    }

    public boolean isValidPath(String path, Project project) {
        if (path.startsWith(PROTOCOL_PREFIX)) {
            path = path.substring(PROTOCOL_PREFIX.length());
        }

        if (path.startsWith("null")) return false;

        int index = path.indexOf(PS);
        if (index < 0) return false;

        ConnectionId connectionId = ConnectionId.get(path.substring(0, index));
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionHandler connection = connectionManager.getConnection(connectionId);
        if (project.isInitialized() && connection == null) return false;

        String relativePath = path.substring(index + 1);
        for (FilePathType pathType : FilePathType.values()) {
            if (pathType.is(relativePath)) {
                return true;
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
        ConnectionHandler connection = ConnectionHandler.get(connectionId);
        if (connection == null) {
            path = path.replace(connectionId.id(), "UNKNOWN");
        } else {
            path = path.replace(connectionId.id(), connection.getName());
        }

        if (index > -1) {
            for (FilePathType value : FilePathType.values()) {
                path = path.replace(value.urlToken, value.presentableUrlToken);
            }

            path = path.replace(PSS, File.separator);
        }

        return path;
    }

    private boolean allowFileLookup(ConnectionHandler connection) {
        ConnectionDetailSettings connectionDetailSettings = connection.getSettings().getDetailSettings();
        if (connectionDetailSettings.isRestoreWorkspace()) {
            return true;
        } else {
            Project project = connection.getProject();
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            return settingsManager.isInitialised();
        }
    }

    @Nullable
    public DBEditableObjectVirtualFile findDatabaseFile(DBSchemaObject object) {
        DBObjectRef<?> objectRef = object.ref();
        return filesCache.get(objectRef);
    }

    @NotNull
    public DBEditableObjectVirtualFile findOrCreateDatabaseFile(@NotNull Project project, @NotNull DBObjectRef<?> objectRef) {
        return filesCache.compute(objectRef, (ref, file) ->
                isValid(file) && file.getProject() == project ? file : Read.conditional(() -> new DBEditableObjectVirtualFile(project, ref)));
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
    public static String createFilePath(DBVirtualFile virtualFile) {
        try {
            ConnectionId connectionId = virtualFile.getConnectionId();

            if (virtualFile instanceof DBConsoleVirtualFile) {
                DBConsoleVirtualFile file = (DBConsoleVirtualFile) virtualFile;
                return connectionId + PSS + CONSOLES + file.getName();
            }

            if (virtualFile instanceof DBConnectionVirtualFile) {
                DBConnectionVirtualFile file = (DBConnectionVirtualFile) virtualFile;
                return file.getConnectionId() + "";
            }

            if (virtualFile instanceof DBObjectVirtualFile) {
                DBObjectVirtualFile<?> file = (DBObjectVirtualFile<?>) virtualFile;
                DBObjectRef<?> objectRef = file.getObjectRef();
                return createObjectPath(objectRef);
            }

            if (virtualFile instanceof DBContentVirtualFile) {
                DBContentVirtualFile file = (DBContentVirtualFile) virtualFile;
                DBObjectRef<?> objectRef = file.getObject().ref();
                DBContentType contentType = file.getContentType();
                return objectRef.getConnectionId() + PSS + OBJECT_CONTENTS + contentType.name() + PS + objectRef.serialize();
            }

            if (virtualFile instanceof DBObjectListVirtualFile) {
                DBObjectListVirtualFile<?> file = (DBObjectListVirtualFile<?>) virtualFile;
                DBObjectList<?> objectList = file.getObjectList();
                DatabaseEntity parentElement = objectList.getParentEntity();
                String listName = objectList.getObjectType().getListName();
                String connectionPath = connectionId.id();
                if (parentElement instanceof DBObject) {
                    DBObject object = (DBObject) parentElement;
                    DBObjectRef<?> objectRef = object.ref();
                    return connectionPath + PSS + objectRef.serialize() + PSS + listName;
                } else {
                    return connectionPath + PSS + listName; }
            }

            if (virtualFile instanceof DBDatasetFilterVirtualFile) {
                DBDatasetFilterVirtualFile file = (DBDatasetFilterVirtualFile) virtualFile;
                return connectionId + PSS + DATASET_FILTERS + PSS + file.getDataset().ref().serialize();
            }

            if (virtualFile instanceof DBSessionBrowserVirtualFile) {
                DBSessionBrowserVirtualFile file = (DBSessionBrowserVirtualFile) virtualFile;
                return connectionId + PSS + SESSION_BROWSERS + PSS + file.getName();

            }

            if (virtualFile instanceof DBSessionStatementVirtualFile) {
                DBSessionStatementVirtualFile file = (DBSessionStatementVirtualFile) virtualFile;
                return connectionId + PSS + SESSION_STATEMENTS + PSS + file.getName();
            }

            if (virtualFile instanceof DBLooseContentVirtualFile) {
                DBLooseContentVirtualFile file = (DBLooseContentVirtualFile) virtualFile;
                return connectionId + PSS + LOOSE_CONTENTS + PSS + DBObjectRef.serialised(file.getObject());
            }

            throw new IllegalArgumentException("File of type " + virtualFile.getClass() + " is not supported");
        } catch (ProcessCanceledException e) {
            return "DISPOSED/"+ UUID.randomUUID();
        }
    }

    @NotNull
    public static String createObjectPath(DBObjectRef<?> object) {
        return object.getConnectionId() + PSS + OBJECTS + object.serialize();
    }

    @NotNull
    public static String createFileUrl(DBVirtualFile virtualFile) {
        return PROTOCOL_PREFIX + createFilePath(virtualFile);
    }

    @NotNull
    public static String createObjectUrl(DBObjectRef<?> object) {
        return PROTOCOL_PREFIX + createObjectPath(object);
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

    @Override
    protected void deleteFile(Object o, @NotNull VirtualFile virtualFile) {}

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
        if (!isEditable(object)) return;

        ConnectionAction.invoke(
                "opening the object editor", false, object,
                (action) -> {
                    Project project = object.getProject();
                    String title = "Opening editor (" + object.getQualifiedName() + ")";
                    if (focusEditor) {
                        Progress.prompt(project, title, true, i -> openEditor(object, editorProviderId, scrollBrowser, true));
                    } else {
                        Background.run(() -> openEditor(object, editorProviderId, scrollBrowser, false));
                    }
                });
    }

    public void openEditor(@NotNull DBObject object, @Nullable EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        if (!isEditable(object)) return;
        if (DBFileOpenHandle.isFileOpening(object)) return;


        NavigationInstructions editorInstructions = NavigationInstructions.create().with(OPEN).with(SCROLL).with(FOCUS, focusEditor);
        NavigationInstructions browserInstructions = NavigationInstructions.create().with(SCROLL, scrollBrowser);
        DBFileOpenHandle<?> handle = DBFileOpenHandle.create(object).
                withEditorProviderId(editorProviderId).
                withEditorInstructions(editorInstructions).
                withBrowserInstructions(browserInstructions);

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
        } finally {
            handle.release();
        }
    }

    private boolean isEditable(DBObject object) {
        if (isNotValid(object)) return false;

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

    private void openSchemaObject(@NotNull DBFileOpenHandle<DBSchemaObject> handle) {
        DBSchemaObject object = handle.getObject();

        EditorProviderId editorProviderId = handle.getEditorProviderId();

        DBObjectRef<?> objectRef = object.ref();
        Project project = object.getProject();

        DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(project, objectRef);
        databaseFile.setSelectedEditorProviderId(editorProviderId);

        invokeFileOpen(handle, () -> {
            if (allValid(object, databaseFile)) {
                // open / reopen (select) the file
                if (isFileOpened(object) || promptFileOpen(databaseFile)) {
                    boolean focusEditor = handle.getEditorInstructions().isFocus();

                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    fileEditorManager.openFile(databaseFile, focusEditor);
                    NavigationInstructions instructions = NavigationInstructions.create().
                            with(SCROLL).
                            with(FOCUS, focusEditor);
                    Editors.selectEditor(project, null, databaseFile, editorProviderId, instructions);
                }
            }
        });
    }

    private void openChildObject(DBFileOpenHandle<?> handle) {
        DBObject object = handle.getObject();
        EditorProviderId editorProviderId = handle.getEditorProviderId();

        DBSchemaObject schemaObject = (DBSchemaObject) object.getParentObject();
        Project project = schemaObject.getProject();
        DBEditableObjectVirtualFile databaseFile = findOrCreateDatabaseFile(project, schemaObject.ref());
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        sourceCodeManager.ensureSourcesLoaded(schemaObject, false);

        invokeFileOpen(handle, () -> {
            if (isNotValid(schemaObject)) return;

            // open / reopen (select) the file
            if (isFileOpened(schemaObject) || promptFileOpen(databaseFile)) {
                boolean focusEditor = handle.getEditorInstructions().isFocus();

                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                FileEditor[] fileEditors = fileEditorManager.openFile(databaseFile, focusEditor);
                for (FileEditor fileEditor : fileEditors) {
                    if (fileEditor instanceof SourceCodeMainEditor) {
                        SourceCodeMainEditor sourceCodeEditor = (SourceCodeMainEditor) fileEditor;
                        NavigationInstructions instructions = NavigationInstructions.create().
                                with(SCROLL).
                                with(FOCUS, focusEditor);

                        Editors.selectEditor(project, fileEditor, databaseFile, editorProviderId, instructions);
                        sourceCodeEditor.navigateTo(object);
                        break;
                    }
                }
            }
        });
    }

    private static void invokeFileOpen(DBFileOpenHandle<?> handle, Runnable opener) {
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
            ConnectionHandler connection = object.getConnection();
            boolean ddlFileBinding = connection.getSettings().getDetailSettings().isEnableDdlFileBinding();
            if (ddlFileBinding && ddlFileSettings.isLookupDDLFilesEnabled()) {
                List<VirtualFile> attachedDDLFiles = databaseFile.getAttachedDDLFiles();
                if (attachedDDLFiles == null || attachedDDLFiles.isEmpty()) {
                    DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                    DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.of(object);
                    List<VirtualFile> virtualFiles = fileAttachmentManager.lookupDetachedDDLFiles(objectRef);
                    if (virtualFiles.size() > 0) {
                        int exitCode = fileAttachmentManager.showFileAttachDialog(object, virtualFiles, true);
                        return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                    } else if (ddlFileSettings.isCreateDDLFilesEnabled()) {
                        Messages.showQuestionDialog(
                                project, "No DDL file found",
                                "Could not find any DDL file for " + object.getQualifiedNameWithType() + ". Do you want to create one? \n" +
                                        "(You can disable this check in \"DDL File\" options)", Messages.OPTIONS_YES_NO, 0,
                                option -> when(option == 0, () -> fileAttachmentManager.createDDLFile(objectRef)));

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
        VirtualFile virtualFile = findOrCreateDatabaseFile(project, object.ref());
        if (fileEditorManager.isFileOpen(virtualFile)) {
            fileEditorManager.closeFile(virtualFile);
            fileEditorManager.openFile(virtualFile, false);
        }
    }

    void clearCachedFiles(Project project) {
        Iterator<DBObjectRef<?>> objectRefs = filesCache.keySet().iterator();
        while (objectRefs.hasNext()) {
            DBObjectRef<?> objectRef = objectRefs.next();
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

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
}

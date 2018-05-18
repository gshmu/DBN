package com.dci.intellij.dbn.vfs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;

public class DatabaseFileSystem extends VirtualFileSystem implements /*NonPhysicalFileSystem, */ApplicationComponent {
    public static final String PROTOCOL = "db";
    public static final String PROTOCOL_PREFIX = PROTOCOL + "://";

    public static final IOException READONLY_FILE_SYSTEM = new IOException("Operation not supported");
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

    @Nullable
    public VirtualFile findFileByPath(@NotNull @NonNls String url) {
        int startIndex = 0;
        if (url.startsWith(PROTOCOL_PREFIX)) {
            startIndex = PROTOCOL_PREFIX.length();
        }

        int index = url.indexOf('/', startIndex);

        if (index > -1) {
            ConnectionId connectionId = ConnectionId.get(url.substring(startIndex, index));
            ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            if (connectionHandler != null && !connectionHandler.isDisposed() && connectionHandler.isEnabled()) {
                String objectPath = url.substring(index + 1);
                if (isValidPath(objectPath) && allowFileLookup(connectionHandler)) {
                    if (objectPath.startsWith("console#")) {
                        String consoleName = objectPath.substring(8);
                        return connectionHandler.getConsoleBundle().getConsole(consoleName);

                    } else if (objectPath.startsWith("session_browser#")) {
                        return connectionHandler.getSessionBrowserFile();

                    } else if (objectPath.startsWith("object#")) {
                        String identifier = objectPath.substring(7);
                        DBObjectRef objectRef = new DBObjectRef(connectionId, identifier);
                        DBObject object = objectRef.get();
                        if (object != null && object.is(DBObjectProperty.EDITABLE)) {
                            return findOrCreateDatabaseFile((DBSchemaObject) object);
                        }
                    } else if (objectPath.startsWith("object_")) {
                        int typeEndIndex = objectPath.indexOf("#");
                        String contentTypeStr = objectPath.substring(7, typeEndIndex);
                        DBContentType contentType = DBContentType.valueOf(contentTypeStr.toUpperCase());
                        String identifier = objectPath.substring(typeEndIndex + 1);
                        DBObjectRef objectRef = new DBObjectRef(connectionId, identifier);
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

    @NotNull
    public static String createPath(DBObjectRef objectRef, DBContentType contentType) {
        StringBuilder buffer = new StringBuilder(objectRef.getFileName());
        DBObjectRef parentRef = objectRef.getParent();
        while (parentRef != null) {
            buffer.insert(0, File.separatorChar);
            buffer.insert(0, parentRef.getObjectName());
            parentRef = parentRef.getParent();
        }
        buffer.insert(0, File.separatorChar);
        buffer.insert(0, objectRef.getObjectType().getListName());
        buffer.insert(0, ']');
        ConnectionHandler connectionHandler = objectRef.lookupConnectionHandler();
        buffer.insert(0, connectionHandler == null ? "UNKNOWN" : connectionHandler.getName());
        buffer.insert(0, '[');
        if (contentType != null) {
            buffer.append('.');
            switch (contentType) {
                case CODE: buffer.append("CODE"); break;
                case DATA: buffer.append("DATA"); break;
                case CODE_SPEC: buffer.append("SPEC"); break;
                case CODE_BODY: buffer.append("BODY"); break;
                case CODE_SPEC_AND_BODY: buffer.append("CODE"); break;
                case CODE_AND_DATA: buffer.append("CODE_AND_DATA"); break;
            }
        }

        return buffer.toString();
    }

    @NotNull
    public static String createPath(DBObjectList objectList) {
        StringBuilder buffer = new StringBuilder(objectList.getName());
        GenericDatabaseElement parent = objectList.getParentElement();
        while (parent != null) {
            buffer.insert(0, File.separatorChar);
            buffer.insert(0, parent.getName());
            parent = parent.getParentElement();
        }

        buffer.insert(0, "] ");
        ConnectionHandler connectionHandler = objectList.getConnectionHandler();
        buffer.insert(0, connectionHandler.getName());
        buffer.insert(0, '[');

        return buffer.toString();
    }

    @NotNull
    public static String createPath(DBObjectRef objectRef) {
        return createPath(objectRef, null);
    }

    @NotNull
    public static String createUrl(DBObjectList objectList) {
        GenericDatabaseElement parentElement = objectList.getParentElement();
        if (parentElement instanceof DBObject) {
            DBObject parentObject = (DBObject) parentElement;
            return createUrl(parentObject.getRef()) + objectList.getName();
        }

        if (parentElement instanceof DBObjectBundle) {
            DBObjectBundle objectBundle = (DBObjectBundle) parentElement;
            return createUrl(objectBundle.getConnectionHandler()) + "/bundle";
        }
        return createUrl(objectList.getConnectionHandler()) + "/unknown";
    }


    public static String createUrl(DBObjectRef objectRef) {
        if (objectRef == null) {
            return PROTOCOL + "://" + UUID.randomUUID() + "/null";
        } else {
            ConnectionId connectionId = objectRef.getConnectionId();
            return PROTOCOL + "://" + connectionId + "/object#" + objectRef.serialize()/* + "." + getDefaultExtension(object)*/;
        }
    }

    public static String createUrl(DBObjectRef objectRef, DBContentType contentType) {
        if (objectRef == null) {
            return PROTOCOL + "://" + UUID.randomUUID() + "/null";
        } else {
            ConnectionId connectionId = objectRef.getConnectionId();
            return PROTOCOL + "://" + connectionId + "/object_" + contentType.name().toLowerCase() + "#" + objectRef.serialize();
        }
    }

    public static String createPath(ConnectionHandler connectionHandler) {
        return '[' + connectionHandler.getName() + ']';

    }

    public static String createUrl(ConnectionHandler connectionHandler) {
        return PROTOCOL + "://" + connectionHandler.getId();
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

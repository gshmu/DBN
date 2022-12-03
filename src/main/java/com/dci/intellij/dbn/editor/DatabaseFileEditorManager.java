package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.code.SourceCodeMainEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBFileOpenHandle;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.common.component.Components.projectService;
import static com.dci.intellij.dbn.common.dispose.Checks.allValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.message.MessageCallback.when;
import static com.dci.intellij.dbn.common.navigation.NavigationInstruction.*;
import static com.dci.intellij.dbn.editor.DatabaseFileEditorManager.COMPONENT_NAME;

@State(
        name = COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseFileEditorManager extends ProjectComponentBase {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseFileEditorManager";

    public static DatabaseFileEditorManager getInstance(Project project) {
        return projectService(project, DatabaseFileEditorManager.class);
    }

    public DatabaseFileEditorManager(Project project) {
        super(project, COMPONENT_NAME);
    }


    public void connectAndOpenEditor(@NotNull DBObject object, @Nullable EditorProviderId editorProviderId, boolean scrollBrowser, boolean focusEditor) {
        if (!isEditable(object)) return;

        ConnectionAction.invoke("opening the object editor", false, object, action -> {
            if (focusEditor) {
                Project project = object.getProject();
                Progress.prompt(project, object, true,
                        "Opening object editor",
                        "Opening editor for " + object.getQualifiedNameWithType(),
                        progress -> openEditor(object, editorProviderId, scrollBrowser, true));
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

    private void openSchemaObject(@NotNull DBFileOpenHandle<DBSchemaObject> handle) {
        DBSchemaObject object = handle.getObject();

        EditorProviderId editorProviderId = handle.getEditorProviderId();

        DBObjectRef<?> objectRef = object.ref();
        Project project = object.getProject();

        DBEditableObjectVirtualFile databaseFile = getFileSystem().findOrCreateDatabaseFile(project, objectRef);
        databaseFile.setSelectedEditorProviderId(editorProviderId);

        invokeFileOpen(handle, () -> {
            if (allValid(object, databaseFile)) {
                // open / reopen (select) the file
                if (DatabaseFileSystem.isFileOpened(object) || promptFileOpen(databaseFile)) {
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
        DBEditableObjectVirtualFile databaseFile = getFileSystem().findOrCreateDatabaseFile(project, schemaObject.ref());
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        sourceCodeManager.ensureSourcesLoaded(schemaObject, false);

        invokeFileOpen(handle, () -> {
            if (isNotValid(schemaObject)) return;

            // open / reopen (select) the file
            if (DatabaseFileSystem.isFileOpened(schemaObject) || promptFileOpen(databaseFile)) {
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
            if (ddlFileBinding && ddlFileSettings.isDdlFilesLookupEnabled()) {
                List<VirtualFile> attachedDDLFiles = databaseFile.getAttachedDDLFiles();
                if (attachedDDLFiles == null || attachedDDLFiles.isEmpty()) {
                    DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                    DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.of(object);
                    List<VirtualFile> virtualFiles = fileAttachmentManager.lookupDetachedDDLFiles(objectRef);
                    if (virtualFiles.size() > 0) {
                        int exitCode = fileAttachmentManager.showFileAttachDialog(object, virtualFiles, true);
                        return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                    } else if (ddlFileSettings.isDdlFilesCreationEnabled()) {
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
        VirtualFile virtualFile = getFileSystem().findDatabaseFile(object);
        if (virtualFile == null) return;

        DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(object.getProject());
        databaseFileManager.closeFile(virtualFile);
    }

    public void reopenEditor(DBSchemaObject object) {
        Project project = object.getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile virtualFile = getFileSystem().findOrCreateDatabaseFile(project, object.ref());
        if (!fileEditorManager.isFileOpen(virtualFile)) return;

        fileEditorManager.closeFile(virtualFile);
        fileEditorManager.openFile(virtualFile, false);
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


    private static DatabaseFileSystem getFileSystem() {
        return DatabaseFileSystem.getInstance();
    }
}

package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.diff.MergeAction;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.editor.DBLanguageFileEditorListener;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBDatasetTrigger;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.text.DateFormatUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;
import static com.dci.intellij.dbn.common.util.CommonUtil.list;
import static com.dci.intellij.dbn.common.util.MessageUtil.*;
import static com.dci.intellij.dbn.common.util.NamingUtil.unquote;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.*;
import static com.intellij.openapi.util.text.StringUtil.equalsIgnoreCase;

@State(
    name = SourceCodeManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class SourceCodeManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, SettingsSavingComponent {
    public static final String COMPONENT_NAME = "DBNavigator.Project.SourceCodeManager";

    public static SourceCodeManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, SourceCodeManager.class);
    }

    private SourceCodeManager(@NotNull Project project) {
        super(project);
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(OverrideReadonlyFragmentModificationHandler.INSTANCE);

        ProjectEvents.subscribe(project, this, DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        ProjectEvents.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        ProjectEvents.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, new DBLanguageFileEditorListener());
    }


    private final DataDefinitionChangeListener dataDefinitionChangeListener = new DataDefinitionChangeListener() {
        @Override
        public void dataDefinitionChanged(DBSchema schema, DBObjectType objectType) {
        }

        @Override
        public void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject) {
            DBEditableObjectVirtualFile databaseFile = schemaObject.getCachedVirtualFile();
            if (databaseFile != null) {
                if (databaseFile.isModified()) {
                    showQuestionDialog(
                            getProject(), "Unsaved changes",
                            "The " + schemaObject.getQualifiedNameWithType() + " has been updated in database. You have unsaved changes in the object editor.\n" +
                                    "Do you want to discard the changes and reload the updated database version?",
                            new String[]{"Reload", "Keep changes"}, 0,
                            (option) -> conditional(option == 0,
                                    () -> reloadAndUpdateEditors(databaseFile, false)));
                } else {
                    reloadAndUpdateEditors(databaseFile, true);
                }

            }
        }
    };

    private final EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void editModeChanged(Project project, DBContentVirtualFile databaseContentFile) {
            if (databaseContentFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseContentFile;
                if (sourceCodeFile.is(MODIFIED)) {
                    loadSourceCode(sourceCodeFile, true);
                }
            }
        }
    };

    private final FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            FileEditor newEditor = event.getNewEditor();
            if (newEditor instanceof SourceCodeEditor) {
                SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) newEditor;
                DBEditableObjectVirtualFile databaseFile = sourceCodeEditor.getVirtualFile().getMainDatabaseFile();
                for (DBSourceCodeVirtualFile sourceCodeFile : databaseFile.getSourceCodeFiles()) {
                    if (!sourceCodeFile.isLoaded()) {
                        loadSourceCode(sourceCodeFile, false);
                    }
                }

            }

        }
    };

    private void reloadAndUpdateEditors(DBEditableObjectVirtualFile databaseFile, boolean startInBackground) {
        Project project = getProject();
        if (databaseFile.isContentLoaded()) {
            if (startInBackground)
                Progress.background(project, "Reloading object source code", false, (progress) -> reloadAndUpdateEditors(databaseFile)); else
                Progress.prompt(project, "Reloading object source code", false, (progress) -> reloadAndUpdateEditors(databaseFile));
        }
    }

    private void reloadAndUpdateEditors(DBEditableObjectVirtualFile databaseFile) {
        List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
        for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
            loadSourceCode(sourceCodeFile, true);
        }
    }

    public void ensureSourcesLoaded(@NotNull DBSchemaObject schemaObject, boolean notifyError) {
        DBEditableObjectVirtualFile editableObjectFile = schemaObject.getEditableVirtualFile();
        List<DBSourceCodeVirtualFile> sourceCodeFiles = editableObjectFile.getSourceCodeFiles();
        for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
            if (!sourceCodeFile.isLoaded()) {
                loadSourceFromDatabase(sourceCodeFile, false, notifyError);
            }
        }
    }

    private void loadSourceFromDatabase(@NotNull DBSourceCodeVirtualFile sourceCodeFile, boolean force, boolean notifyError) {
        Synchronized.run(
                "LOAD_SOURCE:" + sourceCodeFile.getUrl(),
                () -> {
                    boolean initialLoad = !sourceCodeFile.isLoaded();
                    if (sourceCodeFile.isNot(LOADING) && (initialLoad || force)) {
                        sourceCodeFile.set(LOADING, true);
                        EditorUtil.setEditorsReadonly(sourceCodeFile, true);
                        Project project = getProject();
                        DBSchemaObject object = sourceCodeFile.getObject();

                        ProjectEvents.notify(project,
                                SourceCodeManagerListener.TOPIC,
                                (listener) -> listener.sourceCodeLoading(sourceCodeFile));
                        try {
                            sourceCodeFile.loadSourceFromDatabase();
                        } catch (SQLException e) {
                            sourceCodeFile.setSourceLoadError(e.getMessage());
                            sourceCodeFile.set(MODIFIED, false);
                            if (notifyError) {
                                String objectDesc = object.getQualifiedNameWithType();
                                sendErrorNotification(
                                        NotificationGroup.SOURCE_CODE,
                                        "Could not load sourcecode for {0} from database: {1}", objectDesc, e);
                            }
                        } finally {
                            sourceCodeFile.set(LOADING, false);
                            ProjectEvents.notify(project,
                                    SourceCodeManagerListener.TOPIC,
                                    (listener) -> listener.sourceCodeLoaded(sourceCodeFile, initialLoad));
                        }
                    }
                });
    }

    private void saveSourceToDatabase(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor, @Nullable Runnable successCallback) {
        DBSchemaObject object = sourceCodeFile.getObject();
        DBContentType contentType = sourceCodeFile.getContentType();

        if (sourceCodeFile.isNot(SAVING)) {
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
            if (!debuggerManager.checkForbiddenOperation(sourceCodeFile.getConnectionHandler())) {
                return;
            }

            sourceCodeFile.set(SAVING, true);
            Project project = getProject();
            try {
                Document document = Failsafe.nn(DocumentUtil.getDocument(sourceCodeFile));
                DocumentUtil.saveDocument(document);

                DBLanguagePsiFile psiFile = sourceCodeFile.getPsiFile();
                if (psiFile == null || psiFile.getFirstChild() == null || isValidObjectTypeAndName(psiFile, object, contentType)) {
                    ProgressMonitor.setTaskDescription("Checking for third party changes on " + object.getQualifiedNameWithType());
                    boolean isChangedInDatabase = sourceCodeFile.isChangedInDatabase(true);
                    if (isChangedInDatabase && sourceCodeFile.isMergeRequired()) {
                        String presentableChangeTime =
                                DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(object) ?
                                    DateFormatUtil.formatPrettyDateTime(sourceCodeFile.getDatabaseChangeTimestamp()).toLowerCase() : "";
                        String message =
                                "The " + object.getQualifiedNameWithType() +
                                        " was changed in database by another user " + presentableChangeTime + "." +
                                        "\nYou must merge the changes before saving.";

                        showWarningDialog(project,"Version conflict",message,
                                options("Merge Changes", "Cancel"), 0,
                                (option) -> {
                                    if (option == 0) {
                                        Progress.prompt(project,
                                                "Loading database source code", false,
                                                (progress) -> {
                                                    try {
                                                        SourceCodeContent sourceCodeContent = loadSourceFromDatabase(object, contentType);
                                                        String databaseContent = sourceCodeContent.getText().toString();
                                                        SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
                                                        diffManager.openCodeMergeDialog(databaseContent, sourceCodeFile, fileEditor, MergeAction.SAVE);
                                                    } catch (SQLException e) {
                                                        showErrorDialog(project, "Could not load database sources.", e);
                                                    }
                                                });

                                    } else {
                                        sourceCodeFile.set(SAVING, false);
                                    }
                                });

                    } else {
                        storeSourceToDatabase(sourceCodeFile, fileEditor, successCallback);
                    }

                } else {
                    String message = "You are not allowed to change the name or the type of the object";
                    sourceCodeFile.set(SAVING, false);
                    showErrorDialog(project, "Illegal action", message);
                }
            } catch (Exception ex) {
                showErrorDialog(project, "Could not save changes to database.", ex);
                sourceCodeFile.set(SAVING, false);
            }
        }
    }

    public SourceCodeContent loadSourceFromDatabase(@NotNull DBSchemaObject object, DBContentType contentType) throws SQLException {
        ProgressMonitor.setTaskDescription("Loading source code of " + object.getQualifiedNameWithType());
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        boolean optionalContent = contentType == DBContentType.CODE_BODY;

        String sourceCode = DatabaseInterface.call(true,
                connectionHandler,
                (provider, connection) -> {
                    ResultSet resultSet = null;
                    try {
                        DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                        resultSet = loadSourceFromDatabase(
                                object,
                                contentType,
                                metadataInterface,
                                connection);

                        StringBuilder buffer = new StringBuilder();
                        while (resultSet != null && resultSet.next()) {
                            String codeLine = resultSet.getString("SOURCE_CODE");
                            buffer.append(codeLine);
                        }

                        if (buffer.length() == 0 && !optionalContent)
                            throw new SQLException("Source lookup returned empty");

                        return StringUtil.removeCharacter(buffer.toString(), '\r');
                    } finally {
                        ResourceUtil.close(resultSet);
                    }
                });

        SourceCodeContent sourceCodeContent = new SourceCodeContent(sourceCode);

        String objectName = object.getName();
        DBObjectType objectType = object.getObjectType();

        DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
        ddlInterface.computeSourceCodeOffsets(sourceCodeContent, objectType.getTypeId(), objectName);
        return sourceCodeContent;
    }

    @Nullable
    private static ResultSet loadSourceFromDatabase(
            @NotNull DBSchemaObject object,
            DBContentType contentType,
            DatabaseMetadataInterface metadataInterface,
            @NotNull DBNConnection connection) throws SQLException {

        DBObjectType objectType = object.getObjectType();
        String schemaName = object.getSchema().getName();
        String objectName = object.getName();
        short objectOverload = object.getOverload();

        switch (objectType) {
            case VIEW:
                return metadataInterface.loadViewSourceCode(
                        schemaName,
                        objectName,
                        connection);

            case MATERIALIZED_VIEW:
                return metadataInterface.loadMaterializedViewSourceCode(
                        schemaName,
                        objectName,
                        connection);

            case DATABASE_TRIGGER:
                return metadataInterface.loadDatabaseTriggerSourceCode(
                        schemaName,
                        objectName,
                        connection);

            case DATASET_TRIGGER:
                DBDatasetTrigger trigger = (DBDatasetTrigger) object;
                String datasetSchemaName = trigger.getDataset().getSchema().getName();
                String datasetName = trigger.getDataset().getName();
                return metadataInterface.loadDatasetTriggerSourceCode(
                        datasetSchemaName,
                        datasetName,
                        schemaName,
                        objectName,
                        connection);

            case FUNCTION:
                return metadataInterface.loadObjectSourceCode(
                        schemaName,
                        objectName,
                        "FUNCTION",
                        objectOverload,
                        connection);

            case PROCEDURE:
                return metadataInterface.loadObjectSourceCode(
                        schemaName,
                        objectName,
                        "PROCEDURE",
                        objectOverload,
                        connection);

            case TYPE:
                String typeContent =
                        contentType == DBContentType.CODE_SPEC ? "TYPE" :
                        contentType == DBContentType.CODE_BODY ? "TYPE BODY" : null;

                return metadataInterface.loadObjectSourceCode(
                        schemaName,
                        objectName,
                        typeContent,
                        connection);

            case PACKAGE:
                String packageContent =
                        contentType == DBContentType.CODE_SPEC ? "PACKAGE" :
                        contentType == DBContentType.CODE_BODY ? "PACKAGE BODY" : null;

                return metadataInterface.loadObjectSourceCode(
                        schemaName,
                        objectName,
                        packageContent,
                        connection);

            default:
                return null;
        }
    }

    @NotNull
    public ChangeTimestamp loadChangeTimestamp(@NotNull DBSchemaObject object, DBContentType contentType) throws SQLException{
        if (DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(object)) {
            ProgressMonitor.setTaskDescription("Loading timestamp for " + object.getQualifiedNameWithType());
            ConnectionHandler connectionHandler = object.getConnectionHandler();

            Timestamp timestamp = DatabaseInterface.call(true,
                    connectionHandler,
                    (provider, connection) -> {
                        ResultSet resultSet = null;
                        try {
                            String schemaName = object.getSchema().getName();
                            String objectName = object.getName();
                            String contentQualifier = getContentQualifier(object.getObjectType(), contentType);

                            resultSet = provider.getMetadataInterface().loadObjectChangeTimestamp(
                                    schemaName,
                                    objectName,
                                    contentQualifier,
                                    connection);

                            return resultSet.next() ? resultSet.getTimestamp(1) : null;
                        } finally {
                            ResourceUtil.close(resultSet);
                        }
                    });

            if (timestamp != null) {
                return new ChangeTimestamp(timestamp);
            }
        }

        return new ChangeTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    private static String getContentQualifier(DBObjectType objectType, DBContentType contentType) {
        switch (objectType) {
            case FUNCTION:         return "FUNCTION";
            case PROCEDURE:        return "PROCEDURE";
            case VIEW:             return "VIEW";
            case DATASET_TRIGGER:  return "TRIGGER";
            case DATABASE_TRIGGER: return "TRIGGER";
            case PACKAGE:
                return
                    contentType == DBContentType.CODE_SPEC ? "PACKAGE" :
                    contentType == DBContentType.CODE_BODY ? "PACKAGE BODY" : null;
            case TYPE:
                return
                    contentType == DBContentType.CODE_SPEC ? "TYPE" :
                    contentType == DBContentType.CODE_BODY ? "TYPE BODY" : null;
        }
        return null;
    }

    private boolean isValidObjectTypeAndName(@NotNull DBLanguagePsiFile psiFile, @NotNull DBSchemaObject object, DBContentType contentType) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
        if (ddlInterface.includesTypeAndNameInSourceContent(object.getObjectType().getTypeId())) {
            PsiElement psiElement = PsiUtil.getFirstLeaf(psiFile);

            String typeName = object.getTypeName();
            String subtypeName = contentType.getObjectTypeSubname();
            String objectName = object.getName();
            String schemaName = object.getSchema().getName();

            if (psiElement == null || !equalsIgnoreCase(psiElement.getText(), typeName)) {
                return false;
            }

            if (subtypeName != null) {
                psiElement = PsiUtil.getNextLeaf(psiElement);
                if (psiElement == null || !equalsIgnoreCase(psiElement.getText(), subtypeName)) {
                    return false;
                }
            }

            psiElement = PsiUtil.getNextLeaf(psiElement);
            if (psiElement == null) {
                return false;
            }

            if (equalsIgnoreCase(text(psiElement), schemaName)) {
                psiElement = PsiUtil.getNextLeaf(psiElement) ;
                if (psiElement == null || !psiElement.getText().equals(".")) {
                    return false;
                } else {
                    psiElement = PsiUtil.getNextLeaf(psiElement);
                    if (psiElement == null || !equalsIgnoreCase(text(psiElement), objectName)) {
                        return false;
                    }
                }
            } else {
                if (!equalsIgnoreCase(text(psiElement), objectName)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String text(@NotNull PsiElement psiElement) {
        return unquote(psiElement.getText());
    }

    public void storeSourceToDatabase(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor, @Nullable Runnable successCallback) {
        Project project = getProject();
        Progress.prompt(project, "Saving sources to database", false, (progress) -> {
            try {
                sourceCodeFile.saveSourceToDatabase();
                ProjectEvents.notify(project,
                        SourceCodeManagerListener.TOPIC,
                        (listener) -> listener.sourceCodeSaved(sourceCodeFile, fileEditor));

            } catch (SQLException e) {
                showErrorDialog(project, "Could not save changes to database.", e);
            } finally {
                sourceCodeFile.set(SAVING, false);
            }
            if (successCallback != null) successCallback.run();
        });
    }

    public BasePsiElement getObjectNavigationElement(@NotNull DBSchemaObject parentObject, DBContentType contentType, DBObjectType objectType, CharSequence objectName) {
        DBEditableObjectVirtualFile editableObjectFile = parentObject.getEditableVirtualFile();
        DBContentVirtualFile contentFile = editableObjectFile.getContentFile(contentType);
        if (contentFile != null) {
            PSQLFile file = (PSQLFile) PsiUtil.getPsiFile(getProject(), contentFile);
            if (file != null) {
                return
                    contentType == DBContentType.CODE_BODY ? file.lookupObjectDeclaration(objectType, objectName) :
                    contentType == DBContentType.CODE_SPEC ? file.lookupObjectSpecification(objectType, objectName) : null;
            }
        }

        return null;
    }

    public void navigateToObject(@NotNull DBSchemaObject parentObject, @NotNull BasePsiElement basePsiElement) {
        DBEditableObjectVirtualFile editableObjectFile = parentObject.getEditableVirtualFile();
        DBLanguagePsiFile psiFile = basePsiElement.getFile();
        VirtualFile elementVirtualFile = psiFile.getVirtualFile();
        if (elementVirtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) elementVirtualFile;
            BasicTextEditor textEditor = EditorUtil.getTextEditor(sourceCodeFile);
            if (textEditor != null) {
                Project project = getProject();
                EditorProviderId editorProviderId = textEditor.getEditorProviderId();
                FileEditor fileEditor = EditorUtil.selectEditor(project, textEditor, editableObjectFile, editorProviderId, NavigationInstructions.OPEN);
                basePsiElement.navigateInEditor(fileEditor, NavigationInstructions.FOCUS_SCROLL);
            }
        }
    }

    @Override
    public boolean canCloseProject() {
        boolean canClose = true;
        Project project = getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();

        for (VirtualFile openFile : openFiles) {
            if (openFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) openFile;
                if (databaseFile.isModified()) {
                    canClose = false;
                    if (!databaseFile.isSaving()) {
                        DBSchemaObject object = databaseFile.getObject();
                        String objectDescription = object.getQualifiedNameWithType();
                        Project objectProject = object.getProject();

                        CodeEditorSettings codeEditorSettings = CodeEditorSettings.getInstance(objectProject);
                        CodeEditorConfirmationSettings confirmationSettings = codeEditorSettings.getConfirmationSettings();
                        confirmationSettings.getExitOnChanges().resolve(
                                list(objectDescription),
                                option -> {
                                    switch (option) {
                                        case SAVE: saveSourceCodeChanges(databaseFile, () -> closeProject()); break;
                                        case DISCARD: revertSourceCodeChanges(databaseFile, () -> closeProject()); break;
                                        case SHOW: {
                                            List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                                            for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                                                if (sourceCodeFile.is(MODIFIED)) {
                                                    SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(objectProject);
                                                    diffManager.opedDatabaseDiffWindow(sourceCodeFile);
                                                }
                                            }

                                        } break;
                                        case CANCEL: break;
                                    }
                                });
                    }
                }
            }
        }
        return canClose;
    }

    public void loadSourceCode(@NotNull DBSourceCodeVirtualFile sourceCodeFile, boolean force) {
        String objectDescription = sourceCodeFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke("loading the source code", false, sourceCodeFile,
                (action) -> Progress.background(getProject(), "Loading source code for " + objectDescription, false,
                        (progress) -> loadSourceFromDatabase(sourceCodeFile, force, false)));
    }

    public void saveSourceCode(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor, Runnable successCallback) {
        String objectDescription = sourceCodeFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke("saving the source code", false, sourceCodeFile,
                (action) -> Progress.prompt(getProject(), "Saving source code for " + objectDescription, false,
                        (progress) -> saveSourceToDatabase(sourceCodeFile, fileEditor, successCallback)));
    }

    public void revertSourceCodeChanges(@NotNull DBEditableObjectVirtualFile databaseFile, Runnable successCallback) {
        try {
            List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
            for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                sourceCodeFile.revertLocalChanges();
            }
        } finally {
            if (successCallback != null) {
                successCallback.run();
            }
        }

/*
        String objectDescription = databaseFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke("loading the source code", false, databaseFile,
                (action) -> Progress.background(getProject(), "Loading source code for " + objectDescription, false,
                        (progress) -> {
                            try {
                                List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                                for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                                    sourceCodeFile.revertLocalChanges();
                                }
                            } finally {
                                if (successCallback != null) {
                                    successCallback.run();
                                }
                            }
                        }));
*/
    }

    public void saveSourceCodeChanges(@NotNull DBEditableObjectVirtualFile databaseFile, Runnable successCallback) {
        String objectDescription = databaseFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke("saving the source code", false, databaseFile,
                (action) -> Progress.prompt(getProject(), "Saving source code for " + objectDescription, false,
                        (progress) -> {
                            List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                            for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                                if (sourceCodeFile.is(MODIFIED)) {
                                    saveSourceToDatabase(sourceCodeFile, null, successCallback);
                                }
                            }
                        }));
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
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
    public void loadState(@NotNull Element element) {
    }

    /*********************************************
     *            SettingsSavingComponent        *
     *********************************************/
    @Override
    public void save() {
    }
}

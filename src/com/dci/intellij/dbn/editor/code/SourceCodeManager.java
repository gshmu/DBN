package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerAdapter;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SynchronizedTask;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.diff.MergeAction;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.editor.DBLanguageFileEditorListener;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
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

import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.common.thread.TaskInstruction.BACKGROUNDED;
import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;
import static com.dci.intellij.dbn.common.util.CommonUtil.list;
import static com.dci.intellij.dbn.common.util.MessageUtil.options;
import static com.dci.intellij.dbn.common.util.MessageUtil.showErrorDialog;
import static com.dci.intellij.dbn.common.util.MessageUtil.showQuestionDialog;
import static com.dci.intellij.dbn.common.util.MessageUtil.showWarningDialog;
import static com.dci.intellij.dbn.common.util.NamingUtil.unquote;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.LOADING;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.MODIFIED;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.SAVING;
import static com.intellij.openapi.util.text.StringUtil.equalsIgnoreCase;
import static com.intellij.openapi.util.text.StringUtil.indexOfIgnoreCase;
import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;
import static com.intellij.openapi.util.text.StringUtil.replaceIgnoreCase;

@State(
    name = SourceCodeManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class SourceCodeManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, SettingsSavingComponent {
    public static final String COMPONENT_NAME = "DBNavigator.Project.SourceCodeManager";

    private DBLanguageFileEditorListener fileEditorListener;

    public static SourceCodeManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, SourceCodeManager.class);
    }

    private SourceCodeManager(Project project) {
        super(project);
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(OverrideReadonlyFragmentModificationHandler.INSTANCE);
        fileEditorListener = new DBLanguageFileEditorListener();
        EventUtil.subscribe(project, this, DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
        EventUtil.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        EventUtil.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
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
                            MessageCallback.create(0, option ->
                                    reloadAndUpdateEditors(databaseFile, false)));
                } else {
                    reloadAndUpdateEditors(databaseFile, true);
                }

            }
        }
    };

    private EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerAdapter() {
        @Override
        public void editModeChanged(DBContentVirtualFile databaseContentFile) {
            if (databaseContentFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseContentFile;
                if (sourceCodeFile.is(MODIFIED)) {
                    loadSourceCode(sourceCodeFile, true);
                }
            }
        }
    };

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
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
            BackgroundTask.invoke(project,
                    instructions("Reloading object source code", startInBackground ? BACKGROUNDED : null),
                    (task, progress) -> {
                        List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                        for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                            loadSourceCode(sourceCodeFile, true);
                        }
                    });
        }
    }

    public void ensureSourcesLoaded(@NotNull DBSchemaObject schemaObject) {
        DBEditableObjectVirtualFile editableObjectFile = schemaObject.getEditableVirtualFile();
        List<DBSourceCodeVirtualFile> sourceCodeFiles = editableObjectFile.getSourceCodeFiles();
        for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
            if (!sourceCodeFile.isLoaded()) {
                loadSourceFromDatabase(sourceCodeFile, false);
            }
        }
    }

    private void loadSourceFromDatabase(@NotNull DBSourceCodeVirtualFile sourceCodeFile, boolean force) {
        SynchronizedTask.invoke(
                () -> "LOAD_SOURCE:" + sourceCodeFile.getUrl(),
                data -> {
                    boolean initialLoad = !sourceCodeFile.isLoaded();
                    if (sourceCodeFile.isNot(LOADING) && (initialLoad || force)) {
                        sourceCodeFile.set(LOADING, true);
                        EditorUtil.setEditorsReadonly(sourceCodeFile, true);
                        Project project = getProject();
                        DBSchemaObject object = sourceCodeFile.getObject();

                        EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeLoading(sourceCodeFile);
                        try {
                            sourceCodeFile.loadSourceFromDatabase();
                        } catch (SQLException e) {
                            sourceCodeFile.setSourceLoadError(e.getMessage());
                            sourceCodeFile.set(MODIFIED, false);
                            sendErrorNotification("Source Load Error", "Could not load sourcecode for " + object.getQualifiedNameWithType() + " from database. Cause: " + e.getMessage());
                        } finally {
                            sourceCodeFile.set(LOADING, false);
                            EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeLoaded(sourceCodeFile, initialLoad);
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
                Document document = Failsafe.get(DocumentUtil.getDocument(sourceCodeFile));
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
                                BackgroundTask.create(project,
                                        instructions("Loading database source code"),
                                        (data, progress) -> {
                                            if (data == 0) {
                                                try {
                                                    SourceCodeContent sourceCodeContent = loadSourceFromDatabase(object, contentType);
                                                    String databaseContent = sourceCodeContent.getText().toString();
                                                    SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
                                                    diffManager.openCodeMergeDialog(databaseContent, sourceCodeFile, fileEditor, MergeAction.SAVE);
                                                } catch (SQLException e) {
                                                    showErrorDialog(project, "Could not load database sources.", e);
                                                }
                                            } else {
                                                sourceCodeFile.set(SAVING, false);
                                            }
                                        }));

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

    public SourceCodeContent loadSourceFromDatabase(DBSchemaObject object, DBContentType contentType) throws SQLException {
        ProgressMonitor.setTaskDescription("Loading source code of " + object.getQualifiedNameWithType());
        String sourceCode = object.loadCodeFromDatabase(contentType);
        SourceCodeContent sourceCodeContent = new SourceCodeContent(sourceCode);
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
        ddlInterface.computeSourceCodeOffsets(sourceCodeContent, object.getObjectType().getTypeId(), object.getName());
        return sourceCodeContent;
    }

    @Deprecated
    private boolean isValidObjectTypeAndName(String text, DBSchemaObject object, DBContentType contentType) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
        if (ddlInterface.includesTypeAndNameInSourceContent(object.getObjectType().getTypeId())) {
            int typeIndex = indexOfIgnoreCase(text, object.getTypeName(), 0);
            if (typeIndex == -1 || !isEmptyOrSpaces(text.substring(0, typeIndex))) {
                return false;
            }

            int typeEndIndex = typeIndex + object.getTypeName().length();
            if (!Character.isWhitespace(text.charAt(typeEndIndex))) return false;

            if (contentType.getObjectTypeSubname() != null) {
                int subnameIndex = indexOfIgnoreCase(text, contentType.getObjectTypeSubname(), typeEndIndex);
                typeEndIndex = subnameIndex + contentType.getObjectTypeSubname().length();
                if (!Character.isWhitespace(text.charAt(typeEndIndex))) return false;
            }

            QuoteDefinition quotes = DatabaseCompatibilityInterface.getInstance(connectionHandler).getIdentifierQuotes();

            String objectName = object.getName();
            int nameIndex = indexOfIgnoreCase(text, objectName, typeEndIndex);
            if (nameIndex == -1) return false;
            int nameEndIndex = nameIndex + objectName.length();

            char namePreChar = text.charAt(nameIndex - 1);
            char namePostChar = text.charAt(nameEndIndex);
            QuotePair quotePair = null;
            if (quotes.isQuoteBegin(namePreChar)) {
                quotePair = quotes.getQuote(namePreChar);
                if (!quotes.isQuoteEnd(namePreChar, namePostChar)) return false;
                nameIndex = nameIndex -1;
                nameEndIndex = nameEndIndex + 1;
            }

            String typeNameGap = text.substring(typeEndIndex, nameIndex);
            typeNameGap = replaceIgnoreCase(typeNameGap, object.getSchema().getName(), "").replace(".", " ");
            if (quotePair != null) {
                typeNameGap = quotePair.replaceQuotes(typeNameGap, ' ');
            }
            if (!isEmptyOrSpaces(typeNameGap)) return false;
            if (!Character.isWhitespace(text.charAt(nameEndIndex)) && text.charAt(nameEndIndex) != '(') return false;
        }
        return true;
    }

    private boolean isValidObjectTypeAndName(@NotNull DBLanguagePsiFile psiFile, DBSchemaObject object, DBContentType contentType) {
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

    private String text(PsiElement psiElement) {
        return unquote(psiElement.getText());
    }

    public void storeSourceToDatabase(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor, @Nullable Runnable successCallback) {
        Project project = getProject();
        BackgroundTask.invoke(project,
                instructions("Saving sources to database"),
                (task, progress) -> {
                    try {
                        sourceCodeFile.saveSourceToDatabase();
                        EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeSaved(sourceCodeFile, fileEditor);
                    } catch (SQLException e) {
                        showErrorDialog(project, "Could not save changes to database.", e);
                    } finally {
                        sourceCodeFile.set(SAVING, false);
                    }
                    if (successCallback != null) successCallback.run();
                });
    }

    public BasePsiElement getObjectNavigationElement(DBSchemaObject parentObject, DBContentType contentType, DBObjectType objectType, CharSequence objectName) {
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

    public void navigateToObject(DBSchemaObject parentObject, BasePsiElement basePsiElement) {
        DBEditableObjectVirtualFile editableObjectFile = parentObject.getEditableVirtualFile();
        DBLanguagePsiFile psiFile = basePsiElement.getFile();
        VirtualFile elementVirtualFile = psiFile.getVirtualFile();
        if (elementVirtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) elementVirtualFile;
            BasicTextEditor textEditor = EditorUtil.getTextEditor(sourceCodeFile);
            if (textEditor != null) {
                Project project = getProject();
                EditorProviderId editorProviderId = textEditor.getEditorProviderId();
                FileEditor fileEditor = EditorUtil.selectEditor(project, textEditor, editableObjectFile, editorProviderId, NavigationInstruction.OPEN);
                basePsiElement.navigateInEditor(fileEditor, NavigationInstruction.FOCUS_SCROLL);
            }
        }
    }

    @Override
    public boolean canCloseProject(@NotNull Project project) {
        boolean canClose = true;
        if (project == getProject()) {
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
        }
        return canClose;
    }

    public void loadSourceCode(DBSourceCodeVirtualFile sourceCodeFile, boolean force) {
        String objectDescription = sourceCodeFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke(
                instructions("Loading source code for " + objectDescription, BACKGROUNDED),
                "loading the source code",
                sourceCodeFile,
                action -> loadSourceFromDatabase(sourceCodeFile, force));
    }

    public void saveSourceCode(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor, Runnable successCallback) {
        String objectDescription = sourceCodeFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke(
                instructions("Saving source code for " + objectDescription),
                "saving the source code",
                sourceCodeFile,
                action -> saveSourceToDatabase(sourceCodeFile, fileEditor, successCallback));
    }

    public void revertSourceCodeChanges(DBEditableObjectVirtualFile databaseFile, Runnable successCallback) {
        String objectDescription = databaseFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke(
                instructions("Loading source code for " + objectDescription, BACKGROUNDED),
                "loading the source code",
                databaseFile,
                action -> {
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
                });
    }

    public void saveSourceCodeChanges(DBEditableObjectVirtualFile databaseFile, Runnable successCallback) {
        String objectDescription = databaseFile.getObject().getQualifiedNameWithType();
        ConnectionAction.invoke(
                instructions("Saving source code for " + objectDescription),
                "saving the source code",
                databaseFile,
                action -> {
                    List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                    for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                        if (sourceCodeFile.is(MODIFIED)) {
                            saveSourceToDatabase(sourceCodeFile, null, successCallback);
                        }
                    }
                });
    }

    @Override
    public void projectOpened() {
        EventUtil.subscribe(getProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener);
    }

    @Override
    public void projectClosed() {

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

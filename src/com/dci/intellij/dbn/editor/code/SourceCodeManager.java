package com.dci.intellij.dbn.editor.code;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerAdapter;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.ide.IdeMonitor;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SynchronizedTask;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
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
import com.dci.intellij.dbn.editor.code.options.CodeEditorChangesOption;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
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
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.text.DateFormatUtil;

@State(
    name = "DBNavigator.Project.SourceCodeManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class SourceCodeManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, SettingsSavingComponent {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private DBLanguageFileEditorListener fileEditorListener;

    public static SourceCodeManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, SourceCodeManager.class);
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
        public void dataDefinitionChanged(@NotNull final DBSchemaObject schemaObject) {
            final DBEditableObjectVirtualFile databaseFile = schemaObject.getCachedVirtualFile();
            if (databaseFile != null) {
                if (databaseFile.isModified()) {
                    MessageUtil.showQuestionDialog(
                            getProject(), "Unsaved changes",
                            "The " + schemaObject.getQualifiedNameWithType() + " has been updated in database. You have unsaved changes in the object editor.\n" +
                                    "Do you want to discard the changes and reload the updated database version?",
                            new String[]{"Reload", "Keep changes"}, 0,
                            new MessageCallback(0) {
                                @Override
                                protected void execute() {
                                    reloadAndUpdateEditors(databaseFile, false);
                                }
                            });
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
                if (sourceCodeFile.isModified()) {
                    loadSourceCode(sourceCodeFile, true);
                }
            }
        }
    };

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerAdapter() {
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

    private void reloadAndUpdateEditors(final DBEditableObjectVirtualFile databaseFile, boolean startInBackground) {
        Project project = getProject();
        if (databaseFile.isContentLoaded()) {
            new BackgroundTask(project, "Reloading object source code", startInBackground) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                    for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                        loadSourceCode(sourceCodeFile, true);
                    }
                }
            }.start();
        }
    }

    public void ensureSourcesLoaded(@NotNull final DBSchemaObject schemaObject) {
        DBEditableObjectVirtualFile editableObjectFile = schemaObject.getEditableVirtualFile();
        List<DBSourceCodeVirtualFile> sourceCodeFiles = editableObjectFile.getSourceCodeFiles();
        for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
            if (!sourceCodeFile.isLoaded()) {
                loadSourceFromDatabase(sourceCodeFile, false);
            }
        }
    }

    private void loadSourceFromDatabase(@NotNull final DBSourceCodeVirtualFile sourceCodeFile, final boolean force) {
        new SynchronizedTask() {
            @Override
            protected void execute() {
                boolean initialLoad = !sourceCodeFile.isLoaded();
                if (!sourceCodeFile.isLoading() && (initialLoad || force)) {
                    sourceCodeFile.setLoading(true);
                    EditorUtil.setEditorsReadonly(sourceCodeFile, true);
                    Project project = getProject();
                    DBSchemaObject object = sourceCodeFile.getObject();

                    EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeLoading(sourceCodeFile);
                    try {
                        sourceCodeFile.loadSourceFromDatabase();
                    } catch (SQLException e) {
                        sourceCodeFile.setSourceLoadError(e.getMessage());
                        sourceCodeFile.setModified(false);
                        NotificationUtil.sendErrorNotification(project, "Source Load Error", "Could not load sourcecode for " + object.getQualifiedNameWithType() + " from database. Cause: " + e.getMessage());
                    } finally {
                        sourceCodeFile.setLoading(false);
                        EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeLoaded(sourceCodeFile, initialLoad);
                    }
                }
            }

            @Override
            protected String getSyncKey() {
                return "LOAD_SOURCE:" + sourceCodeFile.getUrl();
            }
        }.start();
    }

    private void saveSourceToDatabase(@NotNull final DBSourceCodeVirtualFile sourceCodeFile, @Nullable final SourceCodeEditor fileEditor, @Nullable final Runnable successCallback) {
        final DBSchemaObject object = sourceCodeFile.getObject();
        final DBContentType contentType = sourceCodeFile.getContentType();

        if (!sourceCodeFile.isSaving()) {
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
            if (!debuggerManager.checkForbiddenOperation(sourceCodeFile.getActiveConnection())) {
                return;
            }

            sourceCodeFile.setSaving(true);
            final Project project = getProject();
            try {
                Document document = FailsafeUtil.get(DocumentUtil.getDocument(sourceCodeFile));
                DocumentUtil.saveDocument(document);

                DBLanguagePsiFile psiFile = sourceCodeFile.getPsiFile();
                if (psiFile == null || isValidObjectTypeAndName(psiFile, object, contentType)) {
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
                        BackgroundTask<Integer> openMergeDialogTask = new BackgroundTask<Integer>(project, "Loading database source code", false) {
                            @Override
                            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                                if (getOption() == 0) {
                                    try {
                                        SourceCodeContent sourceCodeContent = loadSourceFromDatabase(object, contentType);
                                        String databaseContent = sourceCodeContent.getText().toString();
                                        SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
                                        diffManager.openCodeMergeDialog(databaseContent, sourceCodeFile, fileEditor, MergeAction.SAVE);
                                    } catch (SQLException e) {
                                        MessageUtil.showErrorDialog(project, "Could not load database sources.", e);
                                    }
                                } else {
                                    sourceCodeFile.setSaving(false);
                                }
                            }

                        };
                        MessageUtil.showWarningDialog(project, "Version conflict", message, new String[]{"Merge Changes", "Cancel"}, 0, openMergeDialogTask);

                    } else {
                        storeSourceToDatabase(sourceCodeFile, fileEditor, successCallback);
                    }

                } else {
                    String message = "You are not allowed to change the name or the type of the object";
                    sourceCodeFile.setSaving(false);
                    MessageUtil.showErrorDialog(project, "Illegal action", message);
                }
            } catch (Exception ex) {
                MessageUtil.showErrorDialog(project, "Could not save changes to database.", ex);
                sourceCodeFile.setSaving(false);
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
            int typeIndex = StringUtil.indexOfIgnoreCase(text, object.getTypeName(), 0);
            if (typeIndex == -1 || !StringUtil.isEmptyOrSpaces(text.substring(0, typeIndex))) {
                return false;
            }

            int typeEndIndex = typeIndex + object.getTypeName().length();
            if (!Character.isWhitespace(text.charAt(typeEndIndex))) return false;

            if (contentType.getObjectTypeSubname() != null) {
                int subnameIndex = StringUtil.indexOfIgnoreCase(text, contentType.getObjectTypeSubname(), typeEndIndex);
                typeEndIndex = subnameIndex + contentType.getObjectTypeSubname().length();
                if (!Character.isWhitespace(text.charAt(typeEndIndex))) return false;
            }

            QuoteDefinition quotes = DatabaseCompatibilityInterface.getInstance(connectionHandler).getIdentifierQuotes();

            String objectName = object.getName();
            int nameIndex = StringUtil.indexOfIgnoreCase(text, objectName, typeEndIndex);
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
            typeNameGap = StringUtil.replaceIgnoreCase(typeNameGap, object.getSchema().getName(), "").replace(".", " ");
            if (quotePair != null) {
                typeNameGap = quotePair.replaceQuotes(typeNameGap, ' ');
            }
            if (!StringUtil.isEmptyOrSpaces(typeNameGap)) return false;
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

            if (psiElement == null || !StringUtil.equalsIgnoreCase(psiElement.getText(), typeName)) {
                return false;
            }

            if (subtypeName != null) {
                psiElement = PsiUtil.getNextLeaf(psiElement);
                if (psiElement == null || !StringUtil.equalsIgnoreCase(psiElement.getText(), subtypeName)) {
                    return false;
                }
            }

            psiElement = PsiUtil.getNextLeaf(psiElement);
            if (psiElement == null) {
                return false;
            }

            if (StringUtil.equalsIgnoreCase(NamingUtil.unquote(psiElement.getText()), schemaName)) {
                psiElement = PsiUtil.getNextLeaf(psiElement) ;
                if (psiElement == null || !psiElement.getText().equals(".")) {
                    return false;
                } else {
                    psiElement = PsiUtil.getNextLeaf(psiElement);
                    if (!StringUtil.equalsIgnoreCase(NamingUtil.unquote(psiElement.getText()), objectName)) {
                        return false;
                    }
                }
            } else {
                if (!StringUtil.equalsIgnoreCase(NamingUtil.unquote(psiElement.getText()), objectName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void storeSourceToDatabase(final DBSourceCodeVirtualFile sourceCodeFile, @Nullable final SourceCodeEditor fileEditor, @Nullable final Runnable successCallback) {
        final DBSchemaObject object = sourceCodeFile.getObject();
        final Project project = getProject();
        new BackgroundTask(project, "Saving sources to database", false) {
            @Override
            protected void execute(@NotNull ProgressIndicator indicator) {
                try {
                    sourceCodeFile.saveSourceToDatabase();
                    EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeSaved(sourceCodeFile, fileEditor);
                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(project, "Could not save changes to database.", e);
                } finally {
                    sourceCodeFile.setSaving(false);
                }
                if (successCallback != null) successCallback.run();

            }
        }.start();
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
        VirtualFile elementVirtualFile = basePsiElement.getFile().getVirtualFile();
        if (elementVirtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) elementVirtualFile;
            BasicTextEditor textEditor = EditorUtil.getTextEditor(sourceCodeFile);
            if (textEditor != null) {
                Project project = getProject();
                EditorProviderId editorProviderId = textEditor.getEditorProviderId();
                FileEditor fileEditor = EditorUtil.selectEditor(project, textEditor, editableObjectFile, editorProviderId, true);
                basePsiElement.navigateInEditor(fileEditor, true);
            }
        }
    }

    @Override
    public boolean canCloseProject(final Project project) {
        return canClose(project, IdeMonitor.getInstance().getProjectCloseCallback(project));
    }

    @Override
    public boolean canExitApplication() {
        return canClose(null, IdeMonitor.getInstance().getAppCloseCallback());
    }

    private boolean canClose(Project project, Runnable successCallback) {
        List<VirtualFile> openFiles = new ArrayList<VirtualFile>();
        if (project == null) {
            ProjectManager projectManager = ProjectManager.getInstance();
            Project[] projects = projectManager.getOpenProjects();
            for (Project openProject : projects) {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(openProject);
                openFiles.addAll(Arrays.asList(fileEditorManager.getOpenFiles()));
            }
        } else {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            openFiles.addAll(Arrays.asList(fileEditorManager.getOpenFiles()));
        }

        boolean canClose = true;
        for (VirtualFile openFile : openFiles) {
            if (openFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) openFile;
                if (databaseFile.isModified()) {
                    canClose = false;
                    if (!databaseFile.isSaving()) {
                        DBSchemaObject object = databaseFile.getObject();
                        Project objectProject = object.getProject();
                        CodeEditorSettings codeEditorSettings = CodeEditorSettings.getInstance(objectProject);
                        CodeEditorConfirmationSettings confirmationSettings = codeEditorSettings.getConfirmationSettings();
                        InteractiveOptionHandler<CodeEditorChangesOption> optionHandler = confirmationSettings.getExitOnChanges();
                        CodeEditorChangesOption option = optionHandler.resolve(object.getQualifiedNameWithType());

                        switch (option) {
                            case SAVE: saveSourceCodeChanges(databaseFile, successCallback); break;
                            case DISCARD: revertSourceCodeChanges(databaseFile, successCallback); break;
                            case SHOW: {
                                List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                                for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                                    if (sourceCodeFile.isModified()) {
                                        SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(objectProject);
                                        diffManager.opedDatabaseDiffWindow(sourceCodeFile);
                                    }
                                }

                            } break;
                            case CANCEL: break;
                        }
                    }
                }
            }
        }
        return canClose;
    }

    public void loadSourceCode(final DBSourceCodeVirtualFile sourceCodeFile, final boolean force) {
        TaskInstructions taskInstructions = new TaskInstructions("Loading source code for " + sourceCodeFile.getObject().getQualifiedNameWithType(), true, false);
        new ConnectionAction("loading the source code", sourceCodeFile, taskInstructions) {
            @Override
            protected void execute() {
                loadSourceFromDatabase(sourceCodeFile, force);
            }
        }.start();
    }

    public void saveSourceCode(final DBSourceCodeVirtualFile sourceCodeFile, @Nullable final SourceCodeEditor fileEditor, final Runnable successCallback) {
        TaskInstructions taskInstructions = new TaskInstructions("Saving source code for " + sourceCodeFile.getObject().getQualifiedNameWithType(), false, false);
        new ConnectionAction("saving the source code", sourceCodeFile, taskInstructions) {
            @Override
            protected void execute() {
                saveSourceToDatabase(sourceCodeFile, fileEditor, successCallback);
            }
        }.start();
    }

    public void revertSourceCodeChanges(final DBEditableObjectVirtualFile databaseFile, final Runnable successCallback) {
        TaskInstructions taskInstructions = new TaskInstructions("Loading source code for " + databaseFile.getObject().getQualifiedNameWithType(), true, false);
        new ConnectionAction("loading the source code", databaseFile, taskInstructions) {
            @Override
            protected void execute() {
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
            }
        }.start();
    }

    public void saveSourceCodeChanges(final DBEditableObjectVirtualFile databaseFile, final Runnable successCallback) {
        TaskInstructions taskInstructions = new TaskInstructions("Saving source code for " + databaseFile.getObject().getQualifiedNameWithType(), false, false);
        new ConnectionAction("saving the source code", databaseFile, taskInstructions) {
            @Override
            protected void execute() {
                List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                    if (sourceCodeFile.isModified()) {
                        saveSourceToDatabase(sourceCodeFile, null, successCallback);
                    }
                }
            }
        }.start();
    }

    @Override
    public void projectOpened() {
        EventUtil.subscribe(getProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener);
    }

    @Override
    public void projectClosed() {

    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.SourceCodeManager";
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

    /*********************************************
     *            SettingsSavingComponent        *
     *********************************************/
    @Override
    public void save() {
    }
}

package com.dci.intellij.dbn.editor.code;

import java.sql.SQLException;
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
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.SynchronizedTask;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.diff.DBSourceFileContent;
import com.dci.intellij.dbn.editor.code.options.CodeEditorChangesOption;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
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
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.ActionButtonPresentation;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffRequestFactory;
import com.intellij.openapi.diff.MergeRequest;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.diff.impl.mergeTool.DiffRequestFactoryImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;

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

    private Runnable closeProjectRunnable = new Runnable() {
        @Override
        public void run() {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    ProjectUtil.closeAndDispose(getProject());
                    WelcomeFrame.showIfNoProjectOpened();
                }
            }.start();

        }
    };


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
                            new SimpleTask() {
                                @Override
                                protected boolean canExecute() {
                                    return getOption() == 0;
                                }

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

    public void opedDatabaseDiffWindow(final DBSourceCodeVirtualFile sourcecodeFile) {
        new ConnectionAction("comparing changes", sourcecodeFile, new TaskInstructions("Loading database source code", false, true)) {
            @Override
            protected void execute() {
                DBSchemaObject object = sourcecodeFile.getObject();
                Project project = getProject();
                try {
                    SourceCodeContent sourceCodeContent = loadSourceFromDatabase(object, sourcecodeFile.getContentType());
                    CharSequence referenceText = sourceCodeContent.getText();
                    if (!isCanceled()) {
                        openDiffWindow(sourcecodeFile, referenceText.toString(), "Database version", "Local version vs. database version");
                    }

                } catch (SQLException e1) {
                    MessageUtil.showErrorDialog(
                            project, "Could not load sourcecode for " +
                                    object.getQualifiedNameWithType() + " from database.", e1);
                }
            }
        }.start();
    }

    public void openDiffWindow(@NotNull final DBSourceCodeVirtualFile sourceCodeFile,  final String referenceText, final String referenceTitle, final String windowTitle) {
        final Project project = sourceCodeFile.getProject();
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                SimpleContent originalContent = new SimpleContent(referenceText, sourceCodeFile.getFileType());
                DBSourceFileContent changedContent = new DBSourceFileContent(project, sourceCodeFile);

                DBSchemaObject object = sourceCodeFile.getObject();
                String title =
                        object.getSchema().getName() + "." +
                                object.getName() + " " +
                                object.getTypeName() + " - " + windowTitle;
                SimpleDiffRequest diffRequest = new SimpleDiffRequest(project, title);
                diffRequest.setContents(originalContent, changedContent);
                diffRequest.setContentTitles(referenceTitle + " ", "Your version ");

                DiffManager.getInstance().getIdeaDiffTool().show(diffRequest);
            }
        }.start();
    }

    public void ensureSourcesLoaded(@NotNull final DBSchemaObject schemaObject) {
        DBEditableObjectVirtualFile virtualFile = schemaObject.getVirtualFile();
        List<DBSourceCodeVirtualFile> sourceCodeFiles = virtualFile.getSourceCodeFiles();
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
                    EditorUtil.setEditorsReadonly(sourceCodeFile, true);
                    sourceCodeFile.setLoading(true);
                    Project project = getProject();
                    DBSchemaObject object = sourceCodeFile.getObject();
                    DBContentType contentType = sourceCodeFile.getContentType();

                    EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeLoading(sourceCodeFile);
                    try {
                        SourceCodeContent sourceCodeContent = loadSourceFromDatabase(object, contentType);
                        sourceCodeFile.applyContent(sourceCodeContent);
                        sourceCodeFile.updateChangeTimestamp();
                    } catch (SQLException e) {
                        sourceCodeFile.setSourceLoadError(e.getMessage());
                        NotificationUtil.sendErrorNotification(project, "Source Load Error", "Could not load sourcecode for " + object.getQualifiedNameWithType() + " from database. Cause: " + e.getMessage());
                    } finally {
                        EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeLoaded(sourceCodeFile, initialLoad);
                        sourceCodeFile.setLoading(false);
                    }
                }
            }

            @Override
            protected String getSyncKey() {
                return sourceCodeFile.getUrl();
            }
        }.start();
    }

    private void saveSourceToDatabase(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable final SourceCodeEditor fileEditor, @Nullable final Runnable successCallback) {
        final DBSchemaObject object = sourceCodeFile.getObject();
        final DBContentType contentType = sourceCodeFile.getContentType();

        if (!sourceCodeFile.isSaving()) {
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
            if (!debuggerManager.checkForbiddenOperation(sourceCodeFile.getActiveConnection())) {
                return;
            }

            sourceCodeFile.setSaving(true);
            Project project = getProject();
            try {
                Document document = FailsafeUtil.get(DocumentUtil.getDocument(sourceCodeFile));
                DocumentUtil.saveDocument(document);

                String content = sourceCodeFile.getContent().toString();
                if (isValidObjectTypeAndName(content, object, contentType)) {
                    ProgressMonitor.setTaskDescription("Checking for third party changes on " + object.getQualifiedNameWithType());
                    boolean isChangedInDatabase = sourceCodeFile.isChangedInDatabase(true);
                    if (isChangedInDatabase) {
                        String message =
                                "The " + object.getQualifiedNameWithType() +
                                        " has been changed by another user. \nYou will be prompted to merge the changes";
                        MessageUtil.showErrorDialog(project, "Version conflict", message);

                        SourceCodeContent sourceCodeContent = loadSourceFromDatabase(object, contentType);
                        String databaseContent = sourceCodeContent.getText().toString();
                        openCodeMergeDialog(databaseContent, sourceCodeFile, fileEditor, true);
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

            char quotes = DatabaseCompatibilityInterface.getInstance(connectionHandler).getIdentifierQuotes();


            String objectName = object.getName();
            int nameIndex = StringUtil.indexOfIgnoreCase(text, objectName, typeEndIndex);
            if (nameIndex == -1) return false;
            int nameEndIndex = nameIndex + objectName.length();

            if (text.charAt(nameIndex -1) == quotes) {
                if (text.charAt(nameEndIndex) != quotes) return false;
                nameIndex = nameIndex -1;
                nameEndIndex = nameEndIndex + 1;
            }

            String typeNameGap = text.substring(typeEndIndex, nameIndex);
            typeNameGap = StringUtil.replaceIgnoreCase(typeNameGap, object.getSchema().getName(), "").replace(".", " ").replace(quotes, ' ');
            if (!StringUtil.isEmptyOrSpaces(typeNameGap)) return false;
            if (!Character.isWhitespace(text.charAt(nameEndIndex)) && text.charAt(nameEndIndex) != '(') return false;
        }
        return true;
    }

    public void openCodeMergeDialog(final String databaseContent, final DBSourceCodeVirtualFile sourceCodeFile, final SourceCodeEditor fileEditor, final boolean isSaveAction) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                DiffRequestFactory diffRequestFactory = new DiffRequestFactoryImpl();
                Project project = sourceCodeFile.getProject();
                if (project != null) {
                    MergeRequest mergeRequest = diffRequestFactory.createMergeRequest(
                            databaseContent,
                            sourceCodeFile.getContent().toString(),
                            sourceCodeFile.getLastSavedContent().toString(),
                            sourceCodeFile,
                            project,
                            ActionButtonPresentation.APPLY,
                            ActionButtonPresentation.CANCEL_WITH_PROMPT);
                    mergeRequest.setVersionTitles(new String[]{"Database version", "Merge result", "Your version"});
                    final DBSchemaObject object = sourceCodeFile.getObject();
                    mergeRequest.setWindowTitle("Version conflict resolution for " + object.getQualifiedNameWithType());

                    DiffManager.getInstance().getDiffTool().show(mergeRequest);

                    int result = mergeRequest.getResult();
                    if (isSaveAction) {
                        if (result == 0) {
                            storeSourceToDatabase(sourceCodeFile, fileEditor, null);
                            //sourceCodeEditor.afterSave();
                        } else if (result == 1) {
                            Editor editor = EditorUtil.getEditor(fileEditor);
                            if (editor != null) {
                                DocumentUtil.setText(editor.getDocument(), sourceCodeFile.getContent());
                                sourceCodeFile.setSaving(false);
                            }
                        }
                    }
                }
            }
        }.start();
    }


    private void storeSourceToDatabase(final DBSourceCodeVirtualFile sourceCodeFile, @Nullable final SourceCodeEditor fileEditor, @Nullable final Runnable successCallback) {
        final DBSchemaObject object = sourceCodeFile.getObject();
        final Project project = getProject();
        new BackgroundTask(project, "Saving " + object.getQualifiedNameWithType() + " to database", false) {
            @Override
            protected void execute(@NotNull ProgressIndicator indicator) {
                try {
                    sourceCodeFile.saveSourceToDatabase();
                    sourceCodeFile.updateChangeTimestamp();
                    EventUtil.notify(project, SourceCodeManagerListener.TOPIC).sourceCodeSaved(sourceCodeFile, fileEditor);

                    object.reload();
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
        DBEditableObjectVirtualFile databaseFile = parentObject.getVirtualFile();
        DBContentVirtualFile contentFile = databaseFile.getContentFile(contentType);
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
        DBEditableObjectVirtualFile databaseFile = parentObject.getVirtualFile();
        VirtualFile virtualFile = basePsiElement.getFile().getVirtualFile();
        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            BasicTextEditor textEditor = EditorUtil.getTextEditor(sourceCodeFile);
            if (textEditor != null) {
                Project project = getProject();
                EditorProviderId editorProviderId = textEditor.getEditorProviderId();
                FileEditor fileEditor = EditorUtil.selectEditor(project, textEditor, databaseFile, editorProviderId, true);
                basePsiElement.navigateInEditor(fileEditor, true);
            }
        }
    }

    @Override
    public boolean canCloseProject(final Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();

        CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(getProject()).getConfirmationSettings();
        InteractiveOptionHandler<CodeEditorChangesOption> optionHandler = confirmationSettings.getExitOnChanges();

        boolean canClose = true;
        for (VirtualFile openFile : openFiles) {
            if (openFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) openFile;
                if (databaseFile.isModified()) {
                    canClose = false;
                    if (!databaseFile.isSaving()) {
                        DBSchemaObject object = databaseFile.getObject();
                        CodeEditorChangesOption option = optionHandler.resolve(object.getQualifiedNameWithType());

                        switch (option) {
                            case SAVE: saveSourceCodeChanges(databaseFile, closeProjectRunnable); break;
                            case DISCARD: revertSourceCodeChanges(databaseFile, closeProjectRunnable); break;
                            case SHOW: {
                                List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                                for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                                    if (sourceCodeFile.isModified()) {
                                        opedDatabaseDiffWindow(sourceCodeFile);
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
                List<DBSourceCodeVirtualFile> sourceCodeFiles = databaseFile.getSourceCodeFiles();
                for (DBSourceCodeVirtualFile sourceCodeFile : sourceCodeFiles) {
                    loadSourceFromDatabase(sourceCodeFile, true);
                }

                if (successCallback != null) {
                    successCallback.run();
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
        System.out.printf("");
    }
}

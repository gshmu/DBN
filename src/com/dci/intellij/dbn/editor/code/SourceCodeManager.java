package com.dci.intellij.dbn.editor.code;

import java.sql.SQLException;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.diff.DBSourceFileContent;
import com.dci.intellij.dbn.editor.code.options.CodeEditorChangesOption;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.editor.DBLanguageFileEditorListener;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diff.ActionButtonPresentation;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffRequestFactory;
import com.intellij.openapi.diff.MergeRequest;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.diff.impl.mergeTool.DiffRequestFactoryImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

@State(
    name = "DBNavigator.Project.SourceCodeManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class SourceCodeManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, SettingsSavingComponent {

    private DBLanguageFileEditorListener fileEditorListener;

    public static SourceCodeManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, SourceCodeManager.class);
    }

    private SourceCodeManager(Project project) {
        super(project);
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(OverrideReadonlyFragmentModificationHandler.INSTANCE);
        fileEditorListener = new DBLanguageFileEditorListener();
    }

    public void showChangesAgainstDatabase(final DBSourceCodeVirtualFile sourcecodeFile) {
        new ConnectionAction("comparing changes", sourcecodeFile, new TaskInstructions("Loading database source code", false, true)) {
            @Override
            protected void execute() {
                DBSchemaObject object = sourcecodeFile.getObject();
                Project project = getProject();
                try {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    CharSequence referenceText = sourceCodeManager.loadSourceCodeFromDatabase(object, sourcecodeFile.getContentType());
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

    public void openDiffWindow(@NotNull final DBSourceCodeVirtualFile virtualFile,  final String referenceText, final String referenceTitle, final String windowTitle) {
        final Project project = virtualFile.getProject();
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                SimpleContent originalContent = new SimpleContent(referenceText, virtualFile.getFileType());
                DBSourceFileContent changedContent = new DBSourceFileContent(project, virtualFile);

                DBSchemaObject object = virtualFile.getObject();
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

    public void loadSourceFromDatabase(@NotNull final SourceCodeEditor fileEditor) {
        final DBSourceCodeVirtualFile virtualFile = fileEditor.getVirtualFile();
        TaskInstructions taskInstructions = new TaskInstructions("Reverting local changes", false, false);
        new ConnectionAction("reverting the changes", virtualFile, taskInstructions) {
            @Override
            protected void execute() {
                boolean reloaded = virtualFile.reloadFromDatabase();

                if (reloaded) {
                    new WriteActionRunner() {
                        public void run() {
                            Editor editor = EditorUtil.getEditor(fileEditor);
                            if (editor != null) {
                                editor.getDocument().setText(virtualFile.getContent());
                                virtualFile.setModified(false);
                            }
                        }
                    }.start();
                }
            }
        }.start();
    }

    public void updateSourceToDatabase(@NotNull final SourceCodeEditor fileEditor, @Nullable final Runnable successCallback) {
        final DBSourceCodeVirtualFile virtualFile = fileEditor.getVirtualFile();
        final DBSchemaObject object = virtualFile.getObject();
        final DBObjectStatusHolder objectStatus = object.getStatus();

        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
        if (!debuggerManager.checkForbiddenOperation(virtualFile.getActiveConnection())) {
            objectStatus.set(DBObjectStatus.SAVING, false);
            return;
        }
        objectStatus.set(DBObjectStatus.SAVING, true);
        TaskInstructions taskInstructions = new TaskInstructions("Checking for third party changes on " + object.getQualifiedNameWithType(), false, false);
        new ConnectionAction("saving the " + object.getQualifiedNameWithType(), object, taskInstructions) {
            @Override
            protected void execute() {
                Project project = getProject();
                try {
                    final DBContentType contentType = virtualFile.getContentType();
                    Editor editor = EditorUtil.getEditor(fileEditor);
                    if (editor != null) {
                        String content = editor.getDocument().getText();
                        if (isValidObjectTypeAndName(content, object, contentType)) {
                            boolean isChangedInDatabase = virtualFile.isChangedInDatabase(true);
                            if (isChangedInDatabase) {
                                String message =
                                        "The " + object.getQualifiedNameWithType() +
                                                " has been changed by another user. \nYou will be prompted to merge the changes";
                                MessageUtil.showErrorDialog(project, "Version conflict", message);

                                CharSequence databaseContent = loadSourceCodeFromDatabase(object, contentType);
                                showSourceMergeDialog(databaseContent.toString(), virtualFile, fileEditor);
                            } else {
                                doUpdateSourceToDatabase(object, virtualFile, fileEditor, successCallback);
                                //sourceCodeEditor.afterSave();
                            }

                        } else {
                            String message = "You are not allowed to change the name or the type of the object";
                            objectStatus.set(DBObjectStatus.SAVING, false);
                            MessageUtil.showErrorDialog(project, "Illegal action", message);
                        }
                    }
                } catch (SQLException ex) {
                    if (DatabaseFeature.OBJECT_REPLACING.isSupported(object)) {
                        virtualFile.updateChangeTimestamp();
                    }
                    MessageUtil.showErrorDialog(project, "Could not save changes to database.", ex);
                    objectStatus.set(DBObjectStatus.SAVING, false);
                }
            }

            @Override
            protected void cancel() {
                objectStatus.set(DBObjectStatus.SAVING, false);
            }

        }.start();
    }

    public CharSequence loadSourceCodeFromDatabase(DBSchemaObject object, DBContentType contentType) throws SQLException {
        return loadSourceFromDatabase(object, contentType).getText();
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

    public void showSourceMergeDialog(final String databaseContent, final DBSourceCodeVirtualFile virtualFile, final FileEditor fileEditor) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                DiffRequestFactory diffRequestFactory = new DiffRequestFactoryImpl();
                Project project = virtualFile.getProject();
                if (project != null) {
                    MergeRequest mergeRequest = diffRequestFactory.createMergeRequest(
                            databaseContent,
                            virtualFile.getContent().toString(),
                            virtualFile.getLastSavedContent().toString(),
                            virtualFile,
                            project,
                            ActionButtonPresentation.APPLY,
                            ActionButtonPresentation.CANCEL_WITH_PROMPT);
                    mergeRequest.setVersionTitles(new String[]{"Database version", "Merge result", "Your version"});
                    final DBSchemaObject object = virtualFile.getObject();
                    mergeRequest.setWindowTitle("Version conflict resolution for " + object.getQualifiedNameWithType());

                    DiffManager.getInstance().getDiffTool().show(mergeRequest);

                    int result = mergeRequest.getResult();
                    if (result == 0) {

                    } else if (result == 1) {
                    }
                }
            }
        }.start();
    }


    private void doUpdateSourceToDatabase(final DBSchemaObject object, final DBSourceCodeVirtualFile virtualFile, final FileEditor fileEditor, @Nullable final Runnable successCallback) {
        new BackgroundTask(object.getProject(), "Saving " + object.getQualifiedNameWithType() + " to database", false) {
            @Override
            protected void execute(@NotNull ProgressIndicator indicator) {
                Project project = getProject();
                try {
                    DBContentType contentType = virtualFile.getContentType();
                    virtualFile.updateToDatabase();

                    ConnectionHandler connectionHandler = object.getConnectionHandler();
                    if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(object)) {
                        boolean isCompilable = object.getProperties().is(DBObjectProperty.COMPILABLE);

                        if (isCompilable) {
                            DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
                            CompileType compileType = compilerManager.getCompileType(object, contentType);

                            CompilerAction compilerAction = new CompilerAction(CompilerActionSource.SAVE, contentType, virtualFile, fileEditor);
                            if (compileType == CompileType.DEBUG) {
                                compilerManager.compileObject(object, compileType, compilerAction);
                            } else {
                                connectionHandler.getObjectBundle().refreshObjectsStatus(object);
                            }

                            compilerManager.createCompilerResult(object, compilerAction);
                        }
                    }

                    object.reload();
                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(project, "Could not save changes to database.", e);
                } finally {
                     object.getStatus().set(DBObjectStatus.SAVING, false);
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
            BasicTextEditor textEditor = EditorUtil.getTextEditor(databaseFile, (DBSourceCodeVirtualFile) virtualFile);
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
        InteractiveOptionHandler<CodeEditorChangesOption> optionHandler = confirmationSettings.getExitOnChangesOptionHandler();

        for (VirtualFile openFile : openFiles) {
            if (openFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) openFile;
                if (databaseFile.isModified()) {
                    DBSchemaObject object = databaseFile.getObject();
                    List<DBContentVirtualFile> contentFiles = databaseFile.getContentFiles();
                    for (DBContentVirtualFile contentFile : contentFiles) {
                        if (contentFile instanceof DBSourceCodeVirtualFile && contentFile.isModified()) {
                            if (object.getStatus().is(DBObjectStatus.SAVING)) {
                                return false;
                            }

                            DBSourceCodeVirtualFile sourcecodeFile = (DBSourceCodeVirtualFile) contentFile;
                            CodeEditorChangesOption option = optionHandler.resolve(object.getQualifiedNameWithType());
                            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                            Runnable closeProjectRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    new SimpleLaterInvocator() {
                                        @Override
                                        protected void execute() {
                                            ProjectManager.getInstance().closeProject(project);
                                        }
                                    }.start();

                                }
                            };

                            switch (option) {
                                case SAVE: databaseFile.saveChanges(closeProjectRunnable); break;
                                case DISCARD: databaseFile.revertChanges(closeProjectRunnable); break;
                                case SHOW: sourceCodeManager.showChangesAgainstDatabase(sourcecodeFile); return false;
                                case CANCEL: return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
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

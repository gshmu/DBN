package com.dci.intellij.dbn.editor.code.diff;

import java.sql.SQLException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
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
import com.intellij.openapi.project.Project;

@State(
        name = "DBNavigator.Project.SourceCodeDiffManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class SourceCodeDiffManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    protected SourceCodeDiffManager(Project project) {
        super(project);
    }
    public static SourceCodeDiffManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, SourceCodeDiffManager.class);
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
                            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                            sourceCodeManager.storeSourceToDatabase(sourceCodeFile, fileEditor, null);
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


    public void openDiffWindow(@NotNull final DBSourceCodeVirtualFile sourceCodeFile,  final String referenceText, final String referenceTitle, final String windowTitle) {
        final Project project = sourceCodeFile.getProject();
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                SimpleContent originalContent = new SimpleContent(referenceText, sourceCodeFile.getFileType());
                SourceCodeFileContent changedContent = new SourceCodeFileContent(project, sourceCodeFile);

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


    public void opedDatabaseDiffWindow(final DBSourceCodeVirtualFile sourcecodeFile) {
        new ConnectionAction("comparing changes", sourcecodeFile, new TaskInstructions("Loading database source code", false, true)) {
            @Override
            protected void execute() {
                DBSchemaObject object = sourcecodeFile.getObject();
                Project project = getProject();
                try {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, sourcecodeFile.getContentType());
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


    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Project.SourceCodeDiffManager";
    }

    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element state) {

    }
}

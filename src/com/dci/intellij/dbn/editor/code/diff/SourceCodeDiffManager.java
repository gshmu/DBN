package com.dci.intellij.dbn.editor.code.diff;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diff.ActionButtonPresentation;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.InvalidDiffRequestException;
import com.intellij.diff.merge.MergeRequest;
import com.intellij.diff.merge.MergeResult;
*/

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


    @Deprecated
    public void openCodeMergeDialog(final String databaseContent, final DBSourceCodeVirtualFile sourceCodeFile, final SourceCodeEditor fileEditor, final MergeAction action) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                com.intellij.openapi.diff.DiffRequestFactory diffRequestFactory = new com.intellij.openapi.diff.impl.mergeTool.DiffRequestFactoryImpl();
                Project project = sourceCodeFile.getProject();
                if (project != null) {
                    com.intellij.openapi.diff.MergeRequest mergeRequest = diffRequestFactory.createMergeRequest(
                            databaseContent,
                            sourceCodeFile.getContent().toString(),
                            sourceCodeFile.getOriginalContent().toString(),
                            sourceCodeFile,
                            project,
                            ActionButtonPresentation.APPLY,
                            ActionButtonPresentation.CANCEL_WITH_PROMPT);
                    mergeRequest.setVersionTitles(new String[]{"Database version", "Merge result", "Your version"});
                    final DBSchemaObject object = sourceCodeFile.getObject();
                    mergeRequest.setWindowTitle("Version conflict resolution for " + object.getQualifiedNameWithType());

                    com.intellij.openapi.diff.DiffManager.getInstance().getDiffTool().show(mergeRequest);

                    int result = mergeRequest.getResult();
                    if (action == MergeAction.SAVE) {
                        switch (result) {
                            case 0:
                                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                                sourceCodeManager.storeSourceToDatabase(sourceCodeFile, fileEditor, null);
                                EventUtil.notify(project, SourceCodeDifManagerListener.TOPIC).contentMerged(sourceCodeFile, action);
                                break;
                            case 1:
                                sourceCodeFile.setSaving(false);
                                break;

                        }
                    } else if (action == MergeAction.MERGE) {
                        switch (result) {
                            case 0:
                                sourceCodeFile.markAsMerged();
                                EventUtil.notify(project, SourceCodeDifManagerListener.TOPIC).contentMerged(sourceCodeFile, action);
                                break;
                            case 1:
                                break;

                        }
                    }
                }
            }
        }.start();
    }

/*
    public void openCodeMergeDialog(final String databaseContent, final DBSourceCodeVirtualFile sourceCodeFile, final SourceCodeEditor fileEditor, final MergeAction action) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                final Project project = getProject();
                SourceCodeDiffContent leftContent = new SourceCodeDiffContent("Database version", databaseContent);
                SourceCodeDiffContent targetContent = new SourceCodeDiffContent("Merge result", sourceCodeFile.getOriginalContent());
                SourceCodeDiffContent rightContent = new SourceCodeDiffContent("Your version", sourceCodeFile.getContent());
                final MergeContent mergeContent = new MergeContent(leftContent, targetContent, rightContent );
                try {
                    DiffRequestFactory diffRequestFactory = DiffRequestFactory.getInstance();
                    MergeRequest mergeRequest = diffRequestFactory.createMergeRequest(
                            project,
                            sourceCodeFile,
                            mergeContent.getByteContents(),
                            "Version conflict resolution for " + sourceCodeFile.getObject().getQualifiedNameWithType(),
                            mergeContent.getTitles(),
                            new Consumer<MergeResult>() {
                                @Override
                                public void consume(MergeResult mergeResult) {
                                    if (action == MergeAction.SAVE) {
                                        switch (mergeResult) {
                                            case LEFT:
                                            case RIGHT:
                                            case RESOLVED:
                                                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                                                sourceCodeManager.storeSourceToDatabase(sourceCodeFile, fileEditor, null);
                                                EventUtil.notify(project, SourceCodeDifManagerListener.TOPIC).contentMerged(sourceCodeFile, action);
                                                break;
                                            case CANCEL:
                                                sourceCodeFile.setSaving(false);
                                                break;
                                        }
                                    } else if (action == MergeAction.MERGE) {
                                        switch (mergeResult) {
                                            case LEFT:
                                            case RIGHT:
                                            case RESOLVED:
                                                sourceCodeFile.markAsMerged();
                                                EventUtil.notify(project, SourceCodeDifManagerListener.TOPIC).contentMerged(sourceCodeFile, action);
                                                break;
                                            case CANCEL:
                                                break;
                                        }
                                    }
                                }
                            });

                    DiffManager diffManager = DiffManager.getInstance();
                    diffManager.showMerge(project, mergeRequest);
                } catch (InvalidDiffRequestException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
*/


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

                com.intellij.openapi.diff.DiffManager.getInstance().getIdeaDiffTool().show(diffRequest);
            }
        }.start();
    }


    public void opedDatabaseDiffWindow(final DBSourceCodeVirtualFile sourceCodeFile) {
        new ConnectionAction("comparing changes", sourceCodeFile, new TaskInstructions("Loading database source code", false, true)) {
            @Override
            protected void execute() {
                DBSchemaObject object = sourceCodeFile.getObject();
                Project project = getProject();
                try {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, sourceCodeFile.getContentType());
                    CharSequence referenceText = sourceCodeContent.getText();

                    if (!isCancelled()) {
                        openDiffWindow(sourceCodeFile, referenceText.toString(), "Database version", "Local version vs. database version");
                    }

                } catch (Exception e1) {
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

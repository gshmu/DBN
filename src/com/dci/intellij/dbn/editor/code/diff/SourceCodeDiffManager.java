package com.dci.intellij.dbn.editor.code.diff;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBLooseContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.InvalidDiffRequestException;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.merge.MergeRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.vfs.VirtualFileStatus.SAVING;

@State(
    name = SourceCodeDiffManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class SourceCodeDiffManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    public static final String COMPONENT_NAME = "DBNavigator.Project.SourceCodeDiffManager";

    protected SourceCodeDiffManager(Project project) {
        super(project);
    }
    public static SourceCodeDiffManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, SourceCodeDiffManager.class);
    }

    public void openCodeMergeDialog(String databaseContent, DBSourceCodeVirtualFile sourceCodeFile, SourceCodeEditor fileEditor, MergeAction action) {
        Dispatch.run(() -> {
            Project project = getProject();
            SourceCodeDiffContent leftContent = new SourceCodeDiffContent("Database version", databaseContent);
            SourceCodeDiffContent targetContent = new SourceCodeDiffContent("Merge result", sourceCodeFile.getOriginalContent());
            SourceCodeDiffContent rightContent = new SourceCodeDiffContent("Your version", sourceCodeFile.getContent());
            MergeContent mergeContent = new MergeContent(leftContent, targetContent, rightContent );
            try {
                DiffRequestFactory diffRequestFactory = DiffRequestFactory.getInstance();
                MergeRequest mergeRequest = diffRequestFactory.createMergeRequest(
                        project,
                        sourceCodeFile,
                        mergeContent.getByteContents(),
                        "Version conflict resolution for " + sourceCodeFile.getObject().getQualifiedNameWithType(),
                        mergeContent.getTitles(),
                        mergeResult -> {
                            if (action == MergeAction.SAVE) {
                                switch (mergeResult) {
                                    case LEFT:
                                    case RIGHT:
                                    case RESOLVED:
                                        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                                        sourceCodeManager.storeSourceToDatabase(sourceCodeFile, fileEditor, null);
                                        ProjectEvents.notify(project,
                                                SourceCodeDifManagerListener.TOPIC,
                                                (listener) -> listener.contentMerged(sourceCodeFile, action));
                                        break;
                                    case CANCEL:
                                        sourceCodeFile.set(SAVING, false);
                                        break;
                                }
                            } else if (action == MergeAction.MERGE) {
                                switch (mergeResult) {
                                    case LEFT:
                                    case RIGHT:
                                    case RESOLVED:
                                        sourceCodeFile.markAsMerged();
                                        ProjectEvents.notify(project,
                                                SourceCodeDifManagerListener.TOPIC,
                                                (listener) -> listener.contentMerged(sourceCodeFile, action));
                                        break;
                                    case CANCEL:
                                        break;
                                }
                            }
                        });

                DiffManager diffManager = DiffManager.getInstance();
                diffManager.showMerge(project, mergeRequest);
            } catch (InvalidDiffRequestException e) {
                e.printStackTrace();
            }
        });
    }


    public void openDiffWindow(@NotNull DBSourceCodeVirtualFile sourceCodeFile, String referenceText, String referenceTitle, String windowTitle) {
        DBSchemaObject object = sourceCodeFile.getObject();
        FileType fileType = sourceCodeFile.getFileType();
        DBLooseContentVirtualFile counterContent = new DBLooseContentVirtualFile(object, referenceText, fileType);
        Project project = getProject();
        DiffContent originalContent = new SourceCodeFileContent(project, sourceCodeFile);
        DiffContent changedContent = new SourceCodeFileContent(project, counterContent);

        String title =
                object.getSchema().getName() + "." +
                        object.getName() + " " +
                        object.getTypeName() + " - " + windowTitle;
        SimpleDiffRequest diffRequest = new SimpleDiffRequest(
                title,
                changedContent,
                originalContent,
                referenceTitle,
                "Your version");

        Dispatch.run(() -> DiffManager.getInstance().showDiff(project, diffRequest));
    }


    public void opedDatabaseDiffWindow(DBSourceCodeVirtualFile sourceCodeFile) {
        ConnectionAction.invoke("comparing changes", false, sourceCodeFile,
                action -> Progress.prompt(getProject(), "Loading database source code", true,
                        progress -> {
                            DBSchemaObject object = sourceCodeFile.getObject();
                            Project project = getProject();
                            try {
                                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                                SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, sourceCodeFile.getContentType());
                                CharSequence referenceText = sourceCodeContent.getText();

                                if (!action.isCancelled()) {
                                    openDiffWindow(sourceCodeFile, referenceText.toString(), "Database version", "Local version vs. database version");
                                }

                            } catch (Exception e1) {
                                Messages.showErrorDialog(
                                        project, "Could not load sourcecode for " +
                                                object.getQualifiedNameWithType() + " from database.", e1);
                            }
                        }));
    }


    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element state) {

    }
}

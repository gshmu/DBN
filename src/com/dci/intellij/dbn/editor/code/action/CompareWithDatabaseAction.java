package com.dci.intellij.dbn.editor.code.action;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.SourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class CompareWithDatabaseAction extends AbstractDiffAction {
    public CompareWithDatabaseAction() {
        super("Compare with database", null, Icons.CODE_EDITOR_DIFF_DB);
    }

    public void actionPerformed(final AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        if (project != null) {
            new BackgroundTask(project, "Loading database source code", false, true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    SourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
                    Editor editor = getEditor(e);
                    if (virtualFile != null && editor != null) {
                        String content = editor.getDocument().getText();
                        virtualFile.setContent(content);
                        DBSchemaObject object = virtualFile.getObject();
                        if (object != null) {
                            try {
                                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                                String referenceText = sourceCodeManager.loadSourceCodeFromDatabase(object, virtualFile.getContentType());
                                if (!progressIndicator.isCanceled()) {
                                    openDiffWindow(e, referenceText, "Database version", "Local version vs. database version");
                                }

                            } catch (SQLException e1) {
                                MessageUtil.showErrorDialog(
                                        "Could not load sourcecode for " +
                                                object.getQualifiedNameWithType() + " from database.", e1);
                            }
                        }
                    }
                }
            }.start();
        }

    }

    public void update(AnActionEvent e) {
        Editor editor = getEditor(e);
        e.getPresentation().setText("Compare with database");
        e.getPresentation().setEnabled(editor != null);
    }
}

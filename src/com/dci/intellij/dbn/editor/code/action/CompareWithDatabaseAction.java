package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class CompareWithDatabaseAction extends AbstractDiffAction {
    public CompareWithDatabaseAction() {
        super("Compare with database", null, Icons.CODE_EDITOR_DIFF_DB);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);

        if (project != null && sourcecodeFile != null) {
            SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
            diffManager.opedDatabaseDiffWindow(sourcecodeFile);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Editor editor = getEditor(e);
        e.getPresentation().setText("Compare with Database");
        e.getPresentation().setEnabled(editor != null);
    }
}

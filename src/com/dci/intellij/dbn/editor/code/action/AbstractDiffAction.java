package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public abstract class AbstractDiffAction extends AbstractSourceCodeEditorAction {
    public AbstractDiffAction(String text, String description, javax.swing.Icon icon) {
        super(text, description, icon);
    }

    protected void openDiffWindow(AnActionEvent e, final String referenceText, final String referenceTitle, final String windowTitle) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Project project = ActionUtil.getProject(e);
        if (project!= null && sourceCodeFile != null) {
            SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
            diffManager.openDiffWindow(sourceCodeFile, referenceText, referenceTitle, windowTitle);
        }
    }
}


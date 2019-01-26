package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

import static com.dci.intellij.dbn.vfs.VirtualFileStatus.MODIFIED;

public class CompareWithOriginalAction extends AbstractDiffAction {
    public CompareWithOriginalAction() {
        super("Compare with original", null, Icons.CODE_EDITOR_DIFF);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        if (sourceCodeFile != null) {
            CharSequence referenceText = sourceCodeFile.getOriginalContent();
            openDiffWindow(e, referenceText.toString(), "Original version", "Local version");
        }
    }

    @Override
    public void update(AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        e.getPresentation().setText("Compare with Original");
        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        if (project == null || sourceCodeFile == null) {
            presentation.setEnabled(false);
        } else {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean readonly = environmentManager.isReadonly(sourceCodeFile);
            presentation.setVisible(!readonly);
            presentation.setEnabled(sourceCodeFile.is(MODIFIED));
        }
    }
}

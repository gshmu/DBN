package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class CompareWithOriginalAction extends AbstractDiffAction {
    public CompareWithOriginalAction() {
        super("Compare with original", null, Icons.CODE_EDITOR_DIFF);
    }

    public void actionPerformed(AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        if (sourceCodeFile != null) {
            CharSequence referenceText = sourceCodeFile.getOriginalContent();
            openDiffWindow(e, referenceText.toString(), "Original version", "Local version");
        }
    }

    public void update(AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        e.getPresentation().setText("Compare with Original");
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(sourceCodeFile != null && sourceCodeFile.isModified());
        presentation.setVisible(sourceCodeFile != null && sourceCodeFile.getEnvironmentType().isCodeEditable());
    }
}

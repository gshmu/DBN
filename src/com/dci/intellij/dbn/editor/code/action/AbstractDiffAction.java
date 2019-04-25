package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

abstract class AbstractDiffAction extends AbstractSourceCodeEditorAction {
    AbstractDiffAction(String text, String description, javax.swing.Icon icon) {
        super(text, description, icon);
    }

    void openDiffWindow(
            @NotNull Project project,
            @NotNull DBSourceCodeVirtualFile sourceCodeFile,
            String referenceText,
            String referenceTitle,
            String windowTitle) {
        SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
        diffManager.openDiffWindow(sourceCodeFile, referenceText, referenceTitle, windowTitle);
    }
}


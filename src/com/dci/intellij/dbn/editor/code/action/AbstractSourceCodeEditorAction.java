package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.vfs.SourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class AbstractSourceCodeEditorAction extends DumbAwareAction {
    public AbstractSourceCodeEditorAction(String text, String description, javax.swing.Icon icon) {
        super(text, description, icon);
    }

    @Nullable
    protected Editor getEditor(AnActionEvent e) {
        return e.getData(PlatformDataKeys.EDITOR);
    }

    @Nullable
    protected SourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        return virtualFile instanceof SourceCodeVirtualFile ? (SourceCodeVirtualFile) virtualFile : null;
    }
}

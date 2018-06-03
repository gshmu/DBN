package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public abstract class AbstractSourceCodeEditorAction extends DumbAwareAction {
    public AbstractSourceCodeEditorAction(String text, String description, javax.swing.Icon icon) {
        super(text, description, icon);
    }

    @Nullable
    protected Editor getEditor(AnActionEvent e) {
        return ActionUtil.getEditor(e);
    }

    @Nullable
    protected SourceCodeEditor getFileEditor(AnActionEvent e) {
        Editor editor = getEditor(e);
        FileEditor fileEditor = EditorUtil.getFileEditor(editor);
        return fileEditor instanceof SourceCodeEditor ? (SourceCodeEditor) fileEditor : null;
    }

    @Nullable
    protected DBSourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = getVirtualFile(e);
        return virtualFile instanceof DBSourceCodeVirtualFile ? (DBSourceCodeVirtualFile) virtualFile : null;
    }
}

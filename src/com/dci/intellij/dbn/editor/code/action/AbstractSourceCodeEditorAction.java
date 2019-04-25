package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSourceCodeEditorAction extends DumbAwareProjectAction {
    AbstractSourceCodeEditorAction(String text, String description, javax.swing.Icon icon) {
        super(text, description, icon);
    }

    @Nullable
    protected Editor getEditor(AnActionEvent e) {
        return Lookup.getEditor(e);
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        SourceCodeEditor fileEditor = getFileEditor(e);
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        if (fileEditor != null && sourcecodeFile != null) {
            actionPerformed(e, project, fileEditor, sourcecodeFile);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        update(e, project, getFileEditor(e), getSourcecodeFile(e));
    }

    @Nullable
    private SourceCodeEditor getFileEditor(AnActionEvent e) {
        Editor editor = getEditor(e);
        FileEditor fileEditor = EditorUtil.getFileEditor(editor);
        return fileEditor instanceof SourceCodeEditor ? (SourceCodeEditor) fileEditor : null;
    }

    @Nullable
    private DBSourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        return virtualFile instanceof DBSourceCodeVirtualFile ? (DBSourceCodeVirtualFile) virtualFile : null;
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull SourceCodeEditor fileEditor,
            @NotNull DBSourceCodeVirtualFile sourceCodeFile);

    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable SourceCodeEditor fileEditor,
            @Nullable DBSourceCodeVirtualFile sourceCodeFile);
}

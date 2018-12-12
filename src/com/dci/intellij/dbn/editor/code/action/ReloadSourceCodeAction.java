package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.vfs.VirtualFileStatus.LOADING;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.MODIFIED;

public class ReloadSourceCodeAction extends AbstractSourceCodeEditorAction {
    public ReloadSourceCodeAction() {
        super("", null, Icons.CODE_EDITOR_RELOAD);
    }

    public void actionPerformed(@NotNull final AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        SourceCodeEditor fileEditor = getFileEditor(e);
        if (fileEditor != null) {
            DBSourceCodeVirtualFile sourceCodeFile = fileEditor.getVirtualFile();
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            sourceCodeManager.loadSourceCode(sourceCodeFile, true);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        if (sourceCodeFile == null) {
            presentation.setEnabled(false);
        } else {
            DBContentType contentType = sourceCodeFile.getContentType();
            String text =
                contentType == DBContentType.CODE_SPEC ? "Reload spec" :
                contentType == DBContentType.CODE_BODY ? "Reload body" : "Reload";

            presentation.setText(text);
            presentation.setEnabled(sourceCodeFile.isNot(LOADING) && sourceCodeFile.isNot(MODIFIED));
        }
    }
}
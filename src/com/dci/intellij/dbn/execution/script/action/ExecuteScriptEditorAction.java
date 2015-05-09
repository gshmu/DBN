package com.dci.intellij.dbn.execution.script.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class ExecuteScriptEditorAction extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            FileDocumentManager.getInstance().saveDocument(editor.getDocument());
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            VirtualFile virtualFile = DocumentUtil.getVirtualFile(editor);
            scriptExecutionManager.executeScript(virtualFile);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(isEnabled(e));
        presentation.setIcon(Icons.EXECUTE_SQL_SCRIPT);
        presentation.setText("Execute SQL Script");
    }

    private static boolean isEnabled(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project == null || editor == null) {
            return false;
        } else {
            PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
            return psiFile instanceof DBLanguagePsiFile;
        }
    }
}

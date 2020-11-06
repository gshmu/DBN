package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ExecuteScriptIntentionAction extends GenericIntentionAction implements HighPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Execute SQL script";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.EXECUTE_SQL_SCRIPT;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile != null && psiFile.getLanguage() instanceof DBLanguage) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile instanceof VirtualFileWindow || virtualFile instanceof DBSourceCodeVirtualFile) {
                return false;
            }

            return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile != null && editor != null && psiFile.getLanguage() instanceof DBLanguage) {
            FileDocumentManager.getInstance().saveDocument(editor.getDocument());
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            scriptExecutionManager.executeScript(psiFile.getVirtualFile());
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }


    @Override
    protected Integer getGroupPriority() {
        return 3;
    }
}

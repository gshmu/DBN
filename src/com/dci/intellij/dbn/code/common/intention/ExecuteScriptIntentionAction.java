package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class ExecuteScriptIntentionAction extends GenericIntentionAction {
    @NotNull
    public String getText() {
        return "Execute SQL script...";
    }

    @NotNull
    public String getFamilyName() {
        return "Statement execution intentions";
    }

    public Icon getIcon(int flags) {
        return Icons.EXECUTE_SQL_SCRIPT;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile != null && psiFile.getLanguage() instanceof DBLanguage) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile instanceof DBConsoleVirtualFile) {
                DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                if (consoleVirtualFile.getType() == DBConsoleType.DEBUG) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile != null && editor != null && psiFile.getLanguage() instanceof DBLanguage) {
            FileDocumentManager.getInstance().saveDocument(editor.getDocument());
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            scriptExecutionManager.executeScript(psiFile.getVirtualFile());
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}

package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ExecuteStatementIntentionAction extends GenericIntentionAction {
    @NotNull
    public String getText() {
        return "Execute statement";
    }

    @NotNull
    public String getFamilyName() {
        return "Statement execution intentions";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.STMT_EXECUTION_RUN;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguageFile) {
            ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(psiFile);

            return  executable != null &&
                    executable.getExecutionProcessor() != null && executable.getExecutionProcessor().canExecute();
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(psiFile);
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        executionManager.fireExecution(executable.getExecutionProcessor());
        DocumentUtil.refreshEditorAnnotations(executable.getFile());
    }

    public boolean startInWriteAction() {
        return false;
    }
}

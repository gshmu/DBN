package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
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
        if (psiFile instanceof DBLanguagePsiFile) {
            ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
            if (executable != null) {
                StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
                StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(editor, executable, true);
                if (executionProcessor != null) {
                    StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                    return executionResult != null;
                }
            }
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        if (executable != null) {
            StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
            StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(editor, executable, true);
            executionManager.executeStatement(executionProcessor);
            DocumentUtil.refreshEditorAnnotations(executable.getFile());
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}

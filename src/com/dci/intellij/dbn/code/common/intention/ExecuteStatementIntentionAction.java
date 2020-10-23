package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ExecuteStatementIntentionAction extends GenericIntentionAction implements HighPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Execute statement";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.STMT_EXECUTION_RUN;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile != null) {
            VirtualFile virtualFile = psiFile.getVirtualFile();

            if (DatabaseDebuggerManager.isDebugConsole(virtualFile)) {
                return false;
            }
            if (virtualFile != null && virtualFile.getFileType() instanceof DBLanguageFileType) {
                ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
                FileEditor fileEditor = EditorUtil.getFileEditor(editor);
                if (executable != null && fileEditor != null) {
                    return true;
/*
                    StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
                    StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(fileEditor, executable, true);
                    if (executionProcessor != null) {
                        StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                        return executionResult != null;
                    }
*/
                }
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        FileEditor fileEditor = EditorUtil.getFileEditor(editor);
        if (executable != null && fileEditor != null) {
            StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
            StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(fileEditor, executable, true);
            if (executionProcessor != null) {
                executionManager.executeStatement(executionProcessor);
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @NotNull
    @Override
    public Priority getPriority() {
        return Priority.TOP;
    }
}

package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.Context;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DebugStatementIntentionAction extends GenericIntentionAction implements HighPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Debug statement";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.STMT_EXECUTION_DEBUG;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile != null) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile != null && virtualFile.getFileType() instanceof DBLanguageFileType) {
                ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
                FileEditor fileEditor = EditorUtil.getFileEditor(editor);
                if (executable != null && fileEditor != null) {
                    return executable.is(ElementTypeAttribute.DEBUGGABLE);
                }
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull final Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        FileEditor fileEditor = EditorUtil.getFileEditor(editor);
        if (executable != null && fileEditor != null && psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile databasePsiFile = (DBLanguagePsiFile) psiFile;
            DataContext dataContext = Context.getDataContext(editor);

            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.selectConnectionAndSchema(
                    databasePsiFile,
                    dataContext,
                    () -> ConnectionAction.invoke("", false, databasePsiFile,
                            (action) -> {
                                StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
                                StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(fileEditor, executable, true);
                                if (executionProcessor != null) {
                                    DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                                    debuggerManager.startStatementDebugger(executionProcessor);
                                }
                            }));
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 1;
    }
}

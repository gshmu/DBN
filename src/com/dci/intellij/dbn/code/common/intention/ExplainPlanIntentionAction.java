package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.Context;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.explain.ExplainPlanManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
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

import javax.swing.Icon;

public class ExplainPlanIntentionAction extends GenericIntentionAction implements HighPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Explain plan for statement";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.STMT_EXECUTION_EXPLAIN;
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
                FileEditor fileEditor = Editors.getFileEditor(editor);
                if (executable != null && fileEditor != null && executable.is(ElementTypeAttribute.DATA_MANIPULATION)) {
                    ConnectionHandler activeConnection = executable.getConnectionHandler();
                    return DatabaseFeature.EXPLAIN_PLAN.isSupported(activeConnection);
                }
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        FileEditor fileEditor = Editors.getFileEditor(editor);
        if (executable != null && fileEditor != null) {
            DataContext dataContext = Context.getDataContext(editor);
            ExplainPlanManager explainPlanManager = ExplainPlanManager.getInstance(project);
            explainPlanManager.executeExplainPlan(executable, dataContext, null);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 2;
    }
}

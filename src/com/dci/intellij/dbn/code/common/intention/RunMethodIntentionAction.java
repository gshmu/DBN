package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class RunMethodIntentionAction extends AbstractMethodExecutionIntentionAction {

    @NotNull
    public String getText() {
        DBMethod method = getMethod();
        if (method != null) {
            return "Run " + method.getObjectType().getName() + " " + method.getName();
        }
        return "Execute method";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.METHOD_EXECUTION_RUN;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile != null) {
            DBMethod method = resolveMethod(psiFile);
            return method != null && DatabaseFeature.DEBUGGING.isSupported(method);
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        DBMethod method = resolveMethod(psiFile);
        if (method != null) {
            MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
            if (executionManager.promptExecutionDialog(method, false)) {
                executionManager.execute(method);
            }
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}

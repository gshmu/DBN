package com.dci.intellij.dbn.execution.explain.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.explain.ExplainPlanManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class ExplainPlanEditorAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookup.getEditor(e);
        if (Failsafe.check(editor)) {
            FileEditor fileEditor = Editors.getFileEditor(editor);
            ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
            if (fileEditor != null && executable != null && executable.is(ElementTypeAttribute.DATA_MANIPULATION)) {
                ExplainPlanManager explainPlanManager = ExplainPlanManager.getInstance(project);
                explainPlanManager.executeExplainPlan(executable, e.getDataContext(), null);
            }
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.STMT_EXECUTION_EXPLAIN);
        presentation.setText("Explain Plan for Statement");

        boolean visible = false;
        boolean enabled = false;

        Editor editor = Lookup.getEditor(e);
        if (editor != null) {
            PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
            if (psiFile instanceof DBLanguagePsiFile) {
                DBLanguagePsiFile languagePsiFile = (DBLanguagePsiFile) psiFile;

                ConnectionHandler connection = languagePsiFile.getConnection();
                visible = isVisible(e) && DatabaseFeature.EXPLAIN_PLAN.isSupported(connection);

                if (visible) {
                    ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
                    if (executable != null && executable.is(ElementTypeAttribute.DATA_MANIPULATION)) {
                        enabled = true;
                    }
                }
            }
        }
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }
}

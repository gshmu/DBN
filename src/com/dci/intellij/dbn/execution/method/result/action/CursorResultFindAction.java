package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CursorResultFindAction extends MethodExecutionCursorResultAction {
    public CursorResultFindAction() {
        super("Find Data", Icons.ACTION_FIND);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        MethodExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        if (Failsafe.check(cursorResultForm)) {
            cursorResultForm.showSearchHeader();
        }
    }
}

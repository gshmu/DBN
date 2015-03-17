package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CursorResultFindAction extends MethodExecutionCursorResultAction {
    public CursorResultFindAction() {
        super("Find Data", Icons.ACTION_FIND);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MethodExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        if (cursorResultForm != null) {
            cursorResultForm.showSearchHeader();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Find Data");
    }
}

package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditMethodAction extends MethodExecutionResultAction {
    public EditMethodAction(MethodExecutionResult executionResult) {
        super(executionResult, "Edit method", Icons.OBEJCT_EDIT_SOURCE);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DatabaseFileSystem.getInstance().openEditor(executionResult.getMethod(), true);
    }
}
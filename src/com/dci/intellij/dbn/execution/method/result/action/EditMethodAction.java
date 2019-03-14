package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class EditMethodAction extends MethodExecutionResultAction {
    public EditMethodAction() {
        super("Edit Method", Icons.OBEJCT_EDIT_SOURCE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
            databaseFileSystem.connectAndOpenEditor(executionResult.getMethod(), null, true, true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(executionResult != null);
        presentation.setText("Edit Method");
    }
}
package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExecutionResultFindDataAction extends AbstractExecutionResultAction {
    public ExecutionResultFindDataAction() {
        super("Find data", Icons.ACTION_FIND);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            executionResult.getForm().showSearchHeader();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Find Data");
    }
}

package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DebugMethodAction extends AnObjectAction<DBMethod> {
    public DebugMethodAction(DBMethod method) {
        super("Debug...", Icons.METHOD_EXECUTION_DEBUG, method);
    }

    DebugMethodAction(DBProgram program, DBMethod method) {
        super(method);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBMethod object) {

        DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(project);
        executionManager.startMethodDebugger(object);
    }
}

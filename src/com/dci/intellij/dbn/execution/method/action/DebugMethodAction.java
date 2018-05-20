package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DebugMethodAction extends AnObjectAction<DBMethod> {
    public DebugMethodAction(DBMethod method) {
        super("Debug...", Icons.METHOD_EXECUTION_DEBUG, method);
    }

    public DebugMethodAction(DBProgram program, DBMethod method) {
        super(method);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBMethod method = getObject();
        if (method != null) {
            DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(method.getProject());
            executionManager.startMethodDebugger(method);
        }
    }
}

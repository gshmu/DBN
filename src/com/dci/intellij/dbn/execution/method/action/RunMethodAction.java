package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RunMethodAction extends AnObjectAction<DBMethod> {
    public RunMethodAction(DBMethod method) {
        super("Run...", Icons.METHOD_EXECUTION_RUN, method);
    }

    public RunMethodAction(DBProgram program, DBMethod method) {
        super(method);
    }

    public void actionPerformed(AnActionEvent e) {
        DBMethod method = getObject();
        if (method != null) {
            MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
            executionManager.startMethodExecution(method, DBDebuggerType.NONE);
        }
    }
}

package com.dci.intellij.dbn.execution.method.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DebugMethodAction extends DumbAwareAction {
    private DBObjectRef<DBMethod> methodRef;
    public DebugMethodAction(DBMethod method) {
        super("Debug...", "", Icons.METHOD_EXECUTION_DEBUG);
        this.methodRef = DBObjectRef.from(method);
    }

    public DebugMethodAction(DBProgram program, DBMethod method) {
        super(NamingUtil.enhanceUnderscoresForDisplay(method.getName()), "", method.getIcon());
        this.methodRef = DBObjectRef.from(method);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBMethod method = DBObjectRef.getnn(methodRef);
        DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(method.getProject());
        executionManager.startMethodDebugger(method);
    }
}

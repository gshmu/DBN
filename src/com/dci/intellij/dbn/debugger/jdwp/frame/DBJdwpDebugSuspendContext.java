package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XSuspendContext;

public class DBJdwpDebugSuspendContext extends XSuspendContext{
    private DBJdwpDebugExecutionStack executionStack;
    private XSuspendContext underlyingSuspendContext;

    public DBJdwpDebugSuspendContext(DBJdwpDebugProcess debugProcess) {
        this.executionStack = new DBJdwpDebugExecutionStack(debugProcess);
        this.underlyingSuspendContext = debugProcess.getSession().getSuspendContext();
    }

    public XSuspendContext getUnderlyingSuspendContext() {
        return underlyingSuspendContext;
    }

    @Override
    public DBJdwpDebugExecutionStack getActiveExecutionStack() {
        return executionStack;
    }
}

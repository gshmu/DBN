package com.dci.intellij.dbn.debugger.jdbc.frame;

import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramDebugProcess;
import com.intellij.xdebugger.frame.XSuspendContext;

public class DBProgramDebugSuspendContext extends XSuspendContext{
    private DBProgramDebugExecutionStack executionStack;

    public DBProgramDebugSuspendContext(DBProgramDebugProcess debugProcess) {
        this.executionStack = new DBProgramDebugExecutionStack(debugProcess);
    }

    @Override
    public DBProgramDebugExecutionStack getActiveExecutionStack() {
        return executionStack;
    }
}

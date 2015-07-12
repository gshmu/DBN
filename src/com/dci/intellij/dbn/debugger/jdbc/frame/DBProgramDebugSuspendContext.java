package com.dci.intellij.dbn.debugger.jdbc.frame;

import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.intellij.xdebugger.frame.XSuspendContext;

public class DBProgramDebugSuspendContext extends XSuspendContext{
    private DBProgramDebugExecutionStack executionStack;

    public DBProgramDebugSuspendContext(DBJdbcDebugProcess debugProcess) {
        this.executionStack = new DBProgramDebugExecutionStack(debugProcess);
    }

    @Override
    public DBProgramDebugExecutionStack getActiveExecutionStack() {
        return executionStack;
    }
}

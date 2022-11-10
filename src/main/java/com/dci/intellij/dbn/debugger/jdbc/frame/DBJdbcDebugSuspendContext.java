package com.dci.intellij.dbn.debugger.jdbc.frame;

import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.intellij.xdebugger.frame.XSuspendContext;

public class DBJdbcDebugSuspendContext extends XSuspendContext{
    private DBJdbcDebugExecutionStack executionStack;

    public DBJdbcDebugSuspendContext(DBJdbcDebugProcess debugProcess) {
        this.executionStack = new DBJdbcDebugExecutionStack(debugProcess);
    }

    @Override
    public DBJdbcDebugExecutionStack getActiveExecutionStack() {
        return executionStack;
    }
}

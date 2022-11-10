package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XSuspendContext;

public class DBJdwpDebugSuspendContext extends XSuspendContext{
    private final DBJdwpDebugProcess debugProcess;
    private final XSuspendContext underlyingContext;

    private final Latent<DBJdwpDebugExecutionStack> executionStack = Latent.basic(() -> new DBJdwpDebugExecutionStack(DBJdwpDebugSuspendContext.this));

    public DBJdwpDebugSuspendContext(DBJdwpDebugProcess debugProcess, XSuspendContext underlyingContext) {
        this.debugProcess = debugProcess;
        this.underlyingContext = underlyingContext;
    }

    XSuspendContext getUnderlyingContext() {
        return underlyingContext;
    }

    public DBJdwpDebugProcess getDebugProcess() {
        return debugProcess;
    }

    private DBJdwpDebugExecutionStack getExecutionStack() {
        return executionStack.get();
    }

    @Override
    public DBJdwpDebugExecutionStack getActiveExecutionStack() {
        return getExecutionStack();
    }
}

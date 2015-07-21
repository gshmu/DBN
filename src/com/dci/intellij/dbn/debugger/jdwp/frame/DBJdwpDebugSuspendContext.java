package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XSuspendContext;

public class DBJdwpDebugSuspendContext extends XSuspendContext{
    private DBJdwpDebugProcess debugProcess;
    private XSuspendContext underlyingContext;
    private LazyValue<DBJdwpDebugExecutionStack> executionStack = new SimpleLazyValue<DBJdwpDebugExecutionStack>() {
        @Override
        protected DBJdwpDebugExecutionStack load() {
            return new DBJdwpDebugExecutionStack(DBJdwpDebugSuspendContext.this);
        }
    };

    public DBJdwpDebugSuspendContext(DBJdwpDebugProcess debugProcess, XSuspendContext underlyingContext) {
        this.debugProcess = debugProcess;
        this.underlyingContext = underlyingContext;
    }

    public XSuspendContext getUnderlyingContext() {
        return underlyingContext;
    }

    public DBJdwpDebugProcess getDebugProcess() {
        return debugProcess;
    }

    public DBJdwpDebugExecutionStack getExecutionStack() {
        return executionStack.get();
    }

    @Override
    public DBJdwpDebugExecutionStack getActiveExecutionStack() {
        return getExecutionStack();
    }
}

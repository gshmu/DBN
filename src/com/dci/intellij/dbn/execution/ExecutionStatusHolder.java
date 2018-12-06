package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

public class ExecutionStatusHolder extends PropertyHolderImpl<ExecutionStatus> {

    public ExecutionStatusHolder() {
        super();
    }

    public boolean canExecute() {
        return isNot(QUEUED) && isNot(EXECUTING) && isNot(CANCELLED);
    }

    @Override
    protected ExecutionStatus[] properties() {
        return ExecutionStatus.values();
    }
}

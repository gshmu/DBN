package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

public class ExecutionStatusHolder extends PropertyHolderImpl<ExecutionStatus> {

    public ExecutionStatusHolder() {
        super(ExecutionStatus.class);
    }

    public boolean canExecute() {
        return isNot(QUEUED) && isNot(EXECUTING) && isNot(CANCELLED);
    }
}

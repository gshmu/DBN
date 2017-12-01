package com.dci.intellij.dbn.execution;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

public class ExecutionStatusHolder extends PropertyHolderImpl<ExecutionStatus> {

    public ExecutionStatusHolder() {
        super(ExecutionStatus.class);
    }

    public boolean canExecute() {
        return !is(QUEUED) && !is(EXECUTING) && !is(CANCELLED);
    }

    public void assertNotCancelled() throws SQLException {
        if (is(CANCELLED)) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }
}

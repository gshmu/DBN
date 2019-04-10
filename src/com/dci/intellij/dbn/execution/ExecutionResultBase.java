package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;

public abstract class ExecutionResultBase<F extends ExecutionResultForm> extends DisposableBase implements ExecutionResult<F> {
    @Override
    public void disposeInner() {
        ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
        executionManager.releaseResultForm(this);
        super.disposeInner();
    }
}

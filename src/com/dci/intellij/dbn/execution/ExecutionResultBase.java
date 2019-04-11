package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExecutionResultBase<F extends ExecutionResultForm> extends DisposableBase implements ExecutionResult<F> {
    @Override
    public void disposeInner() {
        ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
        executionManager.releaseResultForm(this);
        super.disposeInner();
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        return null;
    }
}

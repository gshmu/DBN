package com.dci.intellij.dbn.execution.common.result.ui;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExecutionResultFormBase<T extends ExecutionResult<?>> extends DBNFormImpl implements ExecutionResultForm<T>{
    private T executionResult;

    public ExecutionResultFormBase(@NotNull T executionResult) {
        super(null, executionResult.getProject());
        this.executionResult = executionResult;
    }

    @NotNull
    @Override
    public final T getExecutionResult() {
        return Failsafe.nn(executionResult);
    }

    @Override
    public void setExecutionResult(@NotNull T executionResult) {
        if (this.executionResult != executionResult) {
            this.executionResult = SafeDisposer.replace(this.executionResult, executionResult, true);
            this.executionResult.setPrevious(null);
            rebuildForm();
        }
    }

    protected void rebuildForm(){}

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        Object data = super.getData(dataId);
        if (data == null) {
            data = getExecutionResult().getData(dataId);
        }
        return data;
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(executionResult);
        executionResult = null;
        super.disposeInner();
    }
}

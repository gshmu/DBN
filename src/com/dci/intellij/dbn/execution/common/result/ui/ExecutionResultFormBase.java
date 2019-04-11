package com.dci.intellij.dbn.execution.common.result.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.execution.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExecutionResultFormBase<T extends ExecutionResult> extends DBNFormImpl implements ExecutionResultForm<T>{
    private T executionResult;

    public ExecutionResultFormBase(@NotNull T executionResult) {
        super(executionResult.getProject());
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
            T oldExecutionResult = this.executionResult;
            this.executionResult = executionResult;
            this.executionResult.setPrevious(null);
            rebuildForm();
            Disposer.disposeInBackground(oldExecutionResult);
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
        super.disposeInner();
    }
}

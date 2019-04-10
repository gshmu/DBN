package com.dci.intellij.dbn.execution.common.result.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.execution.ExecutionResult;
import org.jetbrains.annotations.NotNull;

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
        this.executionResult = executionResult;
    }

    @Override
    public final void replaceExecutionResult(@NotNull T executionResult) {
        if (this.executionResult != executionResult) {
            T oldExecutionResult = this.executionResult;
            this.executionResult = executionResult;
            Disposer.disposeInBackground(oldExecutionResult);
        }
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(executionResult);
        super.disposeInner();
    }
}

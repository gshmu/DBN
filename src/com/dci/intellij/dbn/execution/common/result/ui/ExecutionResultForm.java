package com.dci.intellij.dbn.execution.common.result.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.execution.ExecutionResult;
import org.jetbrains.annotations.NotNull;

public interface ExecutionResultForm<E extends ExecutionResult> extends DBNForm {
    void setExecutionResult(@NotNull E executionResult);
    @NotNull E getExecutionResult();
}

package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class ExecutionOptions extends PropertyHolderImpl<ExecutionOption>{

    @Override
    protected ExecutionOption[] properties() {
        return ExecutionOption.values();
    }

    @Override
    public ExecutionOptions clone() {
        return (ExecutionOptions) super.clone();
    }
}

package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class ExecutionOptions extends PropertyHolderImpl<ExecutionOption> {
    public ExecutionOptions(ExecutionOption... properties) {
        super(properties);
    }

    public static ExecutionOptions clone(ExecutionOptions source) {
        ExecutionOptions options = new ExecutionOptions();
        options.computed(source.computed());
        return options;
    }

    @Override
    protected ExecutionOption[] properties() {
        return ExecutionOption.values();
    }
}

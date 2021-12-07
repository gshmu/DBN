package com.dci.intellij.dbn.execution;


import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class ExecutionOptions extends PropertyHolderBase.IntStore<ExecutionOption> {
    public ExecutionOptions(ExecutionOption... properties) {
        super(properties);
    }

    public static ExecutionOptions clone(ExecutionOptions source) {
        ExecutionOptions options = new ExecutionOptions();
        options.replace(source);
        return options;
    }

    @Override
    protected ExecutionOption[] properties() {
        return ExecutionOption.values();
    }
}

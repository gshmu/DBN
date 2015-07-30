package com.dci.intellij.dbn.debugger.common.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.execution.configurations.RunConfiguration;

public abstract class DBMethodRunConfigFactory<T extends DBRunConfigType, C extends DBMethodRunConfig> extends DBRunConfigFactory<T, C> {
    protected DBMethodRunConfigFactory(T type) {
        super(type);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        C runConfiguration = (C) configuration;
        MethodExecutionInput executionInput = runConfiguration.getExecutionInput();
        if (runConfiguration.isGeneric() || executionInput == null) {
            return getIcon();
        } else {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            DBMethod method = methodRef.get();
            return method == null ? methodRef.getObjectType().getIcon() : method.getIcon();
        }
    }

    public abstract DBMethodRunConfig createConfiguration(DBMethod method);
}

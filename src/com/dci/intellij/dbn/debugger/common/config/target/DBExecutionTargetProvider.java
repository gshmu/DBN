package com.dci.intellij.dbn.debugger.common.config.target;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetProvider;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;

public class DBExecutionTargetProvider extends ExecutionTargetProvider{
    @NotNull
    @Override
    public List<ExecutionTarget> getTargets(@NotNull Project project, @NotNull RunnerAndConfigurationSettings configuration) {
        if (configuration.getConfiguration() instanceof DBRunConfig) {
            return DBExecutionTarget.asList();
        }
        return Collections.emptyList();
    }
}

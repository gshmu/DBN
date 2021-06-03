package com.dci.intellij.dbn.debugger.common.config.target;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetProvider;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DBExecutionTargetProvider extends ExecutionTargetProvider{
    @NotNull
    @Override
    @Compatibility
    public List<ExecutionTarget> getTargets(@NotNull Project project, @NotNull RunnerAndConfigurationSettings configuration) {
        return getTargets(project, configuration.getConfiguration());
    }

    @NotNull
    @Override
    public List<ExecutionTarget> getTargets(@NotNull Project project, @NotNull RunConfiguration config) {
        if (config instanceof DBRunConfig) {
            return DBExecutionTarget.asList();
        }
        return Collections.emptyList();
    }
}

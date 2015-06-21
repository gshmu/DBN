package com.dci.intellij.dbn.debugger.execution.common;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetProvider;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DBExecutionTargetProvider extends ExecutionTargetProvider{
    @NotNull
    @Override
    public List<ExecutionTarget> getTargets(@NotNull Project project, @NotNull RunnerAndConfigurationSettings configuration) {
        if (configuration.getConfiguration() instanceof DBProgramRunConfiguration) {
            return DBExecutionTarget.asList();
        }
        return Collections.emptyList();
    }
}

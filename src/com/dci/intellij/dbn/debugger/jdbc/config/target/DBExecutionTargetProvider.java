package com.dci.intellij.dbn.debugger.jdbc.config.target;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.jdbc.config.DBProgramRunConfiguration;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetProvider;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;

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

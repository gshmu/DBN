package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ExecutionTimeout {
    private ProjectRef projectRef;
    private ExecutionTarget executionTarget;
    private boolean debug;
    private int customValue;
    private int settingsValue;

    ExecutionTimeout(@NotNull Project project, ExecutionTarget executionTarget, boolean debug) {
        this.projectRef = ProjectRef.from(project);
        this.executionTarget = executionTarget;
        this.debug = debug;
        this.settingsValue = getSettingsExecutionTimeout();
        this.customValue = getSettingsExecutionTimeout();
    }

    public int get() {
        int timeout = getSettingsExecutionTimeout();
        if (customValue != settingsValue) {
            this.settingsValue = timeout;
        } else {
            this.settingsValue = timeout;
            this.customValue = timeout;
        }
        return customValue;
    }

    public void set(int value) {
        this.customValue = value;
    }

    private int getSettingsExecutionTimeout() {
        Project project = projectRef.ensure();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        ExecutionTimeoutSettings timeoutSettings = executionEngineSettings.getExecutionTimeoutSettings(executionTarget);
        return debug ?
                timeoutSettings.getDebugExecutionTimeout() :
                timeoutSettings.getExecutionTimeout();
    }

}

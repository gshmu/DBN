package com.dci.intellij.dbn.execution;

import com.intellij.openapi.project.Project;

public abstract class RemoteExecutionInput extends ExecutionInput{
    public RemoteExecutionInput(Project project, ExecutionTarget executionTarget) {
        super(project, executionTarget);
    }
}

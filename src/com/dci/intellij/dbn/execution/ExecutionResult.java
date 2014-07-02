package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.intellij.openapi.project.Project;

import javax.swing.Icon;

public interface ExecutionResult {

    ExecutionResultForm getResultPanel();

    String getResultName();

    Icon getResultIcon();

    boolean isOrphan();

    void setExecutionDuration(int executionDuration);
    
    int getExecutionDuration();

    Project getProject();

    ConnectionHandler getConnectionHandler();
}
